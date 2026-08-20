import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import project.convention.logic.androidTestImplementation
import project.convention.logic.implementation
import project.convention.logic.ksp
import project.convention.logic.lib

internal class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("androidx.room")
            }
            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }
            dependencies {
                ksp(lib("androidx-room-compiler"))
                implementation(lib("androidx-room-ktx"))
                implementation(lib("sqlcipher-android"))
                androidTestImplementation(lib("androidx-room-testing"))
            }
        }
    }
}
