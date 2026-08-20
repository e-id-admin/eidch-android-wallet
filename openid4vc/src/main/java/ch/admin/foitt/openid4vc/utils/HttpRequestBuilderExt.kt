package ch.admin.foitt.openid4vc.utils

import ch.admin.foitt.openid4vc.domain.model.TokenType
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

fun HttpRequestBuilder.acceptLanguageHeader() {
    val supportedLocales = listOf("de-CH", "en", "fr-CH", "it-CH", "rm")
    val acceptLanguageHeaderValue = supportedLocales.joinToString(separator = ", ") { it }
    header(HttpHeaders.AcceptLanguage, acceptLanguageHeaderValue)
}

fun HttpRequestBuilder.authorizationHeader(
    tokenType: TokenType,
    accessToken: String,
) = header(HttpHeaders.Authorization, "${tokenType.value} $accessToken")

fun HttpRequestBuilder.dpopHeader(
    dpop: String,
) = header(Constants.DPOP_HEADER, dpop)
