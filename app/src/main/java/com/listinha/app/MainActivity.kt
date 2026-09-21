package com.listinha.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
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
//    - alguém logado   -> HomeScreen (Lista + Perfil)
// ============================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Mostra a tela de abertura (splash) antes de tudo. Precisa vir
        // ANTES do super.onCreate para o Android exibi-la corretamente.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                    )
                }
            }
        }
    }
}
