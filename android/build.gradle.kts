plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "org.weaverdb.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 27
        aarMetadata {
            minCompileSdk = 27
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }

        }
        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add( "x86_64")
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
    externalNativeBuild {
        cmake {
            path("${project.rootDir}/weaverdb/CMakeLists.txt")
            version = "3.29.6"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    ndkVersion = "27.0.11902837 rc2"
    buildToolsVersion = "35.0.0"
    publishing {
        singleVariant("release") {

        }
    }

}

dependencies {
    api(fileTree(mapOf(
        "dir" to "../weaverdb/pgjava_c/build/libs/",
        "include" to listOf("*.aar", "*.jar"),
    )))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.commons.compress)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "org.wearverdb.android"
            artifactId = "dbhome"
            version = "1.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "myrepo"
            url = uri("${project.buildDir}/repo")
        }
    }
}
