package ch.admin.foitt.wallet.platform.utils

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CredentialClaimWithDisplaysListExtTest {

    private val claim1 = createClaimWithDisplays(1)
    private val claim2 = createClaimWithDisplays(2)

    private val claimUnordered1 = createClaimWithDisplays(-1)
    private val claimUnordered2 = createClaimWithDisplays(-1)

    @Test
    fun `Claims that are ordered correctly will be returned in the correct order`() = runTest {
        val list = listOf(claim1, claim2)

        val result = list.sortByOrder()
        val expected = listOf(claim1, claim2)
        assertEquals(expected, result)
    }

    @Test
    fun `Claims that are ordered incorrectly will be returned in the correct order`() = runTest {
        val list = listOf(claim2, claim1)

        val result = list.sortByOrder()
        val expected = listOf(claim1, claim2)
        assertEquals(expected, result)
    }

    @Test
    fun `Claims that are unordered will be placed at the end of the list`() = runTest {
        val list = listOf(claimUnordered1, claim1, claim2)

        val result = list.sortByOrder()
        val expected = listOf(claim1, claim2, claimUnordered1.toCredentialClaimWithMaxIntOrder())
        assertEquals(expected, result)
    }

    @Test
    fun `Unordered Claims are returned in the same order`() = runTest {
        val list = listOf(claimUnordered1, claimUnordered2)

        val result = list.sortByOrder()
        val expected =
            listOf(claimUnordered1.toCredentialClaimWithMaxIntOrder(), claimUnordered2.toCredentialClaimWithMaxIntOrder())
        assertEquals(expected, result)
    }

    private fun createClaimWithDisplays(order: Int) = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            credentialId = CREDENTIAL_ID,
            key = KEY,
            value = VALUE,
            valueType = null,
            order = order,
        ),
        displays = emptyList()
    )

    private fun CredentialClaimWithDisplays.toCredentialClaimWithMaxIntOrder() = CredentialClaimWithDisplays(
        claim = this.claim.copy(order = Int.MAX_VALUE),
        displays = this.displays
    )

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val KEY = "key"
        const val VALUE = "value"
    }
}
