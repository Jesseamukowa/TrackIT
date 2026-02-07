import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.budgettracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.budgettracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- API KEY CONFIGURATION START ---
        val properties = Properties()
        val propertiesFile = project.rootProject.file("local.properties")

        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use { properties.load(it) }
        }

        // Get the value from local.properties
        val apiKey = properties.getProperty("GEMINI_API_KEY") ?: ""

        // Use DOUBLE quotes for the type "String" and for the apiKey variable
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")

    }

    packaging {
        resources {
            // Remove the "META-INF/*" line and use these specific ones instead
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"

            // If you still see conflicts with license files, add these:
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        // Required to use BuildConfig in modern Android Studio
        buildConfig = true
    }
}


dependencies {
    // UI and Core Libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Charts for your Budget Stats screen
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.code.gson:gson:2.10.1")



        //The official Android-optimized Gemini SDK
    implementation(platform("com.google.firebase:firebase-bom:34.8.0")) // Use latest BoM
    implementation("com.google.firebase:firebase-ai")

    implementation("com.google.guava:guava:33.0.0-android")



    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}