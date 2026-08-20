package project.convention.logic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

fun Project.property(key: String, defaultValue: String = ""): String =
    providers.gradleProperty(key)
        .orElse(providers.environmentVariable(key))
        .getOrElse(defaultValue)

fun Project.lib(name: String): Provider<MinimalExternalModuleDependency> =
    extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary(name).orElseThrow {
        RuntimeException("Could not find library '$name' in version catalog.")
    }

fun Project.plugin(name: String): String =
    extensions.getByType<VersionCatalogsExtension>().named("libs").findPlugin(name).map { it.get().pluginId }.orElseThrow {
        RuntimeException("Could not find plugin '$name' in version catalog.")
    }

fun Project.version(name: String): String =
    extensions.getByType<VersionCatalogsExtension>().named("libs").findVersion(name).map {
        it.requiredVersion
    }.orElseThrow {
        RuntimeException("Could not find version '$name' in version catalog.")
    }
