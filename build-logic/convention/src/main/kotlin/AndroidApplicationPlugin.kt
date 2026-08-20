import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import project.convention.logic.coreLibraryDesugaring
import project.convention.logic.implementation
import project.convention.logic.lib
import project.convention.logic.version

class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")

            // Convention plugins
            apply(plugin = "swiyu.android.room")
            apply(plugin = "swiyu.android.dynatrace")
            apply(plugin = "swiyu.android.lint")
            apply(plugin = "swiyu.android.kover")

            extensions.configure<KotlinAndroidExtension> {
                jvmToolchain(version("javaVersion").toInt())
                compilerOptions {
                    allWarningsAsErrors.set(true)
                }
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = version("compileSdk").toInt()
                defaultConfig {
                    minSdk = version("minSdk").toInt()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                compileOptions {
                    isCoreLibraryDesugaringEnabled = true
                }

                // keeps only resources in these languages
                // if libs e.g. include resources in spanish they are not shipped with the app
                // "zz" is the debug locale, generated only for the specific flavors
                @Suppress("UnstableApiUsage")
                androidResources.localeFilters += arrayOf("en", "de", "fr", "it", "rm", "zz")

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                    debug {
                        isMinifyEnabled = false
                        isDebuggable = true
                    }
                }

                buildFeatures {
                    compose = true
                    buildConfig = true
                }

                packaging {
                    resources {
                        excludes.add("/META-INF/{AL2.0,LGPL2.1}")
                    }
                }

                testOptions {
                    animationsDisabled = true
                }

                dependencies {
                    coreLibraryDesugaring(lib("desugar.jdk.libs"))
                    implementation(lib("androidx.core.ktx"))
                    implementation(lib("appcompat"))
                    implementation(lib("androidx.lifecycle.runtime.compose"))
                    implementation(lib("androidx.lifecycle.process"))
                    implementation(lib("androidx.core.splashscreen"))
                }
            }
        }
    }
}
