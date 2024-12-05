plugins {
    id("android-sdk")
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.junit5)
}

android {
    namespace = "ch.admin.foitt.openid4vc"
}

dependencies {

    implementation(libs.androidx.core.ktx)

    // Dagger/Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // ktor
    debugImplementation(libs.slf4j.android)
    releaseImplementation(libs.slf4j.nop)
    implementation(libs.bundles.ktor)

    // JWT
    implementation(libs.nimbus.jose.jwt)

    // Did
    implementation(libs.didresolver)
    implementation(libs.jna) {
        artifact {
            type = "aar"
        }
    }

    // Error handling
    implementation(libs.kotlin.result)
    implementation(libs.kotlin.result.coroutines)

    // Logging
    implementation(libs.timber)

    // Testing
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.slf4j.nop)
    testImplementation(libs.mockk)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    // for "Parameterized Tests"
    testImplementation(libs.junit.jupiter.params)
}
