import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import kotlin.collections.set

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-01
 */

var mainClassPath = ""
var archivesBaseName = ""
var jdkVersion = "17"

fun Project.rootInit() {
    repositoriesInit()
    allprojects {
        // 动态版本version主要用于构建不同环境jar如：开发，测试，预览，生产……等环境 gradle publish -PVERSION=1.0.0-TEST-SNAPSHOT 或者 gradle build -PVERSION=1.0.0-DEV-SNAPSHOT
        if (project.hasProperty("projectVersion")) {
            version = properties["projectVersion"] as String
        }
    }
    subprojects {
        configurations.configureEach() {
            // Gradle 的依赖解析过程中缓存的一些动态信息时间
            resolutionStrategy.cacheChangingModulesFor(10, "seconds")
        }
    }
}


fun Project.toolJvmInit() {
    apply(plugin = "application")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")
    extensions.configure<KotlinMultiplatformExtension> {
        targets.all {
            compilations.all {
                compilerOptions.configure {
                    // 是否将所有警告视为错误
                    // allWarningsAsErrors.set(true)
                }
            }
            sourceSets.all {
                languageSettings {
                    languageVersion = KotlinVersion.KOTLIN_1_9.version
                    apiVersion = KotlinVersion.KOTLIN_1_9.version
                }
            }
        }
        jvmInit()
    }
    extensions.configure<JavaApplication> {
        mainClass.set(mainClassPath)
        applicationName = archivesBaseName
        applicationDefaultJvmArgs = listOf("-Xms128m", "-Xmx256m")
    }
    extensions.configure<AllOpenExtension> {
        preset("all-open")
    }

    shadowJarInit()
    taskInit()
}

fun Project.composeInit(){
    apply(plugin = "org.jetbrains.compose")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")
    extensions.configure<KotlinMultiplatformExtension> {
        targets.all {
            compilations.all {
                compilerOptions.configure {
                    // 是否将所有警告视为错误
                    // allWarningsAsErrors.set(true)
                }
            }
            sourceSets.all {
                languageSettings {
                    languageVersion = KotlinVersion.KOTLIN_1_9.version
                    apiVersion = KotlinVersion.KOTLIN_1_9.version
                }
            }
        }
    }
    extensions.configure<AllOpenExtension> {
        preset("all-open")
    }
    shadowJarInit()
    taskInit()
}





fun Project.taskInit() {
    tasks {
        withType<Jar> {
            manifest {
                attributes["Main-Class"] = mainClassPath
            }
        }
        withType<Test> {
            useJUnitPlatform()
        }
        withType<JavaCompile>().configureEach {
            options.release.set(jdkVersion.toInt())
        }
    }
}


