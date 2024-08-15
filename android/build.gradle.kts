
plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
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
    testImplementation(libs.weaverdb)  // force build of submodule
    api(fileTree(mapOf(    // pick up jar artifact from forced build
        "dir" to "../weaverdb/pgjava_c/build/libs/",
        "include" to listOf("*.jar"),
        "exclude" to listOf("*-sources.jar","*-javadoc.jar"),
    )))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.commons.compress)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

val srcs = tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
    from(fileTree(mapOf(
        "dir" to "../weaverdb/pgjava_c/src/main/java/",
    )))
}
val docset = sourceSets.create("combinedJavadoc") {
    java {
        srcDirs("src/main/java", "../weaverdb/pgjava_c/src/main/java")
        filter.exclude(
            "org/weaverdb/WeaverCmdLine.java",
            "org/weaverdb/sample/**",
            "org/weaverdb/WeaverReferenceFactory17.java",
            "org/weaverdb/DBReferenceFactory.java",
            "org/weaverdb/StreamingTransformer.java",
            "org/weaverdb/StreamingTransformer17.java",
        )
    }
}

val docs = tasks.register<Javadoc>("docs") {
    dependsOn(tasks.get("build"))
    source = docset.java
    classpath += files(configurations["releaseRuntimeClasspath"])
    classpath += files(configurations["androidApis"])
}

val docsJar = tasks.create<Jar>("docsJar") {
    archiveClassifier.set("javadoc")
    from(fileTree(mapOf(
        "dir" to layout.buildDirectory.dir("docs/javadoc/"),
    )))
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "org.weaverdb.android"
            artifactId = "dbhome"
            version = "1.0.2"
            artifact(srcs)
            artifact(docsJar)

            afterEvaluate {
                from(components["release"])
            }
            pom {
                name = "Android WeaverDB"
                description = "AAR library of WeaverDB for Android"
                url = "https://github.com/weaverdb/weaver_android"
                properties = mapOf(
                )
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
}