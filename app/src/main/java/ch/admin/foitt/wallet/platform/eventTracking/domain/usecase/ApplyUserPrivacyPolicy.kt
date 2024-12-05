package ch.admin.foitt.wallet.platform.eventTracking.domain.usecase

fun interface ApplyUserPrivacyPolicy {
    operator fun invoke(hasAccepted: Boolean)
}
