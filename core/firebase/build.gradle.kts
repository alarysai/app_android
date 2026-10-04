plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.alarysai.alarysai.core.firebase"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        // android.util.Log is called when a malformed document is skipped.
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    api(platform(libs.firebase.bom))
    api(libs.firebase.firestore)
    // The BoM would raise it to 24.x (Kotlin 2.3 metadata); keep the pinned 23.x.
    api(libs.firebase.auth) {
        version { strictly(libs.versions.firebaseAuth.get()) }
    }
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.play.services)

    testImplementation(libs.junit)
}
