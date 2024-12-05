package ch.admin.foitt.wallet.platform.credential.domain.usecase

fun interface IsCredentialFromBetaIssuer {
    suspend operator fun invoke(credentialId: Long): Boolean
}
