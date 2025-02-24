package ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import java.time.Instant

object EIdRequestMocks {
    private const val CASE_ID = "caseId"
    private const val RAW_MRZ = "rawMrz"
    private const val DOCUMENT_NUMBER = "documentNumber"
    private const val FIRST_NAME = "firstName"
    private const val LAST_NAME = "lastName"


    val eIdRequestCase = EIdRequestCase(
        id = CASE_ID,
        rawMrz = RAW_MRZ,
        documentNumber = DOCUMENT_NUMBER,
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
    )

    val eIdRequestState = EIdRequestState(
        id = 1L,
        eIdRequestCaseId = CASE_ID,
        state = EIdRequestQueueState.BereitFuerOnlineSession,
        lastPolled = Instant.now().epochSecond,
        onlineSessionStartOpenAt = Instant.now().epochSecond,
        onlineSessionStartTimeoutAt = Instant.now().epochSecond,
    )
}
