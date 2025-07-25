// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("com.google.firebase.crashlytics") version "3.0.3" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    extra.apply {
        set("room_version", "2.6.1")
    }
}





tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}