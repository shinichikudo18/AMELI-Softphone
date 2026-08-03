pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // El repositorio Maven de Liblinphone se añade en la Fase 2, tras verificar su URL oficial.
    }
}

rootProject.name = "AMELI Softphone"
include(":app")
