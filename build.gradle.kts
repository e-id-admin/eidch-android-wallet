buildscript {
    dependencies {
        classpath(libs.detekt.gradle.plugin)
        classpath(libs.dynatrace.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.swiyu.android.detekt)
}
