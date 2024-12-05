package ch.admin.foitt.wallet.platform.crypto.domain.model

data class HashConfiguration(
    val hashAlgorithm: String = "PBKDF2withHmacSHA512",
    val hashIterations: Int = 210_000,
    val hashKeyLength: Int = 512,
    val saltLength: Int = 16
)
