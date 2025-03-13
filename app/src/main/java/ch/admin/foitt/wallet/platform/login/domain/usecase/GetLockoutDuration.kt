package ch.admin.foitt.wallet.platform.login.domain.usecase

import java.time.Duration

fun interface GetLockoutDuration {
    operator fun invoke(): Duration
}
