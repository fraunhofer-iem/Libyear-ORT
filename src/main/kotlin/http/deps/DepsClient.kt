package http.deps

import artifact.model.VersionDto
import http.deps.model.DepsResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DepsClient {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json { ignoreUnknownKeys = true }
            )
        }
    }

    suspend fun getVersionsForPackage(type: String, namespace: String, name: String): List<VersionDto> {

        val requestUrl: String? = when (type.lowercase()) {
            "maven" -> "https://api.deps.dev/v3alpha/systems/$type/packages/$namespace:$name"
            "gradle" ->
                // Gradle must be accessed with the maven key
                "https://api.deps.dev/v3alpha/systems/maven/packages/$namespace:$name"

            else -> null
        }
        return if (requestUrl != null) {
            val responseDto = try {
                val response = httpClient.request(requestUrl)

                val currentResponse = response.body<DepsResponseDto>()
                println("API response:${currentResponse}")

                currentResponse
            } catch (exception: Exception) {
                println("Exception during http call to $requestUrl. $exception")

                null
            }


            responseDto?.versions?.mapNotNull { version ->
                if (version.publishedAt != null) {
                    try {
                        VersionDto(
                            versionNumber = version.versionKey.version,
                            releaseDate = dateToMs(version.publishedAt)
                        )
                    } catch (exception: Exception) {
                        null
                    }
                } else {
                    println("Insufficient data in maven response to create version dto")
                    println(version)
                    null
                }
            } ?: emptyList()
        } else {
            println("Currently unsupported package manager")
            emptyList()
        }

    }

    private fun dateToMs(dateString: String): Long {
        val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val dateTime: OffsetDateTime = OffsetDateTime.parse(dateString, formatter)
        return dateTime.toInstant().toEpochMilli()
    }
}
