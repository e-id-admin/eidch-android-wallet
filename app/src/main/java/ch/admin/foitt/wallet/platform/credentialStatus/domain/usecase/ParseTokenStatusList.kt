package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.ParseTokenStatusStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusList
import com.github.michaelbull.result.Result

fun interface ParseTokenStatusList {
    suspend operator fun invoke(
        statusList: TokenStatusList,
        index: Int
    ): Result<Int, ParseTokenStatusStatusListError>
}
