package ch.admin.foitt.wallet.architectureTests

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

// This class is (mis-)using Konsist to gain information on the code base
// It does not contain any actual tests
class ArchitectureInfos {

    @Test
    @Disabled
    fun `show usage of platform packages`() = runTest {
        val featurePackageImports = Konsist.scopeFromProduction()
            .classes()
            .withPackage("..feature..")
            .filter {
                it.resideOutsidePackage("..test..")
            }
            .map {
                val feature = "feature\\.\\w+".toRegex().find(it.fullyQualifiedName)?.groupValues?.first() ?: it.name
                feature to it.containingFile.imports
            }

        val platformPackageImports = Konsist.scopeFromProduction()
            .classes()
            .withPackage("..platform..")
            .filter {
                it.resideOutsidePackage("..test..")
            }
            .map {
                val platform = "platform\\.\\w+".toRegex().find(it.fullyQualifiedName)?.groupValues?.first() ?: it.name
                platform to it.containingFile.imports.filterNot { import ->
                    import.hasNameContaining(platform)
                }
            }

        val platformPackages = Konsist.scopeFromProduction()
            .classes()
            .withPackage("..platform..")
            .filter {
                it.resideOutsidePackage("..test..")
            }
            .mapNotNull { platform ->
                "${CleanArchitectureKonsistTest.BASE_PACKAGE}.*\\.platform\\.\\w+".toRegex()
                    .find(platform.fullyQualifiedName)
                    ?.groupValues
                    ?.first()
            }
            .toSortedSet()

        platformPackages.forEach { platformPackage ->
            val featuresUsingPackage = featurePackageImports.mapNotNull { (feature, featureImports) ->
                if (featureImports.any { featureImport -> featureImport.name.startsWith(platformPackage) }) {
                    feature
                } else {
                    null
                }
            }.toSortedSet()

            val platformUsingPackage = platformPackageImports.mapNotNull { (feature, platformImports) ->
                if (platformImports.any { platformImport -> platformImport.name.startsWith(platformPackage) }) {
                    "platform\\.\\w+".toRegex().find(feature)?.groupValues?.first() ?: feature
                } else {
                    null
                }
            }.toSortedSet()

            val appUsingPackage = Konsist.scopeFromProduction()
                .classes()
                .withPackage("..app..")
                .filter {
                    it.resideOutsidePackage("..test..")
                }.flatMap { app ->
                    app.containingFile.imports.mapNotNull { appImport ->
                        if (appImport.name.startsWith(platformPackage)) {
                            app.name
                        } else {
                            null
                        }
                    }
                }

            println(platformPackage)
            println(":: ${appUsingPackage.count()} app: $appUsingPackage")
            println(":: ${featuresUsingPackage.count()} features:  $featuresUsingPackage")
            println(":: ${platformUsingPackage.count()} platforms: $platformUsingPackage")
        }
    }
}
