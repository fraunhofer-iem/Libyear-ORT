import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import dependencies.DependencyAnalyzer
import dependencies.db.AnalyzerResult
import dependencies.model.DependencyGraphDto
import git.GitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import libyears.LibyearCalculator
import libyears.LibyearResults
import util.DbConfig
import util.dbQuery
import util.initDatabase
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.system.measureTimeMillis

class DbOptions : OptionGroup() {
    val dbUrl by option(
        envvar = "DB_URL", help = "Optional path to store a file based database which contains" +
                " version numbers and their release dates." +
                "This database is used as a cache and the application works seamlessly without it." +
                "If the path doesn't exist it will be created."
    ).required()

    val userName by option(envvar = "DB_USER", help = "Username of database user").required()
    val password by option(envvar = "DB_PW", help = "Password for given database user").required()
}

@Serializable
data class GitConfig(val urls: List<String>)

class Libyears : CliktCommand() {
    val dbOptions by DbOptions().cooccurring()

//    val projectPath by option(envvar = "PROJECT_PATH", help = "Path to the analyzed project's root.")
//        .path(mustExist = true, mustBeReadable = true, mustBeWritable = true, canBeFile = false)
//        .required()

    val gitConfigFile by option(
        envvar = "GIT_CONFIG_PATH", help = "Path to the file containing the URLs of" +
                "the repositories which should be analyzed."
    )
        .path(mustExist = true, mustBeReadable = true, canBeFile = true)
        .required()

    val outputPath by option(
        envvar = "OUTPUT_PATH", help = "Path to the folder to store the JSON results" +
                "of the created dependency graph. If the path doesn't exist it will be created."
    )
        .path(mustExist = false, canBeFile = false)
        .required()

    override fun run() {
        echo(
            "Running libyears for projects in $gitConfigFile and output path $outputPath" +
                    " and db url ${dbOptions?.dbUrl}"
        )
        outputPath.createDirectories()
    }
}

@Serializable
data class AggregatedResults(val results: List<LibyearResults>)

suspend fun main(args: Array<String>) {
    val libyearCommand = Libyears()
    libyearCommand.main(args)
    val dbConfig = libyearCommand.dbOptions?.let {
        DbConfig(
            url = it.dbUrl,
            userName = it.userName,
            password = it.password
        )
    }

    val gits = getConfigFromPath(libyearCommand.gitConfigFile)


    val libyearResults: MutableList<LibyearResults> = mutableListOf()

    val runtime: Double = measureTimeMillis {
        gits.urls.forEachIndexed { idx, gitUrl ->
            println("Analyzing git at url $gitUrl")
            val outputPath = libyearCommand.outputPath.resolve("${Date().time}-$idx")
            outputPath.createDirectories()
            val gitHelper = GitHelper(gitUrl, outDir = outputPath.toFile())
            gitHelper.forEach { _ ->
                getLibYears(
                    projectPath = outputPath.toFile(),
                    outputPath = outputPath,
                    dbConfig = dbConfig,
                )?.let { libyears ->
                    libyearResults.add(
                        libyears
                    )
                }
            }
        }
    }.toDouble() / 60000
    println("The libyear calculation took $runtime minutes to execute.")

    // TODO: do this later and in one file for easier readability
    // include commit dates
    val outputFileAggregate = libyearCommand.outputPath.resolve("${Date().time}-graphResultAggregate.json").toFile()
    withContext(Dispatchers.IO) {
        outputFileAggregate.createNewFile()
        val json = Json { prettyPrint = false }
        val jsonString =
            json.encodeToString(AggregatedResults.serializer(), AggregatedResults(libyearResults))
        outputFileAggregate.writeText(jsonString)
    }
}

fun getConfigFromPath(path: Path): GitConfig {
    val json = Json
    return json.decodeFromString<GitConfig>(path.toFile().readText())
}


suspend fun getLibYears(projectPath: File, outputPath: Path?, dbConfig: DbConfig?): LibyearResults? {
    val useDb = dbConfig != null

    if (useDb) {
        initDatabase(dbConfig!!)
    }

    val dependencyAnalyzer = DependencyAnalyzer()

    dependencyAnalyzer.getAnalyzerResult(projectPath)?.let { dependencyAnalyzerResult ->
        // TODO: maven currently doesn't work without fixed versions. Need to check ORT if this can be circumvented
        // through configuration

        val libyearAggregates = LibyearCalculator.printDependencyGraph(dependencyAnalyzerResult.dependencyGraphDto)


        if (outputPath != null) {
            val outputFile = outputPath.resolve("${Date().time}-graphResult.json").toFile()
            withContext(Dispatchers.IO) {
                outputFile.createNewFile()
                val json = Json { prettyPrint = false }
                val jsonString =
                    json.encodeToString(DependencyGraphDto.serializer(), dependencyAnalyzerResult.dependencyGraphDto)
                outputFile.writeText(jsonString)
            }
        }


        if (useDb) {
            dbQuery {
                AnalyzerResult.new {
                    result = dependencyAnalyzerResult
                }
            }
        }

        return libyearAggregates
    }
    return null
}
