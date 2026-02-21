import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}

val isDebug = project.hasProperty("isDebug")

kotlin {
    androidTarget {
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Sample"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            // Material Icons
            implementation(libs.material.icons.core)
            implementation(libs.material.icons.extended)

            // Ktor Client (for sample API calls)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        val jvmMain by getting {
            dependencies {
                if (isDebug) implementation(project(":debukker"))
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
            }
            kotlin.srcDir(if (isDebug) "src/nonAndroidDebug/kotlin" else "src/nonAndroidRelease/kotlin")
        }
        val jsMain by getting {
            dependencies {
                if (isDebug) implementation(project(":debukker"))
            }
            kotlin.srcDir(if (isDebug) "src/nonAndroidDebug/kotlin" else "src/nonAndroidRelease/kotlin")
        }
        val wasmJsMain by getting {
            dependencies {
                if (isDebug) implementation(project(":debukker"))
            }
            kotlin.srcDir(if (isDebug) "src/nonAndroidDebug/kotlin" else "src/nonAndroidRelease/kotlin")
        }
        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies { if (isDebug) implementation(project(":debukker")) }
            kotlin.srcDir(if (isDebug) "src/nonAndroidDebug/kotlin" else "src/nonAndroidRelease/kotlin")
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

android {
    namespace = "com.spongycode.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.spongycode.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    debugImplementation(project(":debukker"))
}

compose.desktop {
    application {
        mainClass = "com.spongycode.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.spongycode.sample"
            packageVersion = "1.0.0"
        }
    }
}
