plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.fayf_android002"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fayf_android002"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
/*
    packagingOptions {
        exclude("META-INF/INDEX.LIST")
    }
*/
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment:2.9.5")
    implementation("androidx.navigation:navigation-ui:2.9.5")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    // allow
    // - formatted logging by slf4j
    // - inject shortened stacktrace for errors
    // - clone log info to textview
    // - use android log stream
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("com.github.tony19:logback-android:2.0.0"){
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    configurations.all {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    //
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.robolectric:robolectric:4.10")
}