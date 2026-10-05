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
    }
}

rootProject.name = "alarysai"

include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:ui")
include(":core:navigation")
include(":core:firebase")
include(":core:testing")
include(":feature:home")
include(":feature:questionnaires")
include(":feature:tips")
include(":feature:advertisers")
include(":feature:history")
include(":feature:auth")
include(":feature:plans")
