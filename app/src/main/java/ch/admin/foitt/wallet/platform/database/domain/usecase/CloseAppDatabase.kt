package ch.admin.foitt.wallet.platform.database.domain.usecase

interface CloseAppDatabase {
    suspend operator fun invoke()
}
