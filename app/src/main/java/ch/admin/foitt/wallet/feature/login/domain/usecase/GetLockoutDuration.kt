package ch.admin.foitt.wallet.feature.login.domain.usecase

import java.time.Duration

fun interface GetLockoutDuration {
    operator fun invoke(): Duration
}
