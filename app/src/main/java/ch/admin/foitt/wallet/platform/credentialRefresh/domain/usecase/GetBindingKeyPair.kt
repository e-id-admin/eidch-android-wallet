package ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.GetBindingKeyPairError
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import com.github.michaelbull.result.Result

fun interface GetBindingKeyPair {
    suspend operator fun invoke(
        authentication: CredentialAuthenticationWithDpopBinding,
    ): Result<BindingKeyPair?, GetBindingKeyPairError>
}
