package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.CANCELLED
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.IN_QUEUING
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.READY_FOR_ONLINE_SESSION
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState.TIMEOUT
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent.NOT_REQUIRED
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent.NOT_VERIFIED
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent.VERIFIED

enum class SIdRequestDisplayStatus {
    AV_READY,
    AV_READY_LEGAL_CONSENT_OK,
    AV_READY_LEGAL_CONSENT_PENDING,
    QUEUEING,
    QUEUEING_LEGAL_CONSENT_OK,
    QUEUEING_LEGAL_CONSENT_PENDING,
    AV_EXPIRED,
    AV_EXPIRED_LEGAL_CONSENT_OK,
    AV_EXPIRED_LEGAL_CONSENT_PENDING,
    UNKNOWN,
    OTHER,
}

fun StateResponse.toSIdRequestDisplayStatus(): SIdRequestDisplayStatus =
    Pair(state, toLegalRepresentativeConsent()).toSIdRequestDisplayStatus()

fun EIdRequestState.toSIdRequestDisplayStatus(): SIdRequestDisplayStatus =
    Pair(state, legalRepresentativeConsent).toSIdRequestDisplayStatus()

private fun Pair<EIdRequestQueueState, LegalRepresentativeConsent>.toSIdRequestDisplayStatus(): SIdRequestDisplayStatus = when (this) {
    READY_FOR_ONLINE_SESSION to NOT_REQUIRED -> SIdRequestDisplayStatus.AV_READY
    READY_FOR_ONLINE_SESSION to VERIFIED -> SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_OK
    READY_FOR_ONLINE_SESSION to NOT_VERIFIED -> SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_PENDING

    IN_QUEUING to NOT_REQUIRED -> SIdRequestDisplayStatus.QUEUEING
    IN_QUEUING to VERIFIED -> SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_OK
    IN_QUEUING to NOT_VERIFIED -> SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_PENDING

    TIMEOUT to NOT_REQUIRED -> SIdRequestDisplayStatus.AV_EXPIRED
    TIMEOUT to VERIFIED -> SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_OK
    TIMEOUT to NOT_VERIFIED -> SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_PENDING

    CANCELLED to NOT_REQUIRED -> SIdRequestDisplayStatus.AV_EXPIRED
    CANCELLED to VERIFIED -> SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_OK
    CANCELLED to NOT_VERIFIED -> SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_PENDING

    else -> SIdRequestDisplayStatus.OTHER
}
