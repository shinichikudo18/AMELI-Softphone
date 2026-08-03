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
        // Repositorio Maven oficial de Belledonne Communications (Liblinphone SDK).
        // Verificado: https://www.linphone.org/en/news/liblinphone-sdk-available-through-git-maven-repository/
        maven { url = uri("https://download.linphone.org/maven_repository") }
    }
}

rootProject.name = "AMELI Softphone"
include(":app")
