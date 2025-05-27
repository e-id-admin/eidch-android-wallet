package ch.admin.foitt.wallet.platform.oca.domain.model

interface OcaCesrHashAlgorithm {
    val prefix: String
    val name: String
    val dummyDigest: String
    val paddingSize: Int

    data object Sha256 : OcaCesrHashAlgorithm {
        override val prefix = "I"
        override val name = "sha256"
        override val dummyDigest = "#".repeat(44)
        override val paddingSize = 1
    }

    companion object {
        private val algorithms = listOf(Sha256)

        fun fromDigest(digest: String): OcaCesrHashAlgorithm? = algorithms.firstOrNull {
            digest.startsWith(it.prefix)
        }
    }
}
