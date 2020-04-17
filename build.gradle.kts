// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    //TODO: move this definition to BuildSrc
    //ext.kotlin_version = "1.3.61"
    repositories {
        google()
        jcenter()
        
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        
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
