import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.lepu.ble"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lepu.ble"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        val dateFormat = SimpleDateFormat("yyyyMMddHHmm")
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        val date = dateFormat.format(Date())
        setProperty("archivesBaseName", "O2M_v${versionName}(${versionCode})_${date}")
    }

    signingConfigs {
        create("release") {
            storeFile = File("F:\\LepuBleUtis\\app\\key.jks")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.bluetooth)
    implementation(libs.kable)
    implementation(libs.khronicle)
    implementation(libs.accompanist.permissions)
    implementation(libs.bundles.krayon)
    implementation(libs.exercise.annotations)

    ksp(libs.exercise.compile)

    implementation(libs.stream)
    implementation(libs.stream.file)

}

