package ch.admin.foitt.openid4vc.data

import ch.admin.foitt.openid4vc.domain.model.HttpErrorBody
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestProof
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInformationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toCredentialRequest
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

internal class CredentialOfferRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val safeJson: SafeJson,
) : CredentialOfferRepository {

    private val latestIssuerCredentialInformationMutex = Mutex()
    private var latestIssuerCredentialInformation: IssuerCredentialInformation? = null

    override suspend fun fetchIssuerCredentialInformation(issuerEndpoint: String, refresh: Boolean) =
        latestIssuerCredentialInformationMutex.withLock {
            if (refresh || latestIssuerCredentialInformation == null) {
                runSuspendCatching<IssuerCredentialInformation> {
                    httpClient.get("$issuerEndpoint/.well-known/openid-credential-issuer") {
                        contentType(ContentType.Application.Json)
                    }.body()
                }.mapError(Throwable::toFetchIssuerCredentialInformationError)
                    .onSuccess { latestIssuerCredentialInformation = it }
            } else {
                Ok(latestIssuerCredentialInformation!!)
            }
        }

    override suspend fun fetchIssuerConfiguration(issuerEndpoint: String, refresh: Boolean) =
        runSuspendCatching<IssuerConfiguration> {
            httpClient.get("$issuerEndpoint/.well-known/openid-configuration") {
                contentType(ContentType.Application.Json)
            }.body()
        }.mapError(Throwable::toFetchIssuerConfigurationError)

    override suspend fun fetchAccessToken(
        tokenEndpoint: String,
        preAuthorizedCode: String
    ) = runSuspendCatching<TokenResponse> {
        httpClient.post(tokenEndpoint) {
            url {
                parameters.append("grant_type", PRE_AUTHORIZED_KEY)
                parameters.append("pre-authorized_code", preAuthorizedCode)
            }
            contentType(ContentType.Application.Json)
        }.body()
    }.mapError { throwable ->
        when (throwable) {
            is ClientRequestException -> handleClientRequestException(throwable)
            else -> throwable.toFetchVerifiableCredentialError()
        }
    }

    override suspend fun fetchCredential(
        issuerEndpoint: String,
        credentialConfiguration: AnyCredentialConfiguration,
        proof: CredentialRequestProof?,
        accessToken: String
    ) = runSuspendCatching<CredentialResponse> {
        val credentialRequest = credentialConfiguration.toCredentialRequest(
            credentialId = credentialConfiguration.identifier,
            proof = proof,
        )
        httpClient.post(issuerEndpoint) {
            contentType(ContentType.Application.Json)
            header("Authorization", "BEARER $accessToken")
            setBody(credentialRequest)
        }.body()
    }.mapError { throwable ->
        when (throwable) {
            is ClientRequestException -> handleClientRequestException(throwable)
            else -> throwable.toFetchVerifiableCredentialError()
        }
    }

    private suspend fun handleClientRequestException(clientRequestException: ClientRequestException): FetchVerifiableCredentialError =
        when (clientRequestException.response.status) {
            HttpStatusCode.BadRequest -> parseError(clientRequestException)
            else -> CredentialOfferError.InvalidCredentialOffer
        }

    private suspend fun parseError(clientRequestException: ClientRequestException): FetchVerifiableCredentialError {
        val errorBodyString = clientRequestException.response.bodyAsText()
        val errorBodyResult = safeJson.safeDecodeStringTo<HttpErrorBody>(errorBodyString)
        return errorBodyResult.mapBoth(
            success = { handleErrorBody(it) },
            failure = { CredentialOfferError.InvalidCredentialOffer }
        )
    }

    private fun handleErrorBody(errorBody: HttpErrorBody): FetchVerifiableCredentialError = when (errorBody.error) {
        "invalid_grant" -> CredentialOfferError.InvalidGrant
        else -> CredentialOfferError.InvalidCredentialOffer
    }

    companion object {
        private const val PRE_AUTHORIZED_KEY =
            "urn:ietf:params:oauth:grant-type:pre-authorized_code"
    }
}

private fun Throwable.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError =
    when (this) {
        is IOException -> CredentialOfferError.NetworkInfoError
        else -> CredentialOfferError.Unexpected(this)
    }

private fun Throwable.toFetchIssuerCredentialInformationError(): FetchIssuerCredentialInformationError =
    when (this) {
        is IOException -> CredentialOfferError.NetworkInfoError
        else -> CredentialOfferError.Unexpected(this)
    }

private fun Throwable.toFetchIssuerConfigurationError(): FetchIssuerConfigurationError =
    when (this) {
        is IOException -> CredentialOfferError.NetworkInfoError
        else -> CredentialOfferError.Unexpected(this)
    }
