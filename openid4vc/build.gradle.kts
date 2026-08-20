plugins {
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.swiyu.android.library)
    alias(libs.plugins.mannodermaus.junit)
    kotlin("plugin.serialization") version "2.3.21"
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
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlinx.serialization.json)

    // ktor
    debugImplementation(libs.slf4j.android)
    releaseImplementation(libs.slf4j.nop)
    implementation(libs.bundles.ktor)

    // JWT
    implementation(libs.nimbus.jose.jwt)
    // Required at runtime by Nimbus' Ed25519Verifier (EdDSA signature verification)
    implementation(libs.tink.android)

    // DID resolver
    implementation(libs.didresolver)

    // Error handling
    implementation(libs.kotlin.result)
    implementation(libs.kotlin.result.coroutines)

    // Logging
    implementation(libs.timber)

    // Dcql
    implementation(libs.dcql)

    // Consistency
    implementation(libs.consistency)

    // Testing
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.slf4j.nop)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.ktor.server.core)
    testImplementation(libs.ktor.server.netty)

    val junitBom = platform(libs.junit.jupiter.bom)
    testImplementation(junitBom)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
