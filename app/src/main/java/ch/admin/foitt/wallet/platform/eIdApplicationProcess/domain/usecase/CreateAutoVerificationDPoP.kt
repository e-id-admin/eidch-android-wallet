package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CreateAutoVerificationDPoPError
import com.github.michaelbull.result.Result
import java.net.URL

fun interface CreateAutoVerificationDPoP {
    suspend operator fun invoke(
        url: URL,
        accessToken: String,
        requestBody: ByteArray,
    ): Result<String, CreateAutoVerificationDPoPError>
}
