package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

fun interface SetHasLegalGuardian {
    operator fun invoke(
        hasLegalGuardian: Boolean,
    )
}
