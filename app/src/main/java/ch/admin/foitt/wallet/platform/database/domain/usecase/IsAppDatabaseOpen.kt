package ch.admin.foitt.wallet.platform.database.domain.usecase

fun interface IsAppDatabaseOpen {
    operator fun invoke(): Boolean
}
