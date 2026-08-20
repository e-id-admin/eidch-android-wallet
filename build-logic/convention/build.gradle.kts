plugins {
    `kotlin-dsl`
}

group = "project.build.convention.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
    implementation(libs.androidx.room.gradle.plugin)
    compileOnly(libs.dynatrace.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.swiyu.android.application.get().pluginId
            implementationClass = "AndroidApplicationPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.swiyu.android.library.get().pluginId
            implementationClass = "AndroidLibraryPlugin"
        }
        register("roomConventionPlugin") {
            id = libs.plugins.swiyu.android.room.get().pluginId
            implementationClass = "RoomConventionPlugin"
        }
        register("dynatraceConventionPlugin") {
            id = libs.plugins.swiyu.android.dynatrace.get().pluginId
            implementationClass = "DynatraceConventionPlugin"
        }
        register("detektConventionPlugin") {
            id = libs.plugins.swiyu.android.detekt.get().pluginId
            implementationClass = "DetektConventionPlugin"
        }
        register("lintConventionPlugin") {
            id = libs.plugins.swiyu.android.lint.get().pluginId
            implementationClass = "LintConventionPlugin"
        }
        register("koverConventionPlugin") {
            id = libs.plugins.swiyu.android.kover.get().pluginId
            implementationClass = "KoverConventionPlugin"
        }
    }
}
