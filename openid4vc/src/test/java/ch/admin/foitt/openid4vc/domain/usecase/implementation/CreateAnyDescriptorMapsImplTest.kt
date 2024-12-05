package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Constraints
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtDescriptorMap
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateAnyDescriptorMapsImplTest {

    @MockK
    private lateinit var mockVcSdJwtDescriptorMap: CreateVcSdJwtDescriptorMap

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    @MockK
    private lateinit var mockDescriptorMap: DescriptorMap

    @MockK
    private lateinit var mockDescriptorMap2: DescriptorMap

    private lateinit var useCase: CreateAnyDescriptorMapsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateAnyDescriptorMapsImpl(mockVcSdJwtDescriptorMap)

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Creating any descriptor maps for multiple vc+sd_jwt input descriptors returns the descriptor maps`() = runTest {
        val result = useCase(mockPresentationRequest)

        assertEquals(2, result.size)
        assertEquals(mockDescriptorMap, result[0])
        assertEquals(mockDescriptorMap2, result[1])
    }

    @Test
    fun `Creating any descriptor maps for no input descriptors returns an empty list`() = runTest {
        every { mockPresentationRequest.presentationDefinition.inputDescriptors } returns emptyList()

        val result = useCase(mockPresentationRequest)

        assertTrue(result.isEmpty())
    }

    private fun success() {
        every {
            mockPresentationRequest.presentationDefinition.inputDescriptors
        } returns listOf(vcSdJwtInputDescriptor, vcSdJwtInputDescriptor2)

        coEvery { mockVcSdJwtDescriptorMap(vcSdJwtInputDescriptor, 0) } returns mockDescriptorMap
        coEvery { mockVcSdJwtDescriptorMap(vcSdJwtInputDescriptor2, 0) } returns mockDescriptorMap2
    }

    private companion object {
        val supportedAlgorithms = listOf(SigningAlgorithm.ES512)
        val vcSdJwtInputDescriptor = InputDescriptor(
            constraints = Constraints(listOf()),
            formats = listOf(
                InputDescriptorFormat.VcSdJwt(
                    sdJwtAlgorithms = supportedAlgorithms,
                    kbJwtAlgorithms = emptyList(),
                )
            ),
            id = "inputDescriptor",
            name = "name",
            purpose = "purpose",
        )
        val vcSdJwtInputDescriptor2 = InputDescriptor(
            constraints = Constraints(listOf()),
            formats = listOf(
                InputDescriptorFormat.VcSdJwt(
                    sdJwtAlgorithms = supportedAlgorithms,
                    kbJwtAlgorithms = emptyList(),
                )
            ),
            id = "inputDescriptor2",
            name = "name",
            purpose = "purpose",
        )
    }
}
