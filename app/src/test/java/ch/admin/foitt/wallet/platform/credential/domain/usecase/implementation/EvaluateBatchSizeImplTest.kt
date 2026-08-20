package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.BatchCredentialIssuance
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EvaluateBatchSizeImplTest {

    private val useCase: EvaluateBatchSize = EvaluateBatchSizeImpl()

    @Test
    fun `Returns an error when batch issuance info is missing`() {
        val issuerCredentialInfo = mockk<IssuerCredentialInfo> {
            every { batchCredentialIssuance } returns null
        }

        val error = useCase(issuerCredentialInfo).assertErr()

        assertEquals(CredentialError.InvalidIssuerCredentialInfo, error)
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 9])
    fun `Returns an error when batch size is below the minimum`(batchSize: Int) {
        val issuerCredentialInfo = createIssuerCredentialInfo(batchSize = batchSize)

        val error = useCase(issuerCredentialInfo).assertErr()

        assertEquals(CredentialError.InvalidIssuerCredentialInfo, error)
    }

    @ParameterizedTest
    @ValueSource(ints = [10, 11, 50, 99, 100])
    fun `Returns batch size when it is within allowed bounds`(batchSize: Int) {
        val issuerCredentialInfo = createIssuerCredentialInfo(batchSize = batchSize)

        val result = useCase(issuerCredentialInfo).assertOk()

        assertEquals(batchSize, result)
    }

    @ParameterizedTest
    @ValueSource(ints = [101, 500, 1000, Int.MAX_VALUE])
    fun `Caps batch size to the maximum when it exceeds it`(batchSize: Int) {
        val issuerCredentialInfo = createIssuerCredentialInfo(batchSize = batchSize)

        val result = useCase(issuerCredentialInfo).assertOk()

        assertEquals(100, result)
    }

    private fun createIssuerCredentialInfo(batchSize: BatchSize) = mockk<IssuerCredentialInfo> {
        every { batchCredentialIssuance } returns BatchCredentialIssuance(batchSize = batchSize)
    }
}
