package ch.admin.foitt.openid4vc.utils

import ch.admin.foitt.openid4vc.domain.model.DigestAlgorithm
import java.security.MessageDigest
import java.util.Base64

internal fun ByteArray.toBase64StringUrlEncodedWithoutPadding(): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(this)

internal fun ByteArray.toNonUrlEncodedBase64String() = Base64.getEncoder().encodeToString(this)

fun ByteArray.createBase64Digest(algorithm: DigestAlgorithm): String {
    val digest = MessageDigest.getInstance(algorithm.stdName)
    val bytes = digest.digest(this)
    return bytes.toNonUrlEncodedBase64String()
}

fun ByteArray.createBase64UrlEncodedDigest(algorithm: DigestAlgorithm): String {
    val digest = MessageDigest.getInstance(algorithm.stdName)
    val bytes = digest.digest(this)
    return bytes.toBase64StringUrlEncodedWithoutPadding()
}
