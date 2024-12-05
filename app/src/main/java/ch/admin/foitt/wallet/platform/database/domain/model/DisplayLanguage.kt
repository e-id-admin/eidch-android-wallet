package ch.admin.foitt.wallet.platform.database.domain.model

object DisplayLanguage {
    const val DEFAULT = "en"
    const val FALLBACK = "fallback"
    val PRIORITIES = listOf("de", "en", "fallback")
}
