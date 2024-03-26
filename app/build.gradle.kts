plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}



android {
    namespace = "com.ecom.fyp2023"
    compileSdk = 34

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
    }

    defaultConfig {
        applicationId = "com.ecom.fyp2023"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
    buildToolsVersion = rootProject.extra["buildToolsVersion"] as String
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.google.android.material:material:1.12.0-alpha03")
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation ("org.greenrobot:eventbus:3.3.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.github.AnyChart:AnyChart-Android:1.1.5")
    implementation ("jp.wasabeef:richeditor-android:2.0.0")
    implementation ("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")

}