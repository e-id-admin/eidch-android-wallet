package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.SaveCredentialFromDeferredError
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithAuthenticationAndKeyBinding
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import com.github.michaelbull.result.Result

internal fun interface SaveCredentialFromDeferred {
    suspend operator fun invoke(
        deferredCredentialEntity: DeferredCredentialWithAuthenticationAndKeyBinding,
        credentialResponse: CredentialResponse.VerifiableCredential,
        rawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo,
        identityV2TrustStatement: IdentityV2TrustStatement?,
    ): Result<Long, SaveCredentialFromDeferredError>
}
