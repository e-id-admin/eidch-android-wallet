package ch.admin.foitt.wallet.platform.utils

import java.util.Base64

fun ByteArray.toBase64String(): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(this)

fun ByteArray.toNonUrlEncodedBase64String(): String =
    Base64.getEncoder().encodeToString(this)

fun String.base64StringToByteArray(): ByteArray =
    Base64.getUrlDecoder().decode(this)

fun String.base64NonUrlStringToByteArray(): ByteArray =
    Base64.getDecoder().decode(this)
