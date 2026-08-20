package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import com.github.michaelbull.result.Result
import uniffi.heidi_dcql_rust.DcqlQuery

fun interface GetCompatibleCredentials {
    @CheckResult
    suspend operator fun invoke(
        dcqlQuery: DcqlQuery
    ): Result<Set<CompatibleCredential>, GetCompatibleCredentialsError>
}
