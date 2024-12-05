package ch.admin.foitt.wallet.platform.eventTracking.domain.usecase

fun interface ReportError {
    operator fun invoke(errorMessage: String, error: Throwable?)
}
