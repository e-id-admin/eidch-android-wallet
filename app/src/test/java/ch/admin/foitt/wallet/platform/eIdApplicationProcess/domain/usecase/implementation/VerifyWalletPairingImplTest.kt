package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairedWallets
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.TargetWallets
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class VerifyWalletPairingImplTest {

    private val repository: EIdRequestCaseRepository = mockk()
    private val useCase = VerifyWalletPairingImpl(repository)

    @Test
    fun `invoke returns Ok when all remote pairings are local`() = runTest {
        val caseId = "case1"
        val pairingId1 = "pair1"
        val pairingId2 = "pair2"

        coEvery { repository.getPairingIds(caseId) } returns Ok(listOf(pairingId1, pairingId2))

        val remoteState = StateResponse(
            state = EIdRequestQueueState.IN_AUTO_VERIFICATION,
            queueInformation = null,
            legalRepresentant = null,
            onlineSessionStartTimeout = null,
            targetWallets = TargetWallets(
                limitReached = false,
                pairedWallets = listOf(
                    PairedWallets(pairingId1, "2023-01-01T00:00:00Z", null),
                    PairedWallets(pairingId2, "2023-01-01T00:01:00Z", null)
                )
            )
        )

        val result = useCase(caseId, remoteState)

        result.assertOk()
    }

    @Test
    fun `invoke returns Ok when some local pairings are not remote (local extras)`() = runTest {
        val caseId = "case1"
        val pairingId1 = "pair1"
        val localExtraId = "extra"

        coEvery { repository.getPairingIds(caseId) } returns Ok(listOf(pairingId1, localExtraId))

        val remoteState = StateResponse(
            state = EIdRequestQueueState.IN_AUTO_VERIFICATION,
            queueInformation = null,
            legalRepresentant = null,
            onlineSessionStartTimeout = null,
            targetWallets = TargetWallets(
                limitReached = false,
                pairedWallets = listOf(
                    PairedWallets(pairingId1, "2023-01-01T00:00:00Z", null)
                )
            )
        )

        val result = useCase(caseId, remoteState)

        result.assertOk()
    }

    @Test
    fun `invoke returns UnauthorizedPairing when a remote pairing is not local`() = runTest {
        val caseId = "case1"
        val pairingId1 = "pair1"
        val unauthorizedPairingId = "unauthorized"

        coEvery { repository.getPairingIds(caseId) } returns Ok(listOf(pairingId1))

        val remoteState = StateResponse(
            state = EIdRequestQueueState.IN_AUTO_VERIFICATION,
            queueInformation = null,
            legalRepresentant = null,
            onlineSessionStartTimeout = null,
            targetWallets = TargetWallets(
                limitReached = false,
                pairedWallets = listOf(
                    PairedWallets(pairingId1, "2023-01-01T00:00:00Z", null),
                    PairedWallets(unauthorizedPairingId, "2023-01-01T00:01:00Z", null)
                )
            )
        )

        useCase(caseId, remoteState).assertErrorType(EIdRequestError.UnauthorizedPairing::class)
    }

    @Test
    fun `invoke returns Ok when remote targetWallets is null and local is empty`() = runTest {
        val caseId = "case1"
        coEvery { repository.getPairingIds(caseId) } returns Ok(emptyList())

        val remoteState = StateResponse(
            state = EIdRequestQueueState.IN_AUTO_VERIFICATION,
            queueInformation = null,
            legalRepresentant = null,
            onlineSessionStartTimeout = null,
            targetWallets = null
        )

        val result = useCase(caseId, remoteState)

        result.assertOk()
    }

    @Test
    fun `invoke returns Unexpected when state is not IN_AUTO_VERIFICATION`() = runTest {
        val caseId = "case1"
        val remoteState = StateResponse(
            state = EIdRequestQueueState.READY_FOR_ONLINE_SESSION,
            queueInformation = null,
            legalRepresentant = null,
            onlineSessionStartTimeout = null,
            targetWallets = null
        )

        useCase(caseId, remoteState).assertErrorType(EIdRequestError.Unexpected::class)
    }
}
