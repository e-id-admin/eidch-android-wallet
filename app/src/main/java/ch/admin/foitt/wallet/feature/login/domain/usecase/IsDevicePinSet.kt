package ch.admin.foitt.wallet.feature.login.domain.usecase

import androidx.annotation.CheckResult

fun interface IsDevicePinSet {
    @CheckResult
    operator fun invoke(): Boolean
}
