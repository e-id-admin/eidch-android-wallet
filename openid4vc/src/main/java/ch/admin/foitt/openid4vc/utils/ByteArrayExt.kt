package ch.admin.foitt.openid4vc.utils

import java.util.Base64

internal fun ByteArray.toBase64String(): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(this)

internal fun String.base64StringToByteArray(): ByteArray =
    Base64.getUrlDecoder().decode(this)
