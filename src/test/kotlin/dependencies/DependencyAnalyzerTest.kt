package dependencies

import artifact.ArtifactService
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import http.deps.DepsClient
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.ossreviewtoolkit.analyzer.Analyzer
import org.ossreviewtoolkit.model.*
import org.ossreviewtoolkit.model.config.AnalyzerConfiguration
import org.ossreviewtoolkit.model.config.RepositoryConfiguration
import org.ossreviewtoolkit.utils.ort.Environment
import java.io.File
import java.nio.file.Paths
import java.time.Instant

class DependencyAnalyzerTest {

    private lateinit var artifactService: ArtifactService

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        val viteNoDefaults = this.javaClass.classLoader.getResource("npm/apiResponseViteNoDefaults.json")?.readText()
        val vite = this.javaClass.classLoader.getResource("npm/apiResponseVite.json")?.readText()
        if (vite != null && viteNoDefaults != null) {
            val mockEngine = MockEngine { request ->
                if (request.url.toString() == "https://api.deps.dev/v3alpha/systems/npm/packages/viteNoDefaults") {
                    respond(
                        content = ByteReadChannel(viteNoDefaults, Charsets.UTF_8),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                } else {
                    respond(
                        content = ByteReadChannel(vite, Charsets.UTF_8),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            val httpClient = HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json { ignoreUnknownKeys = true }
                    )
                }
            }
            artifactService = ArtifactService(
                DepsClient(
                    httpClient = httpClient,
                    ioDispatcher = UnconfinedTestDispatcher()
                )
            )
        }
    }

    @Test
    fun emptyResultTest() = runTest {

        val analyzerMock = mockk<Analyzer>()

        every { analyzerMock.findManagedFiles(any(), any(), any())
        } returns Analyzer.ManagedFileInfo(absoluteProjectPath = File(""),
            managedFiles = emptyMap(),
            repositoryConfiguration = RepositoryConfiguration()
        )

        every { analyzerMock.analyze(any(), any())
        } returns OrtResult(
            repository = Repository.EMPTY,
            analyzer = AnalyzerRun(
                startTime = Instant.EPOCH,
                endTime = Instant.EPOCH,
                environment = Environment(),
                config = AnalyzerConfiguration(),
                result = AnalyzerResult(
                    projects = emptySet(),
                    packages = emptySet(),
                    issues = emptyMap()
                )
            ),
            scanner = null,
            advisor = null,
            evaluator = null,
            labels = emptyMap()
        )

        val dependencyAnalyzer = DependencyAnalyzer(
            artifactService = artifactService,
            analyzer = analyzerMock
        )

        val resourceDirectory = Paths.get("src", "test", "resources", "npm").toAbsolutePath()
        val file = resourceDirectory.toFile()

        dependencyAnalyzer.getDependencyPackagesForProject(file)
    }

    @Test
    fun dependencyGraphTest() = runTest {

        val analyzerMock = mockk<Analyzer>()

        every {
            analyzerMock.findManagedFiles(any(), any(), any())
        } returns Analyzer.ManagedFileInfo(absoluteProjectPath = File(""),
            managedFiles = emptyMap(),
            repositoryConfiguration = RepositoryConfiguration()
        )
        // 1. createScopes
        // scopes.filterKeys
        // scopes: Map<name, List<RootDependdencyIndex>>
        // 2. scope.dependencies
        // scope.name
        val dependencyGraphs: Map<String, DependencyGraph> = mapOf(
            "npm" to DependencyGraph()
        )
        val graphText = this.javaClass.classLoader.getResource("npm/analyzer-result.json")?.readText()
        val json = Json
        val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .findAndRegisterModules()

        if(graphText != null) {
            val ortResult = mapper.readValue<OrtResult>(graphText)
            println(ortResult.analyzer.toString())
        }
        every {
            analyzerMock.analyze(any(), any())
        } returns OrtResult(
            repository = Repository.EMPTY,
            analyzer = AnalyzerRun(
                startTime = Instant.EPOCH,
                endTime = Instant.EPOCH,
                environment = Environment(),
                config = AnalyzerConfiguration(),
                result = AnalyzerResult(
                    projects = emptySet(),
                    packages = emptySet(),
                    issues = emptyMap(),
                    dependencyGraphs = dependencyGraphs
                )
            ),
            scanner = null,
            advisor = null,
            evaluator = null,
            labels = emptyMap()
        )

        val dependencyAnalyzer = DependencyAnalyzer(
            artifactService = artifactService,
            analyzer = analyzerMock
        )

        val resourceDirectory = Paths.get("src", "test", "resources", "npm").toAbsolutePath()
        val file = resourceDirectory.toFile()

        dependencyAnalyzer.getDependencyPackagesForProject(file)
    }

}