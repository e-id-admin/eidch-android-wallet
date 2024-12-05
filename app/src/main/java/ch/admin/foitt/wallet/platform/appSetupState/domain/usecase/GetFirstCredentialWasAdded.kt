package ch.admin.foitt.wallet.platform.appSetupState.domain.usecase

fun interface GetFirstCredentialWasAdded {
    suspend operator fun invoke(): Boolean
}
