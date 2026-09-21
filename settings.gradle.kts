// ============================================================
//  settings.gradle.kts  —  "porta de entrada" do projeto Gradle
// ------------------------------------------------------------
//  Este arquivo diz ao Gradle:
//   1) de onde baixar os plugins e as bibliotecas (repositórios)
//   2) qual é o nome do projeto
//   3) quais módulos existem (aqui só temos o módulo ":app")
// ============================================================

pluginManagement {
    // Onde o Gradle procura os PLUGINS (ex: plugin do Android, do Kotlin)
    repositories {
        google()          // repositório oficial do Google (coisas de Android)
        mavenCentral()    // repositório público gigante de bibliotecas Java/Kotlin
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Proíbe cada módulo de declarar seus próprios repositórios:
    // todos usam a lista central abaixo (mais organizado e seguro).
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()          // onde ficam as bibliotecas do Android/Compose/Firebase
        mavenCentral()
    }
}

// Nome que aparece na barra de título da IDE
rootProject.name = "Listinha"

// Incluímos o módulo do aplicativo. Todo app Android tem pelo menos um ":app".
include(":app")
