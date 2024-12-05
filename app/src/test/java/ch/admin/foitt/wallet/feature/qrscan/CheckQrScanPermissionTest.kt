package ch.admin.foitt.wallet.feature.qrscan

import ch.admin.foitt.wallet.feature.qrscan.domain.model.PermissionState
import ch.admin.foitt.wallet.feature.qrscan.domain.usecase.CheckQrScanPermission
import ch.admin.foitt.wallet.feature.qrscan.domain.usecase.implementation.CheckQrScanPermissionImpl
import ch.admin.foitt.wallet.feature.qrscan.mock.InMemoryCameraIntroRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.SpyK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CheckQrScanPermissionTest {

    @SpyK
    private var inMemoryCameraIntroRepository = InMemoryCameraIntroRepository()

    private lateinit var checkQrScanPermissionUseCase: CheckQrScanPermission

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        checkQrScanPermissionUseCase = CheckQrScanPermissionImpl(
            cameraIntroRepository = inMemoryCameraIntroRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Given a fresh state, a permission check should return the intro state`() = runTest {
        val result = checkQrScanPermissionUseCase(
            permissionsAreGranted = false,
            rationaleShouldBeShown = false,
            promptWasTriggered = false,
        )

        assertFalse(inMemoryCameraIntroRepository.value)
        assertEquals(PermissionState.Intro, result)
    }

    @Test
    fun `Given the intro was shown, a rejected permission check without having shown rationale should return a permanently denied state`() = runTest {
        inMemoryCameraIntroRepository.value = true
        val result = checkQrScanPermissionUseCase(
            permissionsAreGranted = false,
            rationaleShouldBeShown = false,
            promptWasTriggered = false,
        )
        assertTrue(inMemoryCameraIntroRepository.value)
        assertEquals(PermissionState.Blocked, result)
    }

    @Test
    fun `A rejected prompt without rationale should return a permanently denied state`() = runTest {
        val result = checkQrScanPermissionUseCase(
            permissionsAreGranted = false,
            rationaleShouldBeShown = false,
            promptWasTriggered = true,
        )
        assertTrue(inMemoryCameraIntroRepository.value)
        assertEquals(PermissionState.Blocked, result)
    }
}
