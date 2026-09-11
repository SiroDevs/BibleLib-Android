plugins {
    alias(libs.plugins.biblelib.android.library.compose)
    alias(libs.plugins.biblelib.hilt)
}

android {
    namespace = "com.biblelib.core.ui"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:database"))
    api(project(":core:design_system"))

    implementation(libs.androidx.compose.livedata)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.hilt.android)
    implementation(libs.zxing.core)
    implementation(libs.androidx.icons.extended)
    implementation(libs.androidx.core.ktx)
}
