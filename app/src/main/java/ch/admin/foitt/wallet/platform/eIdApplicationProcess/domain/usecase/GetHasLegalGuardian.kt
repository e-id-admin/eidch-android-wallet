package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import kotlinx.coroutines.flow.StateFlow

fun interface GetHasLegalGuardian {
    operator fun invoke(): StateFlow<Boolean>
}
