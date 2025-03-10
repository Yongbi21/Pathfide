plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    kotlin("kapt") // Add this line to apply the kapt plugin
}

android {
    namespace = "com.example.mindpath"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mindpath"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.1")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")
    implementation("com.google.firebase:firebase-installations-ktx:18.0.0")
    implementation("androidx.constraintlayout:constraintlayout-core:1.0.4")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.1")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("androidx.compose.foundation:foundation-android:1.7.3")
    implementation("com.google.firebase:firebase-messaging:24.0.2")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-common:19.1.0")
    implementation("com.google.mlkit:vision-common:17.3.0")
    implementation("androidx.credentials:credentials:1.5.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:5.15.3") {
        exclude(group = "com.android.support")
    }
    // Google signin
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Ensure you have the correct AndroidX dependencies
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.7")
    implementation("com.github.bumptech.glide:glide:4.15.0")
    kapt("com.github.bumptech.glide:compiler:4.15.0")
    implementation ("com.google.android.material:material:1.9.0")
    // Retrofit library for making HTTP requests
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

    // Converter for parsing JSON responses
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp for HTTP requests
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okio:okio:3.4.0")


    // Logging interceptor for OkHttp (optional, for logging requests/responses)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("io.agora.rtc:full-sdk:4.4.1")
    implementation ("com.google.mlkit:text-recognition:16.0.0")
    implementation ("com.google.mlkit:face-detection:16.1.7")


    implementation ("androidx.work:work-runtime-ktx:2.9.0")
    implementation ("androidx.work:work-runtime:2.9.0")

    // Core
    implementation ("androidx.core:core:1.12.0")
    implementation ("androidx.core:core-ktx:1.12.0")

    implementation ("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:3.9.2")

}