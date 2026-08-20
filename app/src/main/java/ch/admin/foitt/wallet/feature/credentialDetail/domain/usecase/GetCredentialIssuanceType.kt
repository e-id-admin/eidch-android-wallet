package ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.GetCredentialIssuanceTypeError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuanceType
import com.github.michaelbull.result.Result

interface GetCredentialIssuanceType {
    suspend operator fun invoke(credentialId: Long): Result<IssuanceType, GetCredentialIssuanceTypeError>
}
