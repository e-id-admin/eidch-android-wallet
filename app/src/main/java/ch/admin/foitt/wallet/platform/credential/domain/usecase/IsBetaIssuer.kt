package ch.admin.foitt.wallet.platform.credential.domain.usecase

fun interface IsBetaIssuer {
    suspend operator fun invoke(credentialIssuer: String?): Boolean
}
