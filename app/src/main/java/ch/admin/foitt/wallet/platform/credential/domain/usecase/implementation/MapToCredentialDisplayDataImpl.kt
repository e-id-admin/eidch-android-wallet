package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.model.getDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.toMapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.IsBetaIssuer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class MapToCredentialDisplayDataImpl @Inject constructor(
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val isBetaIssuer: IsBetaIssuer,
) : MapToCredentialDisplayData {
    override suspend fun invoke(
        credential: Credential,
        credentialDisplays: List<CredentialDisplay>,
    ): Result<CredentialDisplayData, MapToCredentialDisplayDataError> = coroutineBinding {
        val anyCredential = credential.toAnyCredential()
            .mapError(AnyCredentialError::toMapToCredentialDisplayDataError)
            .bind()

        val credentialDisplay = getDisplay(credentialDisplays).bind()

        CredentialDisplayData(
            credentialId = credential.id,
            status = anyCredential.getDisplayStatus(credential.status),
            credentialDisplay = credentialDisplay,
            isCredentialFromBetaIssuer = isBetaIssuer(anyCredential.issuer)
        )
    }

    private fun <T : LocalizedDisplay> getDisplay(displays: List<T>): Result<T, MapToCredentialDisplayDataError> =
        getLocalizedDisplay(displays)?.let { Ok(it) }
            ?: Err(CredentialError.Unexpected(IllegalStateException("No localized display found")))
}
