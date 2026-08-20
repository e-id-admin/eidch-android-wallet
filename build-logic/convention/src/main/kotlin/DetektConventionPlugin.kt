import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import project.convention.logic.lib

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("io.gitlab.arturbosch.detekt")
            }
            dependencies {
                add("detektPlugins", lib("detekt-formatting"))
            }

            tasks.register("detektAll", Detekt::class) {
                buildUponDefaultConfig = true
                config.setFrom("$rootDir/config/detekt/detekt.yml")
                description = "Run detekt on whole project"
                autoCorrect = false
                basePath = rootDir.absolutePath
                exclude("**/build/**")
                include("**/src/**/*.kt")
                include("**/src/**/*.kts")
                parallel = true
                setSource(files(projectDir))
            }
        }
    }
}
