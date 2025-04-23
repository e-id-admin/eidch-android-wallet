package ch.admin.foitt.openid4vc.utils

import java.security.MessageDigest
import java.util.Base64

fun String.createDigest(algorithm: String): String {
    val digest = MessageDigest.getInstance(algorithm)
    val bytes = digest.digest(toByteArray(Charsets.US_ASCII))
    return bytes.toBase64String()
}

fun String.base64ToDecodedString() = String(Base64.getDecoder().decode(this))
