// Publishing your Kotlin Multiplatform library to Maven Central
// https://dev.to/kotlin/how-to-build-and-publish-a-kotlin-multiplatform-library-going-public-4a8k

import java.util.*

plugins {
    id("maven-publish")
    id("signing")
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null
ext["mavenRepository"] = null
ext["mavenSnapshots"] = null


// 从local.properties文件或环境变量中获取密钥，这些密钥可以在CI上使用
// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply { load(it) }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
    ext["mavenRepository"] = System.getenv("MAVEN_REPOSITORY")
    ext["mavenSnapshots"] = System.getenv("MAVEN_SNAPSHOTS")
}

// Stub javadoc.jar artifact
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

// 获取extra属性
fun getExtraString(name: String): String {
    return project.extra[name] as String
}

publishing {
    // Configure maven central repository
    repositories {
        maven {
            isAllowInsecureProtocol = true
            name = "sonatype"
            setUrl(getExtraString("mavenSnapshots"))
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set(project.name)
            description.set(project.description)
            // url.set("") todo

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    // id.set("") todo
                    // name.set("") todo
                    // email.set("") todo
                }
            }
            scm {
                // url.set("") todo
            }
        }
    }
}

// Signing artifacts. Signing.* extra properties values will be used
signing {
    if (getExtraString("signing.keyId").isNotBlank()) {
        sign(publishing.publications)
    }
}

