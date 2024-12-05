package ch.admin.foitt.wallet.platform.database.domain.model

interface Claim {
    val key: String
    val value: String
    val valueType: String?
}
