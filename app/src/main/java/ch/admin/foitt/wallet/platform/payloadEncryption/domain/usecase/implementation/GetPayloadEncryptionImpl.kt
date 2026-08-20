package ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.CreatePayloadEncryptionKeyPairError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.toGetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.CreatePayloadEncryptionKeyPair
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetPayloadEncryptionImpl @Inject constructor(
    private val createPayloadEncryptionKeyPair: CreatePayloadEncryptionKeyPair,
) : GetPayloadEncryption {
    override suspend fun invoke(
        requestEncryption: CredentialRequestEncryption,
        responseEncryption: CredentialResponseEncryption
    ): Result<PayloadEncryption, GetPayloadEncryptionTypeError> = coroutineBinding {
        val keyPair = createPayloadEncryptionKeyPair(responseEncryption)
            .mapError(CreatePayloadEncryptionKeyPairError::toGetPayloadEncryptionTypeError)
            .bind()

        PayloadEncryption(
            requestEncryption = requestEncryption,
            responseEncryption = responseEncryption,
            responseEncryptionKeyPair = keyPair
        )
    }
}
