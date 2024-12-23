package ch.admin.foitt.wallet.platform.versionEnforcement.data.repository

import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.FetchVersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject

class VersionEnforcementRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val environmentSetupRepo: EnvironmentSetupRepository
) : VersionEnforcementRepository {
    override suspend fun fetchLatestHighPriority(): Result<VersionEnforcement?, FetchVersionEnforcementError> =
        runSuspendCatching {
            val list = httpClient.get(environmentSetupRepo.appVersionEnforcementUrl) {
                contentType(ContentType.Application.Json)
            }.body<List<VersionEnforcement>>()
            list.sortedByDescending { parseDate(it.created) }
                .firstOrNull {
                    it.priority == VersionEnforcement.Priority.HIGH && it.platform == VersionEnforcement.Platform.ANDROID
                }
        }.mapError(Throwable::toFetchVersionEnforcementInfoError)

    private fun parseDate(from: String): Instant = OffsetDateTime.parse(from).toInstant()
}

private fun Throwable.toFetchVersionEnforcementInfoError(): FetchVersionEnforcementError {
    Timber.e(this)
    return VersionEnforcementError.Unexpected(this)
}
