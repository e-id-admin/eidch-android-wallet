package ch.admin.foitt.wallet.platform.credentialPresentation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientIdentifier
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.VerifierInfo
import uniffi.heidi_dcql_rust.CredentialQuery
import uniffi.heidi_dcql_rust.DcqlQuery

object MockPresentationRequest {

    const val VERIFIER_ATTESTATION_CLIENT_ID = "test app"
    const val CLIENT_ID = "did:example:12345"
    const val CLIENT_ID_WITH_PREFIX = "decentralized_identifier:$CLIENT_ID"
    const val VALID_JWT =
        "eyJraWQiOiJkaWQ6ZXhhbXBsZToxMjM0NSNrZXktMDEiLCJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWF1dGh6LXJlcStqd3QifQ.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwiYXVkIjoiZGlkOmV4YW1wbGU6MTIzNDUiLCJpc3MiOiJkaWQ6ZXhhbXBsZToxMjM0NSIsInJlc3BvbnNlX3R5cGUiOiJ2cF90b2tlbiIsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlkIjoiM2ZhODVmNjQtMDAwMC0wMDAwLWIzZmMtMmM5NjNmNjZhZmE2IiwibmFtZSI6InN0cmluZyIsInB1cnBvc2UiOiJzdHJpbmciLCJpbnB1dF9kZXNjcmlwdG9ycyI6W3siaWQiOiIzZmE4NWY2NC01NzE3LTQ1NjItYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoiQSBuYW1lIiwiZm9ybWF0Ijp7InZjK3NkLWp3dCI6eyJzZC1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdLCJrYi1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdfX0sImNvbnN0cmFpbnRzIjp7ImZpZWxkcyI6W3sicGF0aCI6WyIkLmxhc3ROYW1lIl19XX19XX0sIm5vbmNlIjoiSTAyRmliTEY0azVFc2ZETzJqZ2pEb29QNEEvWnVrUTMiLCJjbGllbnRfaWQiOiJkZWNlbnRyYWxpemVkX2lkZW50aWZpZXI6ZGlkOmV4YW1wbGU6MTIzNDUiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCIsInN0YXRlIjoic3RhdGUifQ.5-3bMiZSxToZHrP8rsQPmSQPsJ5j4aOLrl6WdvBHan5I2JimhoIoU-kHTg0zDMXATosDSOdTYUTY0xlxv1RGNA"

    const val VALID_VQPS =
        "eyJ0eXAiOiJzd2l5dS12ZXJpZmljYXRpb24tcXVlcnktcHVibGljLXN0YXRlbWVudCtqd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDpleGFtcGxlOnZlcmlmaWNhdGlvbi1zdGF0bWVudC1pc3N1ZXIja2V5LTEiLCJwcm9maWxlX3ZlcnNpb24iOiJzd2lzcy1wcm9maWxlLXRydXN0OjEuMC4wIn0.eyJqdGkiOiIwN2YyODlkNS04YjFmLTQ2MDQtYmY3Mi01M2JkY2I3MWVlMDUiLCJzdWIiOiJkaWQ6ZXhhbXBsZTp2ZXJpZmllciIsImlhdCI6MTY5MDM2MDk2OCwiZXhwIjoxNzUzNDMyOTY4LCJwdXJwb3NlX25hbWUiOiJiZWlzcGllbCBhYmZyYWdlIiwicHVycG9zZV9uYW1lI2RlLWNoIjoiYmVpc3BpZWwgYWJmcmFnZSIsInB1cnBvc2VfZGVzY3JpcHRpb24iOiJmcmFnZSBhYiB6dW0gYmVpc3BpZWwiLCJwdXJwb3NlX2Rlc2NyaXB0aW9uI2RlLWNoIjoiZnJhZ2UgYWIgenVtIGJlaXNwaWVsIiwicmVxdWVzdCI6eyJ0eXBlIjoiRENRTCIsInNjb3BlIjoiY29tLmV4YW1wbGUuY3JlZGVudGlhbF9wcmVzZW50YXRpb24iLCJxdWVyeSI6eyJjcmVkZW50aWFscyI6W3siaWQiOiJwaWQiLCJmb3JtYXQiOiJ2YytzZC1qd3QiLCJtZXRhIjp7InZjdF92YWx1ZXMiOlsidmNTY2hlbWFJZCJdfSwiY2xhaW1zIjpbeyJwYXRoIjpbImZpcnN0TmFtZSJdfSx7InBhdGgiOlsibGFzdE5hbWUiXX0seyJwYXRoIjpbImRhdGVPZkJpcnRoIl19LHsicGF0aCI6WyJob21ldG93biJdfSx7InBhdGgiOlsiY2F0ZWdvcnlDb2RlIl19XSwicmVxdWlyZV9jcnlwdG9ncmFwaGljX2hvbGRlcl9iaW5kaW5nIjpmYWxzZX1dLCJjcmVkZW50aWFsX3NldHMiOlt7Im9wdGlvbnMiOltbInBpZCJdXX1dfX19.yT_Uxh8iAvGhC7z7SLGkHrwM0eTH8D0QgeFdJkG8-ZNtKFIsZ9FiOZUBMENBgxpghIJA9X89JtXDXQgPCUDLbw"

    const val VALID_IDTS =
        "eyJ0eXAiOiJzd2l5dS1pZGVudGl0eS10cnVzdC1zdGF0ZW1lbnQrand0IiwiYWxnIjoiRVMyNTYiLCJraWQiOiJkaWQ6ZXhhbXBsZTp0cnVzdC1pc3N1ZXIja2V5LTEiLCJwcm9maWxlX3ZlcnNpb24iOiJzd2lzcy1wcm9maWxlLXRydXN0OjEuMC4wIn0.eyJzdWIiOiJkaWQ6ZXhhbXBsZTphY3RvciIsImp0aSI6IjA3ZjI4OWQ1LThiMWYtNDYwNC1iZjcyLTUzYmRjYjcxZWUwNSIsImlhdCI6MTY5MDM2MDk2OCwiZXhwIjoxNzUzNDMyOTY4LCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsiaWR4IjowLCJ1cmkiOiJodHRwczovL2V4YW1wbGUuY29tL3N0YXR1c2xpc3RzLzEifX0sImVudGl0eV9uYW1lIjoiZW50aXR5TmFtZSIsImVudGl0eV9uYW1lI2RlLUNIIjoiZW50aXR5TmFtZSBkZS1DSCIsImVudGl0eV9uYW1lI2VuLVVTIjoiZW50aXR5TmFtZSBlbi1VUyIsImlzX3N0YXRlX2FjdG9yIjpmYWxzZSwicmVnaXN0cnlfaWRzIjpbeyJ0eXBlIjoidHlwZSIsInZhbHVlIjoidmFsdWUifV19.RtmKqjvL6ZZwT5HiAnkzPQftcQR-IVS0pId0VVSYFMJRxb7jEGP3Fn55e8YSMBg0IwAiKWFNHE-aQMSSNfZJTg"

    val jwk = Jwk(
        x = "x",
        y = "y",
        crv = "curve",
        kty = "kty"
    )
    val authorizationRequest = AuthorizationRequest(
        nonce = "nonce",
        responseUri = "response_uri",
        responseMode = "direct_post.jwt",
        clientIdentifier = ClientIdentifier(
            clientIdPrefix = ClientIdentifier.ClientIdPrefix.DecentralizedIdentifier,
            clientId = CLIENT_ID,
            raw = CLIENT_ID_WITH_PREFIX,
        ),
        responseType = "vp_token",
        clientMetaData = ClientMetaData(
            clientNameList = emptyList(),
            logoUriList = emptyList(),
            jwks = Jwks(listOf(jwk)),
        ),
        dcqlQuery = null,
        state = null,
        expectedOrigins = emptyList(),
        verifierInfo = listOf(
            VerifierInfo("jwt", Jwt(VALID_IDTS)),
            VerifierInfo("jwt", Jwt(VALID_VQPS))
        ),
        scope = "scope"
    )

    val authorizationRequestWithoutIdTS = authorizationRequest.copy(
        verifierInfo = listOf(VerifierInfo("jwt", Jwt(VALID_VQPS))),
    )

    val authorizationRequestWithoutVqPS = authorizationRequest.copy(
        verifierInfo = listOf(VerifierInfo("jwt", Jwt(VALID_IDTS))),
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.DC_SD_JWT.format,
                    requireCryptographicHolderBinding = true,
                )
            )
        ),
        scope = null,
    )
    val authorizationRequestDcApi = authorizationRequestWithoutVqPS.copy(
        verifierInfo = listOf(),
        clientIdentifier = ClientIdentifier(
            clientIdPrefix = ClientIdentifier.ClientIdPrefix.VerifierAttestationJwt,
            clientId = VERIFIER_ATTESTATION_CLIENT_ID,
            raw = "verifier_attestation:$VERIFIER_ATTESTATION_CLIENT_ID"
        ),
        responseMode = "dc_api.jwt"
    )

    val authorizationRequestWithState = authorizationRequestWithoutVqPS.copy(
        state = "state",
    )

    val authorizationRequestWithStateAndNoHolderBinding = authorizationRequestWithoutVqPS.copy(
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.DC_SD_JWT.format,
                    requireCryptographicHolderBinding = false,
                )
            )
        ),
        state = "state",
    )

    fun invalidPresentationRequestClaims() = authorizationRequestWithoutVqPS.copy(
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.DC_SD_JWT.format,
                    claims = emptyList(),
                )
            )
        )
    )

    fun invalidPresentationRequestNoDCQL() = authorizationRequest.copy(
        dcqlQuery = null,
        verifierInfo = null,
        scope = null,
    )

    fun invalidPresentationRequestState() = authorizationRequestWithoutVqPS.copy(
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.DC_SD_JWT.format,
                    requireCryptographicHolderBinding = false,
                ),
                CredentialQuery(
                    id = "id2",
                    format = CredentialFormat.DC_SD_JWT.format,
                    requireCryptographicHolderBinding = true,
                ),
            )
        ),
    )

    val authorizationRequestWithDisplays = authorizationRequest.copy(
        clientMetaData = ClientMetaData(
            clientNameList = listOf(
                ClientName(
                    clientName = "firstClientName",
                    locale = "en"
                ),
                ClientName(
                    clientName = "secondClientName",
                    locale = "fr"
                ),
                ClientName(
                    clientName = "clientName",
                    locale = "fallback"
                )
            ),
            logoUriList = listOf(
                LogoUri(
                    logoUri = "firstLogoUri",
                    locale = "en"
                ),
                LogoUri(
                    logoUri = "secondLogoUri",
                    locale = "de"
                ),
                LogoUri(
                    logoUri = "logoUri",
                    locale = "fallback"
                )
            ),
            jwks = Jwks(emptyList()),
        ),
    )
}
