package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import com.github.michaelbull.result.Result

interface MapToCredentialDisplayData {
    suspend operator fun invoke(
        credential: Credential,
        credentialDisplays: List<CredentialDisplay>
    ): Result<CredentialDisplayData, MapToCredentialDisplayDataError>
}
