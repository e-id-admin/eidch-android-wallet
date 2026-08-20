package ch.admin.foitt.openid4vc.domain.model

/**
 * Base of all cryptographic algorithms known to the wallet.
 *
 * Concrete algorithm types define what an algorithm can be used for,
 * for example [SigningAlgorithm] or [SignatureAlgorithm].
 */
interface Algorithm {
    val stdName: String
}
