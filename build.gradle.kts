// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType


plugins {
    id(GradlePluginId.DETEKT)
    id(GradlePluginId.KTLINT_GRADLE)
    id(GradlePluginId.GRADLE_VERSION_PLUGIN)
    id(GradlePluginId.KOTLIN_JVM) apply false
    id(GradlePluginId.KOTLIN_ANDROID) apply false
    id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS) apply false
    id(GradlePluginId.ANDROID_APPLICATION) apply false
    id(GradlePluginId.ANDROID_DYNAMIC_FEATURE) apply false
    id(GradlePluginId.ANDROID_LIBRARY) apply false
    id(GradlePluginId.SAFE_ARGS) apply false
}


//already done in the setting.gradle
//buildscript {
//    //moved this definition to BuildSrc's object
//    //ext.kotlin_version = "1.3.61"
//    repositories {
//        google()
//        jcenter()
//    }
//    dependencies {
//        classpath("com.android.tools.build:gradle:3.6.2")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${CoreVersion.KOTLIN}")
//
//        // NOTE: Do not place your application dependencies here; they belong
//        // in the individual module build.gradle files
//    }
//}

// all projects = root project + sub projects
allprojects {
    repositories {
        google()
        jcenter()
    }
    // We want to apply ktlint at all project level because it also checks build gradle files
    apply(plugin = GradlePluginId.KTLINT_GRADLE)

    ktlint {
        version.set(CoreVersion.KOTLIN)
        verbose.set(true)
        android.set(true)

        // Uncomment below line and run .\gradlew ktlintCheck to see check ktlint experimental rules
        // enableExperimentalRules.set(true)

        reporters {
            reporter(ReporterType.CHECKSTYLE)
        }

        filter {
            exclude { element -> element.file.path.contains("generated/") }
        }
    }
}

subprojects {
    tasks.withType<Test>{
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

        apply(plugin = GradlePluginId.DETEKT)

        detekt{
            config = files("${project.rootDir}/detekt.yml")
            parallel = true
        }
    }
}


//JVM target applied to all kotlin tasks across all sub-projects
/** according to testing, this does not work*/
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}


tasks {
    //Gradle versions plugin configuration
    "dependencyUpdates"(DependencyUpdatesTask::class) {
        resolutionStrategy {
            componentSelection {
                all {
                    // Do not show pre-release version of library in generated dependency report
                    val rejected = listOf("alpha","beta","rc","cr","m","preview")
                        .map {qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*")}
                        .any { it.matches(candidate.version) }

                    if (rejected) {
                        reject("Release candidate")
                    }

                    //kAndroid newest version is 0.8.8 (jcenter), however maven repository contains version 0.8.7 and
                    // plugin fails to recognize it correctly
                    if (candidate.group == "com.pawegio.kandroid") {
                        reject("version ${candidate.version} is broken for ${candidate.group}'")
                    }
                }
            }
        }
    }
}

task("staticCheck") {
    description =
        """Mimics all static checks that run on CI.
        Note that this task is intended to run locally (not on CI), because on CI we prefer to have parallel execution
        and separate reports for each check (multiple statuses eg. on github PR page).
        """.trimMargin()

    group = "verification"
    afterEvaluate {
        // Filter modules with "lintDebug" task (non-Android modules do not have lintDebug task)
        val lintTasks = subprojects.mapNotNull {
            println(it.name)
            "${it.name}:lintDebug"
        }

        // Get modules with "testDebugUnitTest" task (app module does not have it)
        val testTasks = subprojects.mapNotNull {
            "${it.name}:testDebugUnitTest"
        }.filter { it != "app:testDebugUnitTest" }

        // All task dependencies
        val taskDependencies =
            mutableListOf ("app:assembleAndroidTest", "ktlintCheck", "detekt").also {
                it.addAll(lintTasks)
                it.addAll(testTasks)
            }

        //By defining Gradle dependency all dependent tasks will run before this "empty" task
        dependsOn(taskDependencies)
    }
}

//The gradle version
//task clean(type: Delete) {
//    delete(rootProject.buildDir)
//}
//The kotlon dsl version, equal to task("clean", Delete::class)
task<Delete>("clean") {
    delete(rootProject.buildDir)
}
