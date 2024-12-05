package ch.admin.foitt.openid4vc.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofTypeSigningAlgorithms
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.CREDENTIAL_IDENTIFIER

object MockIssuerCredentialConfiguration {
    private const val VCT = "vct"
    private const val CLAIMS = "{}"
    private val SUPPORTED_CRYPTOGRAPHIC_SUITE = SigningAlgorithm.ES512
    const val DID_JWK_BINDING_METHOD = "did:jwk"
    private val SUPPORTED_PROOF_TYPE = ProofType.JWT
    private val UNKNOWN_PROOF_TYPE = ProofType.UNKNOWN
    private val SIGNING_ALG = SigningAlgorithm.ES256
    private val PROOF_SIGNING_ALG_VALUES_SUPPORTED = listOf(SIGNING_ALG)
    const val JWK = """
    {
        "crv": "P-256",
        "kty": "EC",
        "x": "Q7HpY9d8GlvGqfHtw-9jLLPZaIX9Lc91Q-Hfsz_WbBo",
        "y": "647ttGFFCBoy17NspJszfIW2pEwuzqdep69Av5Mprb8"
    }
    """
    const val JWT_KID = "did:jwk:base64EncodedPublicKey"
    val vcSdJwtCredentialConfiguration = VcSdJwtCredentialConfiguration(
        identifier = CREDENTIAL_IDENTIFIER,
        claims = CLAIMS,
        credentialSigningAlgValuesSupported = listOf(SUPPORTED_CRYPTOGRAPHIC_SUITE),
        cryptographicBindingMethodsSupported = listOf(DID_JWK_BINDING_METHOD),
        format = CredentialFormat.VC_SD_JWT,
        proofTypesSupported = mapOf(SUPPORTED_PROOF_TYPE to ProofTypeSigningAlgorithms(PROOF_SIGNING_ALG_VALUES_SUPPORTED)),
        vct = VCT,
    )
    val credentialConfigurationWithMultipleCryptographicSuites = vcSdJwtCredentialConfiguration.copy(
        credentialSigningAlgValuesSupported = listOf(
            SUPPORTED_CRYPTOGRAPHIC_SUITE,
            SigningAlgorithm.ES512,
        ),
    )
    val credentialConfigurationWithoutProofTypesSupported = vcSdJwtCredentialConfiguration.copy(
        proofTypesSupported = emptyMap(),
    )
    val credentialConfigurationWithOtherProofTypeSigningAlgorithms = vcSdJwtCredentialConfiguration.copy(
        proofTypesSupported = mapOf(UNKNOWN_PROOF_TYPE to ProofTypeSigningAlgorithms(PROOF_SIGNING_ALG_VALUES_SUPPORTED))
    )
}
