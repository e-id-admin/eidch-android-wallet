package ch.admin.foitt.openid4vc.utils

import io.ktor.http.ContentType

object ContentType {
    val applicationJwt = ContentType(ContentType.Application.TYPE, "jwt")
}

val ContentType.content: String
    get() = "$contentType/$contentSubtype"
