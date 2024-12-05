package ch.admin.foitt.wallet.platform.login.domain.usecase

import com.ramcosta.composedestinations.spec.Direction

fun interface NavigateToLogin {
    suspend operator fun invoke(): Direction
}
