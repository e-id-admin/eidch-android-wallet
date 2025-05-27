package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

fun interface SetCurrentSIdCaseId {
    operator fun invoke(caseId: String)
}
