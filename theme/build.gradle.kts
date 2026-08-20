plugins {
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.swiyu.android.library)
    alias(libs.plugins.mannodermaus.junit)
}

android {
    namespace = "ch.admin.foitt.wallet.theme"
}

dependencies {
    // Dagger/Hilt
    implementation(libs.hilt.android)

    // Compose and material
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
}
