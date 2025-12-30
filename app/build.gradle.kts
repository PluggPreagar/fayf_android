import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream

val versionPropsFile = file("version.properties")
val versionProps = Properties()

if (versionPropsFile.exists()) {
    versionProps.load(FileInputStream(versionPropsFile))
}

val versionCode = versionProps["versionCode"].toString().toInt()
val versionName = versionProps["versionName"].toString()


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
        this.versionCode = versionCode
        this.versionName = versionName
        versionCode = 1  // needed for build bundles
        versionName = versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
            buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
            buildConfigField( "String", "GIT_COMMIT_HASH", "\"${getGitCommitHash()}\"")
        }
        debug {
            buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
            buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
            buildConfigField( "String", "GIT_COMMIT_HASH", "\"${getGitCommitHash()}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true // Enable BuildConfig generation
    }
/*
    packagingOptions {
        exclude("META-INF/INDEX.LIST")
    }
*/
}

fun getGitCommitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

tasks.register("incrementVersionCode") {
    doLast {
        versionProps["versionCode"] = (versionCode + 1).toString()
        versionProps.store(FileOutputStream(versionPropsFile), null)
        println("Version code incremented to ${versionProps["versionCode"]}")
    }
}

tasks.register("incrementVersionName") {
    doLast {
        val (major, minor, patch) = versionName.split(".").map { it.toInt() }
        versionProps["versionName"] = "$major.$minor.${patch + 1}"
        versionProps.store(FileOutputStream(versionPropsFile), null)
        println("Version name incremented to ${versionProps["versionName"]}")
    }
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
    // qr code
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}