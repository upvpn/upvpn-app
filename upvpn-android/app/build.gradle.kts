import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    id("com.google.devtools.ksp") version "1.9.10-1.0.13"
    id("kotlin-parcelize")
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "app.upvpn.upvpn"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.upvpn.upvpn"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "u1-beta"

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
                }
            }
        }

        debug {
            isMinifyEnabled = false
            versionNameSuffix = ".debug"
            val baseUrl = gradleLocalProperties(rootDir).getProperty("baseUrl")
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
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val navVersion = "2.7.5"
    val roomVersion = "2.6.0"
    val sandwichVersion = "1.3.9"

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-graphics:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("com.google.accompanist:accompanist-adaptive:0.32.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.18")

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$roomVersion")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    // Network response
    implementation("com.github.skydoves:sandwich:$sandwichVersion")

    // for java.time
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
