// ============================================================
//  build.gradle.kts (RAIZ)  —  configuração do projeto inteiro
// ------------------------------------------------------------
//  Aqui só DECLARAMOS os plugins que os módulos poderão usar.
//  "apply false" = "deixe disponível, mas não ligue aqui na raiz".
//  Quem realmente liga cada plugin é o app/build.gradle.kts.
//
//  Os "libs.plugins.*" vêm do catálogo de versões:
//  gradle/libs.versions.toml (lá ficam as versões, num lugar só).
// ============================================================

plugins {
    alias(libs.plugins.android.application) apply false // plugin que transforma o módulo num APP Android
    alias(libs.plugins.kotlin.android) apply false      // suporte à linguagem Kotlin no Android
    alias(libs.plugins.kotlin.compose) apply false       // compilador do Jetpack Compose (Kotlin 2.0+)
    alias(libs.plugins.google.services) apply false      // conecta o app ao Firebase (lê o google-services.json)
}
