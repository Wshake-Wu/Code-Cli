import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.buildscript
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories


fun Project.repositoriesInit() {
    allprojects {
        repositories {
            mavenInit()
        }
        buildscript {
            repositories {
                mavenInit()
            }
        }
    }
}

private fun RepositoryHandler.mavenInit() {
    mavenLocal()
    maven("https://mirrors.cloud.tencent.com/gradle/")
    maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    maven("https://maven.aliyun.com/repository/public")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://plugins.gradle.org/m2/")
    maven("https://jitpack.io")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/amper/amper")
}