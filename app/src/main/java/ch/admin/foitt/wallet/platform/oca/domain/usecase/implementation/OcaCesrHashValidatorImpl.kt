package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCesrHashAlgorithm
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCesrHashValidatorError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.toCesrHashValidatorError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCesrHashValidator
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import ch.admin.foitt.wallet.platform.utils.toBase64String
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toErrorIfNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import org.erdtman.jcs.JsonCanonicalizer
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Implementation of [OcaCesrHashValidator].
 *
 * Follows the Swiss E-ID OCA spec, documented [here](https://github.com/e-id-admin/open-source-community/blob/main/tech-roadmap/rfcs/oca/spec.md)
 */
class OcaCesrHashValidatorImpl @Inject constructor(
    private val safeJson: SafeJson,
) : OcaCesrHashValidator {

    override suspend operator fun invoke(ocaObjectJson: String): Result<Unit, OcaCesrHashValidatorError> = coroutineBinding {
        val originalOCAJson: JsonObject = safeJson.safeDecodeStringTo<JsonObject>(ocaObjectJson)
            .mapError(JsonParsingError::toCesrHashValidatorError)
            .bind()

        val originalDigest = runSuspendCatching {
            originalOCAJson[DIGEST_KEY]?.jsonPrimitive?.content
        }.mapError {
            OcaError.InvalidCESRHash("$LOG_PREFIX is missing digest key")
        }.toErrorIfNull {
            OcaError.InvalidCESRHash("$LOG_PREFIX is missing digest key")
        }.bind()

        val digestAlgorithm = OcaCesrHashAlgorithm.fromDigest(originalDigest)
            ?: Err(OcaError.InvalidCESRHash("$LOG_PREFIX unknown digest algorithm")).bind()

        val dummyDigestOCAJsonMap = originalOCAJson.toMutableMap().apply {
            put(DIGEST_KEY, JsonPrimitive(digestAlgorithm.dummyDigest))
        }
        val dummyDigestOCAJsonString = JsonObject(dummyDigestOCAJsonMap).toString()

        // JSON Canonicalization Scheme following [RFC8785](https://datatracker.ietf.org/doc/html/rfc8785)
        val canonicalJsonBytes = runSuspendCatching {
            JsonCanonicalizer(dummyDigestOCAJsonString).encodedUTF8
        }.mapError { error ->
            Timber.e(t = error, message = "$LOG_PREFIX json canonicalization for CESR failed")
            OcaError.Unexpected(error)
        }.bind()

        val rawDigest = runSuspendCatching {
            MessageDigest.getInstance(digestAlgorithm.name).digest(canonicalJsonBytes)
        }.mapError { error ->
            Timber.e(t = error, message = "$LOG_PREFIX digest computation failed")
            OcaError.Unexpected(error)
        }.bind()

        val rawDigestWithPadding = ByteArray(digestAlgorithm.paddingSize) + rawDigest
        val base64EncodedSha256Without1stChar = rawDigestWithPadding.toBase64String().drop(CHAR_TO_DROP)
        val computedDigest = "${digestAlgorithm.prefix}$base64EncodedSha256Without1stChar"

        if (originalDigest == computedDigest) {
            Ok(Unit)
        } else {
            Err(OcaError.InvalidCESRHash("$LOG_PREFIX computed digest mismatch"))
        }.bind()
    }

    private companion object {
        const val DIGEST_KEY = "digest"
        const val CHAR_TO_DROP = 1
        const val LOG_PREFIX = "OCA object "
    }
}
