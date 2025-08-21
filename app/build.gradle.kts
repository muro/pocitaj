import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutLibraries)
}

val (appVersionCode, appVersionName) = Versioning.getVersionInfo()

val keystorePropsFile = rootProject.file("app/keystore.properties")
val keystoreProps = Properties()
// Use a try-catch block to avoid build failures if the file is missing (e.g., on a CI server)
try {
    keystoreProps.load(keystorePropsFile.inputStream())
} catch (e: Exception) {
    println("keystore.properties file not found, skipping signing configuration.")
}


android {
    namespace = "dev.aidistillery.pocitaj"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.aidistillery.pocitaj"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        buildConfigField("String", "VERSION_NAME", "\"${appVersionName}\"")

        testInstrumentationRunner = "dev.aidistillery.pocitaj.PocitajTestRunner"
    }
    signingConfigs {
        create("release") {
            // Only configure signing if the properties file was found
            if (keystorePropsFile.exists()) {
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
                storeFile = file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }
    lint {
        disable.add("IconMissingDensityFolder")
    }
    testOptions {
        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    tasks.withType<Test> {
        //timeout.set(Duration.ofSeconds(10))
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
    tasks.register("printVersion") {
        doLast {
            // Call the function from your Versioning object
            val (code, name) = Versioning.getVersionInfo()

            // Print the results to the console
            println("Version Code: $code")
            println("Version Name: $name")
        }
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            optIn.add("kotlin.time.ExperimentalTime")
        }
    }
    packaging {
        resources {
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/LICENSE-notice.md")
        }
        jniLibs {
            keepDebugSymbols.add("*/arm64-v8a/*.so")
            keepDebugSymbols.add("*/armeabi-v7a/*.so")
            keepDebugSymbols.add("*/x86/*.so")
            keepDebugSymbols.add("*/x86_64/*.so")
        }
    }
}

allprojects {
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.tasks)
    implementation(libs.digiink)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.core.ktx)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.material3.android)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.datastore.preferences)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.ext.junit.ktx)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

aboutLibraries {
    android {
        registerAndroidTasks = true
    }
    collect {
        configPath = file("licenses")
    }
}