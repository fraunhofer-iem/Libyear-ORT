package http.maven

import artifact.model.MetadataDto
import artifact.model.VersionDto
import http.maven.model.api.MavenApiResponseDto
import http.maven.model.api.Response
import http.maven.model.repository.MavenMetadata
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.xml.*
import kotlinx.serialization.json.Json


class MavenClient {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json { ignoreUnknownKeys = true }
            )
            xml(
                contentType = ContentType.Text.Xml
            )
        }
    }

    suspend fun getVersionsFromSearch(namespace: String, name: String, retry: Int = 5): List<VersionDto> {

        val mavenUrl = "https://search.maven.org/solrsearch/select?q=g:$namespace+AND+a:$name&core=gav&rows=20&wt=json"
        var mavenApiResponseDto: Response? = null

        for (i in 1..retry) {
            try {
                val response = httpClient.request(mavenUrl)
                if (response.status == HttpStatusCode.OK) {
                    val currentResponse = response.body<MavenApiResponseDto>()
                    println("Maven api response:${currentResponse}")

                    if (currentResponse.responseHeader?.status == 0) {
                        println("successfully got response from maven search query in try $i")
                        mavenApiResponseDto = currentResponse.response
                        break
                    }
                }
            } catch (exception: Exception) {
                println("Exception during http call to $mavenUrl. Try $i")
            }
        }

        return mavenApiResponseDto?.docs?.mapNotNull {
            if (it.v != null && it.timestamp != null) {
                VersionDto(versionNumber = it.v, releaseDate = it.timestamp)
            } else {
                println("Insufficient data in maven response to create version dto")
                println(it)
                null
            }
        } ?: emptyList()
    }


    suspend fun getAllVersionsFromRepo(namespace: String, name: String): MetadataDto? {
        val changedNameSpaceFormat = namespace.replace('.', '/')
        val mavenUrl = "https://repo.maven.apache.org/maven2/${changedNameSpaceFormat}/$name/maven-metadata.xml"
        val response = httpClient.request(mavenUrl)


        val body = response.body<MavenMetadata>()

        return if (body.artifactId != null && body.groupId != null && body.versioning?.versions != null) {
            MetadataDto(
                artifactId = body.artifactId,
                groupId = body.groupId,
                versions = body.versioning.versions.version
            )
        } else {
            null
        }

    }
}
