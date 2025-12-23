plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.kreedzt.robin"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.kreedzt.robin"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API Regions Configuration
        // Format: "id|url|label_en|label_zh;id2|url2|label_en2|label_zh2"
        // Example: "china|https://robin.kreedzt.cn/|China Mainland|\\u4e2d\\u56fd\\u5927\\u9646;global|https://robin.kreedzt.com/|Global|\\u5168\\u7403"
        // Can be overridden by gradle.properties or environment variables
        val apiRegionsConfig = project.findProperty("API_REGIONS")?.toString()
            ?: System.getenv("API_REGIONS")
            ?: "global|https://robin.kreedzt.com/|Global|\\u5168\\u7403;china|https://robin.kreedzt.cn/|China Mainland|\\u4e2d\\u56fd\\u5927\\u9646"

        buildConfigField("String", "API_REGIONS_CONFIG", "\"${apiRegionsConfig.replace("\"", "\\\"")}\"")
    }

    // Create release signing config only if credentials are available
    val hasSigningCredentials = !((System.getenv("KEYSTORE_FILE") ?: findProperty("KEYSTORE_FILE")?.toString()).isNullOrEmpty()
        || (System.getenv("KEYSTORE_PASSWORD") ?: findProperty("KEYSTORE_PASSWORD")?.toString()).isNullOrEmpty()
        || (System.getenv("KEY_ALIAS") ?: findProperty("KEY_ALIAS")?.toString()).isNullOrEmpty()
        || (System.getenv("KEY_PASSWORD") ?: findProperty("KEY_PASSWORD")?.toString()).isNullOrEmpty())

    val releaseSigning = if (hasSigningCredentials) {
        signingConfigs.create("release") {
            storeFile = System.getenv("KEYSTORE_FILE")?.let { file(it) }
                ?: findProperty("KEYSTORE_FILE")?.let { file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD")?.toString()
                ?: findProperty("KEYSTORE_PASSWORD")?.toString()
            keyAlias = System.getenv("KEY_ALIAS")?.toString()
                ?: findProperty("KEY_ALIAS")?.toString()
            keyPassword = System.getenv("KEY_PASSWORD")?.toString()
                ?: findProperty("KEY_PASSWORD")?.toString()
        }
    } else {
        null
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            // Debug build allows cleartext traffic for local development
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            // Only sign if signing config is available
            signingConfig = releaseSigning
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Release build disallows cleartext traffic by default
            manifestPlaceholders["usesCleartextTraffic"] = "false"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")
    androidTestImplementation("org.mockito:mockito-android:5.8.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Gson for SharedPreferences serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // HTTP Client
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Firebase SDK for Google Analytics
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")
}