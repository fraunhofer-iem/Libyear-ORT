package artifact.model

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import libyears.LibyearCalculator
import org.apache.logging.log4j.kotlin.logger

data class CreateArtifactDto(
    var nameId: String? = null,
    var groupId: String? = null,
    var usedVersion: String? = null,
    var versionDeferred: Deferred<List<VersionDto>>? = null,
    var isTopLevelDependency: Boolean? = null,
    val transitiveDependencyDeferreds: List<Deferred<CreateArtifactDto?>> = emptyList(),
    val transitiveDependencies: List<CreateArtifactDto> = emptyList()
) {


    suspend fun toArtifactDto(): ArtifactDto {
        if (artifactIsComplete()) {

            val versions = try {
                versionDeferred?.await()
            } catch (exception: Exception) {
                logger.error { "API version job failed with error $exception" }
                null
            } ?: emptyList()

            val usedVersionDto = versions.find { it.versionNumber == usedVersion }
                ?: VersionDto(versionNumber = usedVersion!!)

            val deferredDeps = transitiveDependencyDeferreds.awaitAll().mapNotNull { it?.toArtifactDto() }

            return ArtifactDto(
                artifactId = nameId!!,
                groupId = groupId!!,
                usedVersion = usedVersionDto,
                isTopLevelDependency = isTopLevelDependency!!,
                versions = versions,
                transitiveDependencies = transitiveDependencies.map { it.toArtifactDto() } + deferredDeps,
                libyearResult = LibyearCalculator.calculateDifferenceForPackage(usedVersionDto, versions),
            )
        }

        throw Exception("Transformation of incomplete artifact not possible.")
    }

    private fun artifactIsComplete(): Boolean {
        return nameId != null &&
                groupId != null &&
                usedVersion != null &&
                isTopLevelDependency != null
    }
}
