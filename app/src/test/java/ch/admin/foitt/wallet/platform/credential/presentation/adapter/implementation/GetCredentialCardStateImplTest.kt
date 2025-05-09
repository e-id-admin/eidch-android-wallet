package ch.admin.foitt.wallet.platform.credential.presentation.adapter.implementation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.implementation.GetColorImpl
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCredentialCardStateImplTest {

    private lateinit var getCredentialCardState: GetCredentialCardState

    @MockK
    private lateinit var mockGetDrawableFromUri: GetDrawableFromUri

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(ColorUtils::class)

        val getColor = GetColorImpl()
        getColor(Color.Black.toString())

        getCredentialCardState = GetCredentialCardStateImpl(
            getColor = getColor,
            getDrawableFromUri = mockGetDrawableFromUri
        )

        coEvery { mockGetDrawableFromUri(any()) } returns null
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `If contrast of black and white are the same, use black text`() = runTest {
        every { ColorUtils.calculateContrast(any(), any()) } returns 1.0

        val credentialDisplayData: CredentialDisplayData = MockCredentialDetail.credentialDisplayData
        val result = getCredentialCardState(credentialDisplayData)

        assertEquals(result.contentColor, Color.Black, "black text used")
    }

    @Test
    fun `If contrast of black is less than white, use white text`() = runTest {
        every { ColorUtils.calculateContrast(any(), Color.Black.toArgb()) } returns 0.2
        every { ColorUtils.calculateContrast(any(), Color.White.toArgb()) } returns 0.8

        val credentialDisplayData: CredentialDisplayData = MockCredentialDetail.credentialDisplayData
        val result = getCredentialCardState(credentialDisplayData)

        assertEquals(result.contentColor, Color.White, "white text used")
    }

    @Test
    fun `If contrast of black is more than white, use black text`() = runTest {
        every { ColorUtils.calculateContrast(any(), Color.Black.toArgb()) } returns 0.6
        every { ColorUtils.calculateContrast(any(), Color.White.toArgb()) } returns 0.2

        val credentialDisplayData: CredentialDisplayData = MockCredentialDetail.credentialDisplayData
        val result = getCredentialCardState(credentialDisplayData)

        assertEquals(result.contentColor, Color.Black, "black text used")
    }
}
