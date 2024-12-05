plugins {
    id("jacoco")
    id("com.android.application")
}

dependencies {
    implementation("org.jacoco:org.jacoco.core:0.8.11")
}

project.afterEvaluate {
    val buildTypes = android.buildTypes.map { type -> type.name }
    var productFlavors = android.productFlavors.map { flavor -> flavor.name }

    if (productFlavors.isEmpty()) {
        productFlavors = productFlavors + ""
    }

    productFlavors.forEach { flavorName ->
        buildTypes.forEach { buildTypeName ->

            val sourceName: String = if (flavorName.isEmpty()) {
                buildTypeName
            } else {
                "${flavorName}${buildTypeName.replaceFirstChar(Char::titlecase)}"
            }

            val testTaskName = "test${sourceName.replaceFirstChar(Char::titlecase)}UnitTest"

            registerCodeCoverageTask(
                testTaskName = testTaskName,
                sourceName = sourceName,
                flavorName = flavorName,
                buildTypeName = buildTypeName
            )
        }
    }
}

val excludes = setOf(
    "**/R.class",
    "**/R$*.class",
    "**/*\$ViewInjector*.*",
    "**/*\$ViewBinder*.*",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Factory*",
    "**/*_MembersInjector*",
    "**/*Module*",
    "**/*Component*",
    "**android**",
    "**/BR.class",
    // Hilt
    "**/dagger**/**",
    "**/hilt**/**",
)

fun Project.registerCodeCoverageTask(
    testTaskName: String,
    sourceName: String,
    flavorName: String,
    buildTypeName: String
) {
    tasks.register<JacocoReport>("${testTaskName}Coverage") {
        dependsOn(testTaskName)
        group = "Reporting"
        description = "Generate Jacoco coverage reports on the $sourceName build."

        val javaDirectories = fileTree(
            layout.buildDirectory.dir("intermediates/javac/$sourceName")
        ) { exclude(excludes) }

        val kotlinDirectories = fileTree(
            layout.buildDirectory.dir("tmp/kotlin-classes/$sourceName")
        ) { exclude(excludes) }

        val coverageSrcDirectories = listOf(
            "src/main/java",
            "src/$flavorName/java",
            "src/$buildTypeName/java",
            "src/main/kotlin",
            "src/$flavorName/kotlin",
            "src/$buildTypeName/kotlin"
        )

        classDirectories.setFrom(files(javaDirectories, kotlinDirectories))
        additionalClassDirs.setFrom(files(coverageSrcDirectories))
        sourceDirectories.setFrom(files(coverageSrcDirectories))
        executionData.setFrom(
            files(layout.buildDirectory.dir("jacoco/$testTaskName.exec"))
        )

        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
