import com.android.build.gradle.internal.dsl.BaseFlavor
import com.android.build.gradle.internal.dsl.DefaultConfig

plugins {

    id(GradlePluginId.ANDROID_APPLICATION)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.KOTLIN_ANDROID_EXTENSIONS)
    id(GradlePluginId.KTLINT_GRADLE)
    id(GradlePluginId.SAFE_ARGS)
//    id("com.android.application")
//    //kotlin("android") is the same as id("kotlin-android")
//    kotlin("android")
//    //kotlin("android.extensions") is the same as id("kotlin-android-extensions")
//    kotlin("android.extensions")
}

android {
    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)


    defaultConfig {
        //compileSdkVersion(12)
        applicationId = "edu.nju.ics.alex.wang.androidshowcasemimic"
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
        buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)

        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME
        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER

        //self-defined function
        buildConfigFieldFromGradleProperty("apiBaseUrl")
        buildConfigFieldFromGradleProperty("apiToken")

        buildConfigField("FEATURE_MODULE_NAMES", getDynamicFeatureModuleNames())

    }

    buildTypes {
        getByName(BuildType.RELEASE) {
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
        }

        // seems like following to options can be inside or ourside
        testOptions {
            unitTests.isReturnDefaultValues = TestOptions.IS_RETURN_DEFAULT_VALUES
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    // Each feature module that is included in settings.gradle.kts is added here as dynamic feature
    dynamicFeatures = ModuleDependency.getDynamicFeatureModules().toMutableSet()

    lintOptions {
        // By default lint does not check test sources, but setting this option means that lint will not even parse them
        isIgnoreTestSources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}


dependencies {
    api(project(ModuleDependency.LIBRARY_BASE))

    implementation(LibraryDependency.OK_HTTP)
    implementation(LibraryDependency.LOGGING_INTERCEPTOR)
    implementation(LibraryDependency.PLAY_CORE)
    implementation(LibraryDependency.STETHO)
    implementation(LibraryDependency.STETHO_OK_HTTP)

    api(LibraryDependency.RETROFIT)
    api(LibraryDependency.RETROFIT_MOSHI_CONVERTER)
    api(LibraryDependency.SUPPORT_CONSTRAINT_LAYOUT)
    api(LibraryDependency.COORDINATOR_LAYOUT)
    api(LibraryDependency.RECYCLER_VIEW)
    api(LibraryDependency.MATERIAL)
    api(LibraryDependency.FRAGMENT_KTX)
    api(LibraryDependency.K_ANDROID)
    api(LibraryDependency.LOTTIE)

    //To be moved the the library module
    api(LibraryDependency.KODEIN)
    api(LibraryDependency.KODEIN_ANDROID_X)
    api(LibraryDependency.TIMBER)


//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${CoreVersion.KOTLIN}")
//    implementation("androidx.appcompat:appcompat:1.0.2")
//    implementation("androidx.core:core-ktx:1.0.2")
//    testImplementation("junit:junit:4.12")
//    androidTestImplementation("androidx.test.ext:junit:1.1.1")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

fun BaseFlavor.buildConfigFieldFromGradleProperty(gradlePropertyName: String) {
    val propertyValue = project.properties[gradlePropertyName] as? String
    checkNotNull(propertyValue) { "Gradle property $gradlePropertyName is Null" }

    val androidResourceName = "GRADLE_${gradlePropertyName.toSnakeCase()}".toUpperCase()
    buildConfigField("String", androidResourceName, propertyValue)
}

fun String.toSnakeCase() = this.split(Regex("(?=[A_Z])")).joinToString("-") { it.toLowerCase()}

fun DefaultConfig.buildConfigField(name: String, value: Set<String>) {
    //Generates String that holds Java String Array code
    val strValue = value.joinToString(prefix = "{", separator = ",", postfix = "}"){"\"$it\""}
    buildConfigField("String[]",name, strValue)
}

fun getDynamicFeatureModuleNames() = ModuleDependency.getDynamicFeatureModules()
    .map { it.replace(":feature_", "")}
    .toSet()
