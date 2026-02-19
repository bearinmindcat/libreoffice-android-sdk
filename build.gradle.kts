plugins {
    id("com.android.library") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("maven-publish")
}

android {
    namespace = "org.libreoffice.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        consumerProguardFiles("consumer-rules.pro")

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "org.libreoffice"
            artifactId = "libreoffice-android-sdk"
            version = "0.0.1-beta"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("LibreOffice Android SDK")
                description.set("LibreOffice document processing SDK for Android")
                url.set("https://github.com/bearinmindcat/libreoffice-android-sdk")

                licenses {
                    license {
                        name.set("Mozilla Public License 2.0")
                        url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                    }
                }
            }
        }
    }
}
