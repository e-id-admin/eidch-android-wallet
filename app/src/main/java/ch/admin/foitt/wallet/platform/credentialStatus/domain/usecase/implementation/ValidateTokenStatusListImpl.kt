package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListResponse
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.ValidateTokenStatusStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.toValidateTokenStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.toValidateTokenStatusStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ValidateTokenStatusList
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jwt.JWTClaimNames
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import javax.inject.Inject

class ValidateTokenStatusListImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val verifyJwtSignature: VerifyJwtSignature,
) : ValidateTokenStatusList {
    override suspend fun invoke(
        anyCredential: AnyCredential,
        statusListJwt: String,
        subject: String,
    ): Result<TokenStatusListResponse, ValidateTokenStatusStatusListError> = coroutineBinding {
        runSuspendCatching {
            val signedJwt = SignedJWT.parse(statusListJwt)
            val credentialJson = anyCredential.json

            check(signedJwt.header.type.type == SUPPORTED_STATUS_TYPE) { "Status list token is not of proper type" }
            checkNotNull(signedJwt.jwtClaimsSet.issueTime) { "Status list token iat claim is missing" }
            check(subject == signedJwt.jwtClaimsSet.subject) { "Subject does not match" }
            signedJwt.jwtClaimsSet.expirationTime?.let { expirationTime ->
                check(expirationTime.toInstant().isAfter(Instant.now())) { "Status list token is expired" }
            }

            val issuerDid: String = checkNotNull(signedJwt.jwtClaimsSet.issuer) { "Issuer is missing" }
            check(credentialJson.jsonObject[JWTClaimNames.ISSUER]?.jsonPrimitive?.content == issuerDid) { "Issuers does not match" }

            verifyJwtSignature(
                issuerDid = issuerDid,
                signedJwt = signedJwt,
            ).mapError(VerifyJwtError::toValidateTokenStatusListError)
                .bind()

            parseResponse(signedJwt.payload.toString()).bind()
        }.mapError(Throwable::toValidateTokenStatusStatusListError)
            .bind()
    }

    private fun parseResponse(payload: String) =
        safeJson.safeDecodeStringTo<TokenStatusListResponse>(string = payload)
            .mapError(JsonParsingError::toValidateTokenStatusListError)

    companion object {
        private const val SUPPORTED_STATUS_TYPE = "statuslist+jwt"
    }
}
