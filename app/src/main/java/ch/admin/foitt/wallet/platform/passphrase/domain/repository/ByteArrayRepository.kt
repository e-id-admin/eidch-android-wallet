package ch.admin.foitt.wallet.platform.passphrase.domain.repository

interface ByteArrayRepository {
    suspend fun get(): ByteArray
    suspend fun save(data: ByteArray)
}
