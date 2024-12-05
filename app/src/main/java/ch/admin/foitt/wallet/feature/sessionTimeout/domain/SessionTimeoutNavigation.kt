package ch.admin.foitt.wallet.feature.sessionTimeout.domain

import com.ramcosta.composedestinations.spec.Direction

interface SessionTimeoutNavigation {
    suspend operator fun invoke(): Direction?
}
