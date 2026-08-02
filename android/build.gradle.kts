
plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
}

configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "org.weaverdb.android"
    compileSdk = 37

    defaultConfig {
        minSdk = 34
        aarMetadata {
            minCompileSdk = 34
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
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
        getByName("debug") {
            isJniDebuggable = true
            isMinifyEnabled = false
        }
    }
    externalNativeBuild {
        cmake {
            path = file("${project.rootDir}/weaverdb/CMakeLists.txt")
            version = "4.1.2"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    ndkVersion = "28.2.13676358"
    buildToolsVersion = "37.0.0"
    lint {
        targetSdk = 37
    }
    testOptions {
        targetSdk = 37
    }
    publishing {
        singleVariant("release")
        singleVariant("debug")
    }
}

dependencies {
    testImplementation(libs.weaverdb)  // force build of submodule
    api(fileTree(mapOf(    // pick up jar artifact from forced build
        "dir" to "../weaverdb/pgjava_c/build/libs/",
        "include" to listOf("*.jar"),
        "exclude" to listOf("*-sources.jar", "*-javadoc.jar"),
    )))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.commons.compress)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

val sourcesJar by tasks.registering(Jar::class) {
    description = "Creates a jar containing the source code."
    archiveClassifier.set("sources")
    from("src/main/java")
    from(fileTree(mapOf(
        "dir" to "../weaverdb/pgjava_c/src/main/java/",
    )))
}

val docs by tasks.registering(Javadoc::class) {
    description = "Generates Javadoc for the project."
    dependsOn(tasks.named("build"))
    source("src/main/java")
    source("../weaverdb/pgjava_c/src/main/java")
    exclude("org/weaverdb/WeaverCmdLine.java")
    exclude("org/weaverdb/sample/**")
    exclude("org/weaverdb/WeaverReferenceFactory17.java")
    exclude("org/weaverdb/DBReferenceFactory.java")
    exclude("org/weaverdb/StreamingTransformer.java")
    exclude("org/weaverdb/StreamingTransformer17.java")

    classpath = configurations["releaseRuntimeClasspath"]
    val androidComponents = extensions.getByType<com.android.build.api.variant.LibraryAndroidComponentsExtension>()
    classpath += files(androidComponents.sdkComponents.bootClasspath)
}

val docsJar by tasks.registering(Jar::class) {
    description = "Creates a jar containing the Javadoc."
    dependsOn(docs)
    archiveClassifier.set("javadoc")
    from(docs.map { it.destinationDir!! })
}

publishing {
    publications {
        register<MavenPublication>("debug") {
            groupId = "org.weaverdb.android"
            artifactId = "dbhome"
            version = "1.0.3-debug"

            afterEvaluate {
                from(components["debug"])
            }
            pom {
                name = "Android WeaverDB"
                description = "AAR library of WeaverDB for Android"
                url = "https://github.com/weaverdb/weaver_android"
                licenses {
                    license {
                        name = "BSD 3 Clause License"
                        url = "https://github.com/weaverdb/weaver_android/blob/main/LICENSE"
                    }
                }
                developers {
                    developer {
                        id = "mkscott"
                        name = "Myron Scott"
                        email = "myron@weaverdb.org"
                    }
                }
                scm {
                    connection = "scm:git:git://git@github.com:weaverdb/weaver_android.git"
                    developerConnection = "scm:ssh://git@github.com:weaverdb/weaver_android.git"
                    url = "https://github.com/weaverdb/weaver_android.git"
                }
            }
        }
        register<MavenPublication>("release") {
            groupId = "org.weaverdb.android"
            artifactId = "dbhome"
            version = "1.0.3"
            artifact(sourcesJar)
            artifact(docsJar)

            afterEvaluate {
                from(components["release"])
            }
            pom {
                name = "Android WeaverDB"
                description = "AAR library of WeaverDB for Android"
                url = "https://github.com/weaverdb/weaver_android"
                licenses {
                    license {
                        name = "BSD 3 Clause License"
                        url = "https://github.com/weaverdb/weaver_android/blob/main/LICENSE"
                    }
                }
                developers {
                    developer {
                        id = "mkscott"
                        name = "Myron Scott"
                        email = "myron@weaverdb.org"
                    }
                }
                scm {
                    connection = "scm:git:git://git@github.com:weaverdb/weaver_android.git"
                    developerConnection = "scm:ssh://git@github.com:weaverdb/weaver_android.git"
                    url = "https://github.com/weaverdb/weaver_android.git"
                }
            }
        }
    }
    repositories {
        maven {
            name = "myrepo"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    sign(publishing.publications["release"])
    sign(publishing.publications["debug"])
}
