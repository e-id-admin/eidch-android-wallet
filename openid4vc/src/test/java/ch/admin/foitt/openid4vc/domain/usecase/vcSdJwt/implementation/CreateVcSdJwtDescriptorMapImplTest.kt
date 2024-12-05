package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Constraints
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateVcSdJwtDescriptorMapImplTest {

    private lateinit var useCase: CreateVcSdJwtDescriptorMapImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateVcSdJwtDescriptorMapImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Creating VcSdJwt descriptor map returns descriptor map`() = runTest {
        val result = useCase(inputDescriptor, CREDENTIAL_INDEX)

        val expected = DescriptorMap(
            format = CredentialFormat.VC_SD_JWT.format,
            id = INPUT_DESCRIPTOR_ID,
            path = "$",
        )
        assertEquals(expected, result)
    }

    private companion object {
        const val INPUT_DESCRIPTOR_ID = "inputDescriptorId"
        const val CREDENTIAL_INDEX = 1

        val supportedAlgorithms = listOf(SigningAlgorithm.ES512)
        val inputDescriptor = InputDescriptor(
            constraints = Constraints(emptyList()),
            formats = listOf(
                InputDescriptorFormat.VcSdJwt(
                    sdJwtAlgorithms = supportedAlgorithms,
                    kbJwtAlgorithms = emptyList(),
                )
            ),
            id = INPUT_DESCRIPTOR_ID,
            name = "name",
            purpose = "purpose",
        )
    }
}
