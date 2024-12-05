package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.Claim
import ch.admin.foitt.wallet.platform.database.domain.model.ClaimDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toMapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.utils.base64NonUrlStringToByteArray
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class MapToCredentialClaimDataImpl @Inject constructor(
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : MapToCredentialClaimData {
    override suspend fun <T : Claim, U : ClaimDisplay> invoke(
        claim: T,
        displays: List<U>
    ): Result<CredentialClaimData, MapToCredentialClaimDataError> =
        runSuspendCatching {
            getLocalizedDisplay(displays)?.let { display ->
                when (claim.valueType) {
                    "bool", "string" -> CredentialClaimText(localizedKey = display.name, value = claim.value)
                    "image/png", "image/jpeg", "image/jp2" -> {
                        val byteArray = claim.value.base64NonUrlStringToByteArray()
                        CredentialClaimImage(localizedKey = display.name, imageData = byteArray)
                    }

                    else -> error("Unsupported value type '${claim.valueType}' found for claim '${claim.key}'")
                }
            } ?: error("No localized display found")
        }.mapError(Throwable::toMapToCredentialClaimDataError)
}
