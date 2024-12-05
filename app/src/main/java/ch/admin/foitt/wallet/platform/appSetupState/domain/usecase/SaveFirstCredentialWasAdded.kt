package ch.admin.foitt.wallet.platform.appSetupState.domain.usecase

fun interface SaveFirstCredentialWasAdded {
    suspend operator fun invoke()
}
