import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-13
 */
fun KotlinMultiplatformExtension.jvmInit() {
    jvm() {
        // 将依赖的jar包打包到生成的jar包中
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = jdkVersion
                javaParameters = true
                freeCompilerArgs = listOf(
                    "-Xjsr305=strict",
                    // "-Dfile.encoding=UTF-8"
                )
            }
        }
        jvmToolchain(jdkVersion.toInt())
    }
}

fun KotlinMultiplatformExtension.mingwX64Init() {
    mingwX64 {
        binaries {
            executable {
                // Change to specify fully qualified name of your application's entry point:
                entryPoint = mainClassPath
                // Specify command-line arguments, if necessary:
                runTask?.args("")

            }
        }
    }
}

fun KotlinMultiplatformExtension.linuxInit() {
    linuxX64 {
        binaries {
            executable {
                // Change to specify fully qualified name of your application's entry point:
                entryPoint = mainClassPath
                // Specify command-line arguments, if necessary:
                runTask?.args("")

            }
        }
    }
}