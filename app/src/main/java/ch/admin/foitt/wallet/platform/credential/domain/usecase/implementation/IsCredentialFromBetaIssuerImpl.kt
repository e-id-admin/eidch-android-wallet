package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.IsCredentialFromBetaIssuer
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOr
import javax.inject.Inject

class IsCredentialFromBetaIssuerImpl @Inject constructor(
    private val getAnyCredential: GetAnyCredential,
) : IsCredentialFromBetaIssuer {
    private val regex = Regex(pattern = "^([^:]+:){3}identifier-reg(-.)?\\.trust-infra\\.swiyu-int\\.admin\\.ch:.*")

    override suspend fun invoke(credentialId: Long): Boolean = runSuspendCatching {
        val credential = getAnyCredential(credentialId)
        val payload = credential.value?.payload

        if (payload.isNullOrEmpty()) {
            return false
        }

        val issuer = SdJwt(payload).issuer
        regex.containsMatchIn(issuer)
    }.getOr(false)
}
