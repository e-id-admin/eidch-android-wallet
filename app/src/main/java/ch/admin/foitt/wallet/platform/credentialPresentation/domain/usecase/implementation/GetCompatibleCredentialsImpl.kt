package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetRequestedFieldsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toGetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetRequestedFields
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetCompatibleCredentialsImpl @Inject constructor(
    private val getAnyCredentials: GetAnyCredentials,
    private val getRequestedFields: GetRequestedFields,
) : GetCompatibleCredentials {
    override suspend fun invoke(
        inputDescriptors: List<InputDescriptor>
    ): Result<List<CompatibleCredential>, GetCompatibleCredentialsError> =
        getAnyCredentials()
            .mapError(GetAnyCredentialsError::toGetCompatibleCredentialsError)
            .andThen { credentials ->
                findCompatibleCredentials(credentials, inputDescriptors)
            }

    private suspend fun findCompatibleCredentials(
        credentials: List<AnyCredential>,
        inputDescriptors: List<InputDescriptor>
    ): Result<List<CompatibleCredential>, GetCompatibleCredentialsError> = coroutineBinding {
        credentials.mapNotNull { credential ->
            runSuspendCatching {
                val inputDescriptor = inputDescriptors.first()
                val compatibleFormat = getCompatibleFormat(credential.format, inputDescriptor.formats) ?: return@mapNotNull null
                if (isProofTypeCompatible(compatibleFormat, credential).not()) {
                    return@mapNotNull null
                }
                val fields = getRequestedFields(credential.json.toString(), inputDescriptors)
                    .mapError(GetRequestedFieldsError::toGetCompatibleCredentialsError)
                    .bind()
                if (fields.isNotEmpty()) {
                    val id = requireNotNull(credential.id)
                    CompatibleCredential(id, fields)
                } else {
                    null
                }
            }.mapError(Throwable::toGetCompatibleCredentialsError)
                .bind()
        }
    }

    private fun getCompatibleFormat(credentialFormat: CredentialFormat, inputDescriptorFormats: List<InputDescriptorFormat>) =
        inputDescriptorFormats.find { it.name == credentialFormat.format }

    private fun isProofTypeCompatible(
        compatibleFormat: InputDescriptorFormat,
        credential: AnyCredential
    ) = when (compatibleFormat) {
        is InputDescriptorFormat.VcSdJwt ->
            credential.keyBindingAlgorithm?.let {
                compatibleFormat.kbJwtAlgorithms?.contains(it) == true
            } ?: compatibleFormat.kbJwtAlgorithms.isNullOrEmpty()
    }
}
