@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google() //{
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//                includeGroupByRegex("com\\.google\\.android\\.gms.*")
//            }
//        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Pocitaj"
include(":app")
 