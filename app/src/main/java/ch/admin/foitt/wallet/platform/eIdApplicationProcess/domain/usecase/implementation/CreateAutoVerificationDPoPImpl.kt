package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateDPoPProofJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.usecase.CreateDPoPProofJwt
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.openid4vc.utils.Constants
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CreateAutoVerificationDPoPError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toCreateAutoVerificationDPoPError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.CreateAutoVerificationDPoP
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

internal class CreateAutoVerificationDPoPImpl @Inject constructor(
    private val getHardwareKeyPair: GetHardwareKeyPair,
    private val createDPoPProofJwt: CreateDPoPProofJwt,
) : CreateAutoVerificationDPoP {
    override suspend operator fun invoke(
        url: URL,
        accessToken: String,
        requestBody: ByteArray,
    ): Result<String, CreateAutoVerificationDPoPError> = coroutineBinding {
        val keyPair = getHardwareKeyPair(
            keyId = ClientAttestation.KEY_ALIAS,
            provider = Constants.ANDROID_KEY_STORE,
        ).mapError(GetKeyPairError::toCreateAutoVerificationDPoPError).bind()

        val jwsKeyPair = JWSKeyPair(
            algorithm = ClientAttestation.SIGNING_ALGORITHM,
            keyPair = keyPair,
            keyId = ClientAttestation.KEY_ALIAS,
            bindingType = KeyBindingType.HARDWARE,
        )

        createDPoPProofJwt(
            method = "POST",
            url = url,
            keyPair = jwsKeyPair,
            nonce = null,
            accessToken = accessToken,
            requestBody = requestBody,
            keyAttestationJwt = null,
        ).mapError(CreateDPoPProofJwtError::toCreateAutoVerificationDPoPError).bind()
    }
}
