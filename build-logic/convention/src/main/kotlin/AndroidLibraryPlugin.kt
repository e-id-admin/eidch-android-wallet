import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import project.convention.logic.version

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")

            // Convention plugins
            apply(plugin = "swiyu.android.room")
            apply(plugin = "swiyu.android.lint")
            apply(plugin = "swiyu.android.kover")

            extensions.configure<KotlinAndroidExtension> {
                jvmToolchain(version("javaVersion").toInt())
                compilerOptions {
                    allWarningsAsErrors.set(true)
                }
            }

            extensions.configure<LibraryExtension> {
                compileSdk = version("compileSdk").toInt()
                defaultConfig {
                    minSdk = version("minSdk").toInt()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                buildTypes {
                    release {
                        // minification is done when the whole app is build
                        isMinifyEnabled = false
                        consumerProguardFiles(
                            "consumer-proguard-rules.pro"
                        )
                    }
                }

                packaging {
                    resources {
                        excludes.add("/META-INF/{AL2.0,LGPL2.1}")
                    }
                }

                testOptions {
                    animationsDisabled = true
                }
            }
        }
    }
}
