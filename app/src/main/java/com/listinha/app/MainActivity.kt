package com.listinha.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.listinha.app.auth.AuthViewModel
import com.listinha.app.ui.screens.HomeScreen
import com.listinha.app.ui.screens.LoginScreen
import com.listinha.app.ui.theme.ListinhaTheme

// ============================================================
//  MainActivity.kt  —  o ponto de entrada do app
// ------------------------------------------------------------
//  Decide o TEMA (claro/escuro/automático) e qual tela mostrar:
//    - ninguém logado  -> tela de Login
//    - alguém logado   -> HomeScreen (Lista + Receitas + Perfil)
//  Também recebe texto do "Compartilhar" de outros apps (uma
//  receita do Instagram, de um site...) e manda para Receitas.
// ============================================================

class MainActivity : ComponentActivity() {

    // Texto que chegou pelo "Compartilhar" de outro app (null = nenhum).
    private var sharedText by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Mostra a tela de abertura (splash) antes de tudo. Precisa vir
        // ANTES do super.onCreate para o Android exibi-la corretamente.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Só na 1ª abertura: ao girar a tela o Android recria a Activity com o
        // MESMO intent, e não queremos abrir a receita compartilhada de novo.
        if (savedInstanceState == null) readSharedText(intent)

        setContent {
            // Tema escolhido pelo usuário. Fica AQUI em cima para valer no app
            // inteiro. Começa em "auto" e é sincronizado com o perfil ao logar.
            var themeMode by rememberSaveable { mutableStateOf("auto") }

            // Converte a escolha em "está escuro?":
            //  light -> claro | dark -> escuro | auto -> segue o sistema do celular
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            // Ícones da barra de status (hora, bateria) seguem o tema DO APP, e
            // não o do celular: com o celular claro e o app em "Escuro", eles
            // ficavam escuros sobre fundo escuro (quase invisíveis).
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                )
                onDispose { }
            }

            ListinhaTheme(darkTheme = darkTheme) {
                val authViewModel: AuthViewModel = viewModel()
                val context = LocalContext.current
                val webClientId = stringResource(R.string.default_web_client_id)

                val user = authViewModel.currentUser

                if (user == null) {
                    LoginScreen(
                        isLoading = authViewModel.isLoading,
                        errorMessage = authViewModel.errorMessage,
                        onSignIn = { authViewModel.signInWithGoogle(context, webClientId) },
                    )
                } else {
                    HomeScreen(
                        userId = user.uid,
                        userName = user.displayName,
                        userEmail = user.email,
                        photoUrl = user.photoUrl?.toString(),
                        themeMode = themeMode,
                        onThemeModeChange = { themeMode = it },
                        onSignOut = { authViewModel.signOut(context) },
                        onDeleteAccount = { authViewModel.deleteAccount(context, webClientId) },
                        sharedText = sharedText,
                        onSharedTextConsumed = { sharedText = null },
                    )
                }
            }
        }
    }

    // App já aberto e chegou outro compartilhamento.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readSharedText(intent)
    }

    // Lê o texto compartilhado. Muitos sites mandam o TÍTULO da página no
    // "assunto" e só o link no texto — juntamos os dois (o título vira o
    // nome da receita).
    private fun readSharedText(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        if (intent.type?.startsWith("text/") != true) return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim().orEmpty()
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)?.trim().orEmpty()
        val full = if (subject.isNotEmpty() && !text.contains(subject)) "$subject\n$text" else text
        if (full.isNotBlank()) sharedText = full
    }
}
