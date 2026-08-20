package ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase

fun interface IsCredentialRefreshable {
    suspend operator fun invoke(credentialId: Long): Boolean
}
