package ch.admin.foitt.wallet.architectureTests

import androidx.lifecycle.ViewModel
import androidx.room.Dao
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withParentClassOf
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertNotEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

class CleanArchitectureKonsistTest {
    companion object {
        internal const val BASE_PACKAGE = "ch.admin.foitt"
    }

    @Test
    fun `clean architecture has correct layer dependencies`() = runTest {
        Konsist.scopeFromProduction().assertArchitecture {
            val presentation = Layer("Presentation", "..presentation..")
            val domain = Layer("Domain", "..domain..")
            val data = Layer("Data", "..data..")

            domain.dependsOnNothing()
            presentation.dependsOn(domain)
            data.dependsOn(domain)
        }
    }

    @Test
    fun `base packages have correct dependencies on other base packages`() = runTest {
        Konsist.scopeFromProduction()
            .assertArchitecture {
                val app = Layer("app", "$BASE_PACKAGE..app..")
                val feature = Layer("feature", "$BASE_PACKAGE..feature..")
                val platform = Layer("platform", "$BASE_PACKAGE..platform..")

                app.dependsOn(platform, feature)
                feature.dependsOn(platform)
                platform.dependsOnNothing()
            }
    }

    @TestFactory
    fun `feature packages should not depend on other feature packages`(): Stream<DynamicTest> =
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("$BASE_PACKAGE..feature..")
            .stream()
            .flatMap { feature ->
                Stream.of(
                    DynamicTest.dynamicTest("${feature.name} should not depend on other feature packages") {
                        val featureName = ".*\\.feature\\.\\w+".toRegex()
                            .find(feature.fullyQualifiedName)
                            ?.groupValues
                            ?.first() ?: return@dynamicTest
                        runTest {
                            val otherImportedFeatures = feature.containingFile.imports.filter {
                                it.hasNameMatching(Regex("$BASE_PACKAGE.*\\.feature\\..*")) && !it.hasNameContaining(featureName)
                            }
                            otherImportedFeatures.assertEmpty()
                        }
                    }
                )
            }

    @Test
    fun `domain_repository should only contain interfaces and no classes`() = runTest {
        val domainRepositories = Konsist.scopeFromPackage("..domain.repository")
        domainRepositories
            .interfaces()
            .assertNotEmpty()

        domainRepositories
            .classes()
            .assertEmpty()
    }

    @TestFactory
    fun `@Dao interfaces should be in data layer and file name end in Dao`(): Stream<DynamicTest> = Konsist.scopeFromProduction()
        .interfaces()
        .withAnnotationOf(Dao::class)
        .stream()
        .flatMap { dao ->
            Stream.of(
                DynamicTest.dynamicTest("${dao.fullyQualifiedName} should reside in ..data.. package") {
                    runTest {
                        dao.assertTrue {
                            it.resideInPackage("..data..")
                        }
                    }
                },
                DynamicTest.dynamicTest("${dao.fullyQualifiedName} should end in Dao") {
                    runTest {
                        dao.assertTrue {
                            it.hasNameEndingWith("Dao")
                        }
                    }
                },
            )
        }

    @TestFactory
    fun `'Repository' files should reside in 'repository' package`(): Stream<DynamicTest> =
        Konsist
            .scopeFromProduction()
            .files
            .withNameEndingWith("Repository")
            .stream()
            .map { repositoryFile ->
                DynamicTest.dynamicTest("${repositoryFile.name} should reside inside repository package") {
                    runTest {
                        repositoryFile.assertTrue {
                            it.packagee?.fullyQualifiedName?.contains(".repository")
                        }
                    }
                }
            }

    @Test
    fun `ViewModels classes should have 'ViewModel' suffix`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withParentClassOf(listOf(ViewModel::class, ScreenViewModel::class))
            .assertTrue { it.name.endsWith("ViewModel") }
    }

    @TestFactory
    fun `use cases should implement an interface with single method 'invoke' and have Impl suffix`(): Stream<DynamicTest> = Konsist
        .scopeFromProduction()
        .classes()
        .withPackage("..domain.usecase.implementation")
        .stream()
        .flatMap { useCase ->
            Stream.of(
                DynamicTest.dynamicTest("${useCase.fullyQualifiedName} should end with Impl") {
                    runTest {
                        useCase.assertTrue {
                            it.hasNameEndingWith("Impl")
                        }
                    }
                },
                DynamicTest.dynamicTest("${useCase.fullyQualifiedName} should have single public method named 'invoke'") {
                    runTest {
                        useCase.assertTrue {
                            val hasSingleInvokeOperatorMethod = it.hasFunction { function ->
                                function.name == "invoke" && function.hasPublicOrDefaultModifier
                            }

                            hasSingleInvokeOperatorMethod && it.countFunctions { item -> item.hasPublicOrDefaultModifier } == 1
                        }
                    }
                },
                DynamicTest.dynamicTest("${useCase.fullyQualifiedName} should implement interface from parent package") {
                    runTest {
                        useCase.assertTrue {
                            it.hasParentInterface { parentInterface ->
                                val hasSingleInvokeOperatorMethod = parentInterface.hasFunction { function ->
                                    function.name == "invoke" && function.hasPublicOrDefaultModifier && function.hasOperatorModifier
                                }
                                hasSingleInvokeOperatorMethod && parentInterface.packagee?.fullyQualifiedName == useCase.packagee?.fullyQualifiedName?.removeSuffix(".implementation")
                            }
                        }
                    }
                },
            )
        }

    private val highPrioUseCases = Konsist.scopeFromProduction()
        .classes()
        .withPackage(
            "..platform.credential.domain.usecase..",
            "..platform.sdjwt.domain.usecase..",
            "..platform.login.domain.usecase..",
            "..openid4vc.domain.usecase..",
            "..platform.ssi.domain.usecase..",
        )

//    private val mediumPrioUseCases = Konsist.scopeFromProduction()
//        .classes()
//        .withPackage(
//            "..platform.passphrase.domain.usecase..",
//            "..platform.locale.domain.usecase..",
//            "..platform.biometricPrompt.domain.usecase..",
//            "..platform.database.domain.usecase..",
//            "..platform.updateCredentialStatus.domain.usecase..",
//            "..platform.biometrics.domain.usecase..",
//            "..platform.keystoreCrypto.domain.usecase..",
//            "..platform.passphrasePeppering.domain.usecase..",
//            "..platform.passphrase.passphraseHashing.domain.usecase..",
//        )

    private val testClasses = Konsist.scopeFromTest().classes()
    private val useCases = highPrioUseCases // + mediumPrioUseCases

    @TestFactory
    fun `use cases should have tests`(): Stream<DynamicTest> =
        // Konsist.scopeFromProduction().classes().withPackage("..domain.usecase..")  // all use cases
        useCases
            .stream()
            .flatMap { useCase ->
                Stream.of(
                    DynamicTest.dynamicTest("${useCase.fullyQualifiedName} should have a test") {
                        runTest {
                            useCase.assertTrue { case ->
                                testClasses.any { testCase ->
                                    testCase.name == case.name + "Test" && testCase.packagee?.fullyQualifiedName == case.packagee?.fullyQualifiedName
                                }
                            }
                        }
                    }
                )
            }
}
