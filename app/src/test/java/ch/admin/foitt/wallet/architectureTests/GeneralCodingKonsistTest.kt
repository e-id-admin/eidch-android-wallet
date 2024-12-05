package ch.admin.foitt.wallet.architectureTests

import ch.admin.foitt.wallet.app.MainActivity
import ch.admin.foitt.wallet.app.WalletApplication
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.functions
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.ext.provider.hasAnnotationOf
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import javax.inject.Inject

class GeneralCodingKonsistTest {
    @Test
    fun `unit test should not use JUnit4`() {
        Konsist
            .scopeFromTest()
            .classes()
            .filterNot { "androidTest" in it.path }
            .functions()
            .assertFalse {
                it.annotations.any { annotation ->
                    annotation.fullyQualifiedName == "org.junit.Test"
                }
            }
    }

    @Test
    fun `instrumentation tests should not use JUnit5`() {
        Konsist
            .scopeFromTest()
            .classes()
            .filter { "androidTest" in it.path }
            .functions()
            .assertFalse(additionalMessage = "Bitbar cannot run JUnit 5 tests") {
                it.annotations.any { annotation ->
                    annotation.fullyQualifiedName == "org.junit.jupiter.api.Test"
                }
            }
    }

    @Test
    fun `no class should use field injection`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filterNot {
                // Exceptions: Application and MainActivity
                listOf(
                    WalletApplication::class.java.name,
                    MainActivity::class.java.name
                ).contains(it.fullyQualifiedName)
            }
            .properties()
            .assertFalse { it.hasAnnotationOf<Inject>() }
    }

    @TestFactory
    fun `check for 'blacklisted' methods or classes`(): Stream<DynamicTest> =
        Konsist
            .scopeFromProduction()
            .files
            .stream()
            .flatMap { file ->
                Stream.of(
                    DynamicTest.dynamicTest("${file.name}: compose.material should not be used") {
                        file.assertFalse(additionalMessage = "use 'compose.material3' instead") {
                            it.hasImport { import ->
                                // Exceptions: for now we allow material.icons, material.pullrefresh and material.ExperimentalApi
                                import.hasNameMatching(
                                    "androidx\\.compose\\.material\\.(?!icons|pullrefresh|ExperimentalMaterialApi).*".toRegex()
                                )
                            }
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: collectAsState() should not be used") {
                        file.assertFalse(additionalMessage = "use 'collectAsStateWithLifecycle' instead") {
                            it.hasImportWithName("androidx.compose.runtime.collectAsState")
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: vanilla Kotlin Result should not be used") {
                        file.assertFalse(additionalMessage = "use 'michaelbull.result instead'") {
                            it.hasImportWithName("kotlin.Result")
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: java.util.logging and android.util.Log should not be used") {
                        file.assertFalse(additionalMessage = "use 'Timber' instead") {
                            it.hasImportWithName(listOf("java.util.logging..", "android.util.Log"))
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: sharedPreferences should not be used") {
                        file.assertFalse(additionalMessage = "use 'encryptedSharedPreferences' instead") {
                            it.hasImportWithName("android.content.sharedPreferences")
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: android.util.Base64 should not be used") {
                        file.assertFalse {
                            it.hasImportWithName("android.util.Base64")
                        }
                    },
                    DynamicTest.dynamicTest(
                        "${file.name}: java.util.Base64 should not be used outside of 'platform_utils' package or 'openid4vc' module"
                    ) {
                        if (!file.hasPackage("..platform.utils..") && !file.resideInModule("openid4vc")) {
                            file.assertFalse {
                                it.hasImportWithName("java.util.Base64")
                            }
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: RoomDatabase should not be used outside of 'platform_database' package") {
                        if (!file.hasPackage("..platform.database..")) {
                            file.assertFalse {
                                it.hasImportWithName("androidx.room.RoomDatabase")
                            }
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: kotlinx.serialization.json.Json methods should not be used") {
                        val exceptions = listOf("SafeJson")
                        if (!file.hasClassWithName(names = exceptions)) {
                            file.assertFalse(additionalMessage = "use methods from 'SafeJson' instead") {
                                it.hasImportWithName(
                                    listOf(
                                        "kotlinx.serialization.encodeToString",
                                        "kotlinx.serialization.decodeFromString",
                                        "kotlinx.serialization.json.encodeToJsonElement",
                                        "kotlinx.serialization.json.decodeFromJsonElement"
                                    )
                                )
                            }
                        }
                    },
                )
            }
}
