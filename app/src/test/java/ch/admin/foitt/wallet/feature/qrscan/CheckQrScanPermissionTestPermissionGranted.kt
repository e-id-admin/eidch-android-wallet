package ch.admin.foitt.wallet.feature.qrscan

import ch.admin.foitt.wallet.feature.qrscan.domain.model.PermissionState
import ch.admin.foitt.wallet.feature.qrscan.domain.usecase.CheckQrScanPermission
import ch.admin.foitt.wallet.feature.qrscan.domain.usecase.implementation.CheckQrScanPermissionImpl
import ch.admin.foitt.wallet.feature.qrscan.mock.InMemoryCameraIntroRepository
import ch.admin.foitt.wallet.util.getFlagLists
import io.mockk.impl.annotations.SpyK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class CheckQrScanPermissionTestPermissionGranted {
    @SpyK
    private var inMemoryCameraIntroRepository = InMemoryCameraIntroRepository()

    private lateinit var checkQrScanPermissionUseCase: CheckQrScanPermission

    @BeforeEach
    fun setup() {
        checkQrScanPermissionUseCase = CheckQrScanPermissionImpl(
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
                    val actual = checkQrScanPermissionUseCase(
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
