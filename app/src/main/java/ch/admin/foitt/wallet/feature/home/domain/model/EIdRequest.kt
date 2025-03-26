package ch.admin.foitt.wallet.feature.home.domain.model

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.utils.asDayFullMonthYear
import ch.admin.foitt.wallet.platform.utils.epochSecondsToZonedDateTime
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import timber.log.Timber
import java.util.Locale

data class EIdRequest(
    val state: EIdRequestQueueState?,
    val firstName: String,
    val lastName: String,
    val onlineSessionStartOpenAt: String? = null,
    val onlineSessionStartTimeoutAt: String? = null,
)

fun EIdRequestCaseWithState.toEIdRequest(currentAppLocale: Locale) = EIdRequest(
    state = this.state?.state,
    firstName = this.case.firstName,
    lastName = this.case.lastName,
    onlineSessionStartOpenAt = state?.let {
        runSuspendCatching {
            it.onlineSessionStartOpenAt?.epochSecondsToZonedDateTime()?.asDayFullMonthYear(currentAppLocale)
        }.onFailure { throwable ->
            Timber.w(throwable, "Could not parse onlineSessionStartOpenAt date")
        }.get()
    },
    onlineSessionStartTimeoutAt = state?.let {
        runSuspendCatching {
            it.onlineSessionStartTimeoutAt?.epochSecondsToZonedDateTime()?.asDayFullMonthYear(currentAppLocale)
        }.onFailure { throwable ->
            Timber.w(throwable, "Could not parse onlineSessionStartTimeoutAt date")
        }.get()
    }
)
