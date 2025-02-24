package ch.admin.foitt.wallet.feature.qrscan

import ch.admin.foitt.wallet.feature.qrscan.mock.InMemoryCameraIntroRepository
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.CheckCameraPermission
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.implementation.CheckCameraPermissionImpl
import ch.admin.foitt.wallet.util.getFlagLists
import io.mockk.impl.annotations.SpyK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class CheckCameraPermissionTestPermissionGranted {
    @SpyK
    private var inMemoryCameraIntroRepository = InMemoryCameraIntroRepository()

    private lateinit var checkCameraPermissionUseCase: CheckCameraPermission

    @BeforeEach
    fun setup() {
        checkCameraPermissionUseCase = CheckCameraPermissionImpl(
            cameraIntroRepository = inMemoryCameraIntroRepository,
        )
    }

    @TestFactory
    fun `When permissions are granted the usecase should always return PermissionState Granted`(): List<DynamicTest> {
        val expected = PermissionState.Granted
        return getAllExpectedPermissionsStates().map { currentPermissionState ->
            DynamicTest.dynamicTest("$currentPermissionState should return $expected") {
                inMemoryCameraIntroRepository.value = currentPermissionState.permissionWasTriggeredOnce
                runTest {
                    val actual = checkCameraPermissionUseCase(
                        permissionsAreGranted = currentPermissionState.resultPermissionGranted,
                        rationaleShouldBeShown = currentPermissionState.resultRationaleShouldBeShown,
                        promptWasTriggered = currentPermissionState.resultPromptWasTriggered,
                    )
                    assertEquals(expected, actual)
                }
            }
        }
    }

    private fun getAllExpectedPermissionsStates() = getFlagLists(0, 7).map { flags ->
        PermissionsState(
            resultPermissionGranted = true,
            permissionWasTriggeredOnce = flags.getOrElse(0) { false },
            resultRationaleShouldBeShown = flags.getOrElse(1) { false },
            resultPromptWasTriggered = flags.getOrElse(2) { false },
        )
    }
}
