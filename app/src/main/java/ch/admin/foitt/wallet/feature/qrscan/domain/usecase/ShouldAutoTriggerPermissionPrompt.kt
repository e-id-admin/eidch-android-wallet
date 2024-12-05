package ch.admin.foitt.wallet.feature.qrscan.domain.usecase

import androidx.annotation.CheckResult

fun interface ShouldAutoTriggerPermissionPrompt {
    @CheckResult
    suspend operator fun invoke(): Boolean
}
