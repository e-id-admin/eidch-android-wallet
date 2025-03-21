plugins {
    id("android-common")
    id("com.android.application")
    id("com.mikepenz.aboutlibraries.plugin")
    id("dynatrace-android-app")
}

android {
    compileSdk = 35

    defaultConfig {
        minSdk = 29
        targetSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

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
            isShrinkResources = false
            isDebuggable = true
        }
    }

    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        allWarningsAsErrors = true
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

    lint {
        lintConfig = file("$rootDir/config/lint/custom-lint.xml")
        quiet = false
        abortOnError = properties.getOrDefault("abortOnLintError", "true") == "true"

        // Warnings
        ignoreWarnings = false
        checkAllWarnings = true
        warningsAsErrors = false

        // If true, running lint on the app module will also run it on all the
        // dependent modules the app depends on. This way, lint has only be
        // invocated once and not again for each module.
        checkDependencies = true

        // Report formats and output paths.
        absolutePaths = false
        xmlReport = true
        htmlReport = true
    }

    testOptions {
        animationsDisabled = true
    }
}
