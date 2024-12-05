package ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.ReportError
import com.dynatrace.android.agent.Dynatrace
import javax.inject.Inject

class ReportErrorImpl @Inject constructor() : ReportError {
    override fun invoke(errorMessage: String, error: Throwable?) {
        Dynatrace.reportError(errorMessage, error)
    }
}
