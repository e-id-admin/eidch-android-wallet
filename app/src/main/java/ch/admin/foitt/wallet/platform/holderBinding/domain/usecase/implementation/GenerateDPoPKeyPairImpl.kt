package ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.GenerateDPoPKeyPairError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestKeyAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.toGenerateDPoPKeyPairError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestKeyAttestation
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.toGenerateDPoPKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInSoftware
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GenerateDPoPKeyPairImpl @Inject constructor(
    private val createJWSKeyPairInSoftware: CreateJWSKeyPairInSoftware,
    private val requestKeyAttestation: RequestKeyAttestation,
) : GenerateDPoPKeyPair {
    override suspend fun invoke(
        verifiableCredentialParams: VerifiableCredentialParams,
        actorDid: String,
    ): Result<BindingKeyPair?, GenerateDPoPKeyPairError> = coroutineBinding {
        val supportedAlgorithms = verifiableCredentialParams.dpopSigningAlgValuesSupported
        val isHardwareBound = verifiableCredentialParams.proofTypeConfig?.keyAttestationsRequired != null
        val algorithm = supportedAlgorithms?.firstOrNull { it == SigningAlgorithm.ES256 } ?: return@coroutineBinding null

        if (isHardwareBound) {
            val keyAttestation = requestKeyAttestation(
                actorDid = actorDid,
                keyAlias = null,
                signingAlgorithm = algorithm,
            ).mapError(RequestKeyAttestationError::toGenerateDPoPKeyPairError).bind()

            BindingKeyPair(
                keyPair = keyAttestation.keyPair,
                attestationJwt = keyAttestation.attestation,
            )
        } else {
            val keyPair = createJWSKeyPairInSoftware(algorithm)
                .mapError(CreateJWSKeyPairError::toGenerateDPoPKeyPairError)
                .bind()

            BindingKeyPair(
                keyPair = keyPair,
                attestationJwt = null,
            )
        }
    }
}
