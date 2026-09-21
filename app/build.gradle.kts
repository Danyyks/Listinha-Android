// ============================================================
//  app/build.gradle.kts  —  configuração DO APLICATIVO
// ------------------------------------------------------------
//  É aqui que dizemos: qual a versão do Android, o nome do pacote,
//  quais bibliotecas o app usa, etc.
// ============================================================

plugins {
    alias(libs.plugins.android.application) // este módulo é um APP Android
    alias(libs.plugins.kotlin.android)      // escrito em Kotlin
    alias(libs.plugins.kotlin.compose)       // com telas em Jetpack Compose
    alias(libs.plugins.google.services)      // ATIVA o Firebase neste app (precisa do google-services.json na pasta app/)
}

android {
    // "namespace" = identificador interno do código (pacote base das classes).
    namespace = "com.listinha.app"

    // compileSdk = com qual versão do Android o código é COMPILADO.
    // (usar a mais recente estável dá acesso às APIs novas)
    compileSdk = 36

    defaultConfig {
        // applicationId = a "identidade única" do app na Play Store.
        // ATENÇÃO: depois de publicado, este ID NÃO pode mais mudar.
        // (o "com.listinha.app" já estava registrado por outra pessoa no
        //  Google Play — nome de pacote é único no mundo — então usamos este.)
        applicationId = "com.danyks.listinha"

        // minSdk = Android mínimo para instalar (26 = Android 8.0, cobre ~95%+).
        // Escolhido para suportar fontes variáveis (Fredoka/Nunito) e ícones adaptativos.
        minSdk = 26
        // targetSdk = versão para a qual o app foi testado/otimizado (a Play exige uma recente).
        targetSdk = 36

        versionCode = 1        // número interno que SOBE a cada envio para a loja (1, 2, 3...)
        versionName = "1.0"    // versão que o usuário vê ("1.0", "1.1"...)
    }

    buildTypes {
        release {
            // Na versão final (release), encolhe e ofusca o código para ficar menor/seguro.
            // Ligamos de verdade na Fase 8 (publicação); por ora deixamos desligado.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // O Android roda em Java 17 por baixo dos panos.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true // liga o Jetpack Compose neste módulo
    }
}

dependencies {
    // --- Base do Android/Kotlin ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // --- Jetpack Compose (o BOM alinha todas as versões) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)               // núcleo do Compose
    implementation(libs.androidx.ui.graphics)      // desenho/gráficos
    implementation(libs.androidx.ui.tooling.preview) // @Preview no Android Studio
    implementation(libs.androidx.material3)         // componentes Material 3 (Material You)

    // --- Liga tela + lógica (ViewModel) ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // --- Firebase (login + banco) ---
    // O platform(bom) NÃO adiciona código: só alinha as versões das libs abaixo.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)        // login com Google
    implementation(libs.firebase.firestore)   // banco de dados na nuvem

    // --- Corrotinas (tarefas em segundo plano, sem travar a tela) ---
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // --- Login com Google (Credential Manager) ---
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // --- Carregar a foto do perfil (imagem da internet) ---
    implementation(libs.coil.compose)

    // --- Tela de abertura (splash) ---
    implementation(libs.androidx.core.splashscreen)

    // Só na build de debug: ferramentas de inspeção do Compose.
    debugImplementation(libs.androidx.ui.tooling)
}
