package ch.admin.foitt.wallet.platform.login.domain.usecase

interface AfterLoginWork {
    suspend operator fun invoke()
}
