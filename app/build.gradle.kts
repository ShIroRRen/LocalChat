@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.android.build.gradle.tasks.PackageAndroidArtifact

plugins {
	id("com.android.application") version "8.12.0"
	id("org.jetbrains.kotlin.android") version "+"
	id("org.jetbrains.kotlin.plugin.compose") version "+"
	id("org.lsposed.lsplugin.resopt") version "+"
}

kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_24

android {
	namespace = "work.niggergo.localchat"
	compileSdk = 36
	buildToolsVersion = "36.1.0"
	ndkVersion = "29.0.14033849"

	defaultConfig {
		applicationId = "work.niggergo.localchat"
		minSdk = 23
		targetSdk = 36
		versionCode = 1
		versionName = "1"

		externalNativeBuild.cmake {
			arguments(
				"-DBUILD_JNI=TRUE",
				"-DBUILD_FOR_ANDROID=TRUE",
				"-DANDROID_STL=c++_static",
				"-DMNN_OPENCL=ON"
			)
		}

		ndk.abiFilters += listOf("arm64-v8a", "x86_64")
		androidResources.localeFilters += listOf("en", "zh")
	}
	externalNativeBuild.cmake.path("src/main/jni/CMakeLists.txt")
	sourceSets.getByName("main").jniLibs.srcDir("libs")
	buildTypes {
		release {
			signingConfig = signingConfigs.getByName("debug")
			isMinifyEnabled = true
			isShrinkResources = true
			vcsInfo.include = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_24
		targetCompatibility = JavaVersion.VERSION_24
	}
	buildFeatures {
		buildConfig = true
		compose = true
	}
	tasks.withType<PackageAndroidArtifact> {
		doFirst { appMetadata.asFile.orNull?.writeText("") }
	}
	packaging {
		jniLibs {
			keepDebugSymbols.clear()
			useLegacyPackaging = true
		}
		resources.excludes += listOf(
			"META-INF/**",
			"**/*.kotlin_builtins",
			"DebugProbesKt.bin",
			"kotlin-tooling-metadata.json",
			"**.properties"
		)
	}
	dependenciesInfo {
		includeInApk = false
		includeInBundle = false
	}
	lint {
		checkReleaseBuilds = false
		abortOnError = false
	}
}

// noinspection GradleDynamicVersion
dependencies {
	implementation("com.google.android.material:material:+")
	implementation("androidx.core:core-ktx:+")
	implementation("androidx.activity:activity-compose:+")
	implementation(platform("androidx.compose:compose-bom:+"))
	implementation("androidx.compose.foundation:foundation")
	implementation("androidx.compose.material3:material3")
	implementation("androidx.compose.runtime:runtime")
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling-preview")
	implementation("com.github.jeziellago:compose-markdown:+")
}