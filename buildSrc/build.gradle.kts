plugins {
    `kotlin-dsl`
}
val kotlinVersion = "1.9.20"
dependencies{
    implementation("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    implementation("com.github.johnrengelman:shadow:8.1.1")
    implementation("org.graalvm.buildtools.native:org.graalvm.buildtools.native.gradle.plugin:0.9.28")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.10")
}
repositories {
    maven("https://mirrors.cloud.tencent.com/gradle/")
    maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    maven("https://maven.aliyun.com/repository/public")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://plugins.gradle.org/m2/")
    maven("https://jitpack.io")
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/amper/amper")
}