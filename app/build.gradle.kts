import com.android.build.api.artifact.SingleArtifact

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.vtsdaily3"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.vtsdaily3"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("com.opencsv:opencsv:5.9")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.runtime)
    implementation(libs.ui)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.camera.camera2.pipe)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation(libs.androidx.documentfile)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.apache.poi)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    debugImplementation(libs.androidx.ui.tooling)
}

val apkDropDir: File = file("C:/AutoSyncToPhone/PassengerSchedules/Builds")

androidComponents {
    onVariants(selector().all()) { variant ->
        val cap = variant.name.replaceFirstChar { it.uppercaseChar() }

        val apkArtifact = variant.artifacts.get(SingleArtifact.APK)

        val copyTask = tasks.register("copy${cap}ApkToCustomFolder") {
            doLast {
                apkDropDir.mkdirs()

                val artifactFile = apkArtifact.get().asFile
                val base = if (artifactFile.isDirectory) artifactFile else artifactFile.parentFile

                val apkTree = project.fileTree(base) { include("**/*.apk") }
                val apkFiles = apkTree.files

                if (apkFiles.isEmpty()) {
                    logger.warn("⚠ No APKs found for variant '${variant.name}'.")
                } else {
                    copy {
                        from(apkTree)
                        into(apkDropDir)
                        duplicatesStrategy = DuplicatesStrategy.INCLUDE
                    }
                    println("✅ Copied ${apkFiles.size} APK(s) for ${variant.name} to: $apkDropDir")
                }
            }
        }

        tasks.configureEach {
            if (name == "package$cap" || name == "assemble$cap" || name == "install$cap") {
                finalizedBy(copyTask)
            }
        }
    }
}