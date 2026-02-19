import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10"
    id("com.google.devtools.ksp") version "2.3.6"
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "app.upvpn.upvpn"
    compileSdk = 36

    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = "app.upvpn.upvpn"
        minSdk = 24
        targetSdk = 36
        versionCode = 15
        versionName = "u6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    externalNativeBuild {
        cmake {
            path("tunnel/CMakeLists.txt")
        }
    }

    if (keystorePropertiesFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile =
                    file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {

        all {
            externalNativeBuild {
                cmake {
                    targets("libwg-go.so")
                    arguments("-DGRADLE_USER_HOME=${project.gradle.gradleUserHomeDir}")
                    arguments("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
                }
            }
        }

        debug {
            isMinifyEnabled = false
            versionNameSuffix = ".debug"
            val baseUrl = gradleLocalProperties(rootDir, providers).getProperty(
                "baseUrl",
                "\"https://upvpn.dev\""
            )
            buildConfigField(
                "String",
                "UPVPN_BASE_URL",
                baseUrl
            )

            externalNativeBuild {
                cmake {
                    arguments("-DANDROID_PACKAGE_NAME=${namespace}")
                }
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "UPVPN_BASE_URL", "\"https://upvpn.app\"")

            signingConfig = signingConfigs.getByName("release")

            externalNativeBuild {
                cmake {
                    arguments("-DANDROID_PACKAGE_NAME=${namespace}")
                }
            }
        }
    }

    flavorDimensions += listOf("upvpn")

    productFlavors {
        create("production") {
            dimension = "upvpn"
            isDefault = true
            buildConfigField("Boolean", "IS_AMAZON", "false")
        }

        create("amazon") {
            dimension = "upvpn"
            buildConfigField("Boolean", "IS_AMAZON", "true")
            versionNameSuffix = ".amzn"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("17")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val navVersion = "2.8.3"
    val roomVersion = "2.8.4"
    val sandwichVersion = "1.3.9"
    val billingVersion = "7.1.1"

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui:1.7.4")
    implementation("androidx.compose.ui:ui-graphics:1.7.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.4")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.4")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("com.google.accompanist:accompanist-adaptive:0.32.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.18")

    implementation("androidx.room:room-runtime:$roomVersion")

    // Kotlin Symbol Processing (KSP) for Room
    ksp("androidx.room:room-compiler:$roomVersion")

    // Network response
    implementation("com.github.skydoves:sandwich:$sandwichVersion")

    // IAP
    implementation("com.android.billingclient:billing:$billingVersion")
    implementation("com.android.billingclient:billing-ktx:$billingVersion")


    // for java.time
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
