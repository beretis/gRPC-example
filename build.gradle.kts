import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension.OutputTarget

plugins {
    kotlin("multiplatform") version "1.8.0"
    id("com.android.application")
    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version "0.2.1"
    id("org.jetbrains.kotlin.native.cocoapods") version "1.8.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

repositories {
    //...
    maven(url = "https://jitpack.io")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    //For iOS support, configure cocoapods
    cocoapods {
        summary = "..."
        homepage = "..."
        ios.deploymentTarget = "14.0"

            pod("gRPC-ProtoRPC", moduleName = "GRPCClient")
            pod("Protobuf")
    }

    
    iosArm64 {
        binaries {
            framework {
                baseName = "library"
            }
        }
    }
    android()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib:0.2.1")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val nativeMain by getting
        val nativeTest by getting
        val iosArm64Main by getting
        val iosArm64Test by getting
        val androidMain by getting {
            dependencies {
                implementation("com.google.android.material:material:1.6.1")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

android {
    compileSdkVersion(32)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        applicationId = "org.example.library"
        minSdkVersion(24)
        targetSdkVersion(32)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

grpcKotlinMultiplatform {
    targetSourcesMap.put(OutputTarget.COMMON, listOf(kotlin.sourceSets.getByName("commonMain")))
    targetSourcesMap.put(OutputTarget.IOS, listOf(kotlin.sourceSets.getByName("iosArm64Main")))
    targetSourcesMap.put(OutputTarget.JVM, listOf(kotlin.sourceSets.getByName("androidMain")))

    //Specify the folders where your proto files are located, you can list multiple.
    protoSourceFolders.set(listOf(projectDir.resolve("../protos/src/main/proto")))
}