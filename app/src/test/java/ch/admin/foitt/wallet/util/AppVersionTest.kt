package ch.admin.foitt.wallet.util

import ch.admin.foitt.wallet.platform.utils.AppVersion
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class AppVersionTest {

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(strings = ["1.2.4", "1.2.10", "1.3.0", "1.3", "1.3.10", "1.10.3", "2.0.0", "2", "2.0.3", "10.2.3", "10"])
    fun `App version comparison where other is higher returns is higher`(otherVersion: String) {
        val version = AppVersion("1.2.3")
        val other = AppVersion(otherVersion)

        val isHigher = version < other

        assertEquals(true, isHigher)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1.2.2", "1.2", "1.1.10", "1.1.3", "1.1.0", "1.0.0", "1", "0.10.4", "0.3.3", "0.0.0"])
    fun `App version comparison where other is lower returns is lower`(otherVersion: String) {
        val version = AppVersion("1.2.3")
        val other = AppVersion(otherVersion)

        val isLower = other < version

        assertEquals(true, isLower)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1.2.2", "1.2", "1.1.10", "1.1.3", "1.1.0", "1.0.0", "1", "0.10.4", "0.3.3", "0.0.0"])
    fun `App version comparison where other is equal returns is equal`(versionString: String) {
        val version = AppVersion(versionString)
        val other = AppVersion(versionString)

        val isEqual = version == other

        assertEquals(true, isEqual)
    }
}
