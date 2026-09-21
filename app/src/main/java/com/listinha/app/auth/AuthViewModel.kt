package com.listinha.app.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.listinha.app.data.ShoppingRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ============================================================
//  AuthViewModel.kt  —  o "gerente" do login
// ------------------------------------------------------------
//  "ViewModel" é uma peça da arquitetura recomendada pelo Google:
//  ela guarda o ESTADO e a LÓGICA de uma tela, separados do
//  desenho. Assim, se a tela girar ou for recriada, o login não
//  se perde.
//
//  Este ViewModel faz o papel do seu hook useAuth do React:
//    - sabe QUEM está logado (currentUser)
//    - faz o login com Google (signInWithGoogle)
//    - faz o logout (signOut)
// ============================================================

class AuthViewModel : ViewModel() {

    // Atalho para o serviço de login do Firebase.
    private val auth = Firebase.auth

    // Repositório para apagar os dados do usuário ao excluir a conta.
    private val repo = ShoppingRepository()

    // --- ESTADO que a tela observa ---
    // "mutableStateOf" cria um valor que, ao mudar, faz o Compose
    // redesenhar a tela automaticamente.

    // Usuário logado (ou null se ninguém entrou ainda).
    var currentUser by mutableStateOf<FirebaseUser?>(auth.currentUser)
        private set

    // Está no meio de um login? (para mostrar a rodinha de carregando)
    var isLoading by mutableStateOf(false)
        private set

    // Mensagem de erro para mostrar ao usuário (ou null se está tudo bem).
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // "Ouvinte" do Firebase: sempre que o login/logout acontece, ele
    // atualiza o currentUser sozinho — inclusive quando o app reabre
    // e o Firebase lembra da sessão salva.
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        currentUser = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        // Quando o ViewModel morre, paramos de ouvir (evita vazamento).
        auth.removeAuthStateListener(authListener)
    }

    // Abre a tela "Entrar com Google" e, ao final, autentica no Firebase.
    // "webClientId" é a senha pública que veio do google-services.json.
    fun signInWithGoogle(context: Context, webClientId: String) {
        // viewModelScope.launch = roda em segundo plano, sem travar a tela.
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1) Monta a opção "Entrar com Google".
                //    setFilterByAuthorizedAccounts(false) = mostra TODAS as
                //    contas do celular, não só as que já usaram o app.
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 2) Abre o seletor de contas do Google e espera a escolha.
                val result = CredentialManager.create(context).getCredential(context, request)
                val credential = result.credential

                // 3) Confere se veio mesmo uma credencial do Google e pega o "token".
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleCredential.idToken

                    // 4) Entrega o token do Google ao Firebase para efetivar o login.
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential).await()
                    // O authListener acima já atualiza o currentUser.
                } else {
                    errorMessage = "Tipo de credencial inesperado."
                }
            } catch (e: GetCredentialCancellationException) {
                // O usuário fechou a janela do Google. Não é erro: não avisa nada.
            } catch (e: NoCredentialException) {
                // Não há nenhuma conta Google cadastrada neste aparelho.
                errorMessage = "Nenhuma conta Google neste aparelho. Adicione uma em Configurações → Contas e tente de novo."
                Log.e("AuthViewModel", "Sem conta Google no dispositivo", e)
            } catch (e: Exception) {
                errorMessage = "Não foi possível entrar. Tente novamente."
                Log.e("AuthViewModel", "Falha no login com Google", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Sai da conta: desloga do Firebase e limpa a credencial guardada.
    fun signOut(context: Context) {
        viewModelScope.launch {
            auth.signOut()
            try {
                CredentialManager.create(context)
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Falha ao limpar credencial", e)
            }
        }
    }

    // Exclui a conta e TODOS os dados do usuário (exigência do Google Play).
    // A ORDEM importa para não deixar "sobras":
    //   1) reautentica com uma credencial fresca do Google (o Firebase exige
    //      "login recente" para excluir a conta). Fazemos isto ANTES de apagar
    //      qualquer dado — se o usuário cancelar o seletor de contas aqui,
    //      NADA é perdido.
    //   2) apaga todos os dados no Firestore (ainda logado, senão o uid some).
    //   3) apaga a conta do Firebase Auth (o authListener volta ao login).
    //   4) limpa a credencial guardada no aparelho.
    fun deleteAccount(context: Context, webClientId: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                errorMessage = "Você precisa estar logado para excluir a conta."
                return@launch
            }
            errorMessage = null
            try {
                // 1) Reautenticação obrigatória.
                val credential = freshGoogleCredential(context, webClientId)
                user.reauthenticate(credential).await()

                // 2) Apaga os dados do usuário.
                repo.deleteAllUserData()

                // 3) Apaga a conta de login.
                user.delete().await()

                // 4) Limpa a credencial salva (best-effort).
                try {
                    CredentialManager.create(context)
                        .clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Falha ao limpar credencial", e)
                }
            } catch (e: GetCredentialCancellationException) {
                // Usuário fechou o seletor de contas: cancelou a exclusão.
                // Nada foi apagado — nem dados, nem conta.
            } catch (e: Exception) {
                errorMessage = "Não foi possível excluir a conta. Tente novamente."
                Log.e("AuthViewModel", "Falha ao excluir conta", e)
            }
        }
    }

    // Abre o seletor do Google e devolve uma credencial FRESCA do Firebase,
    // usada só para reautenticar antes de excluir a conta. Filtra pelas contas
    // já autorizadas (mostra a que está logada), evitando pegar outra por engano.
    private suspend fun freshGoogleCredential(
        context: Context,
        webClientId: String,
    ): com.google.firebase.auth.AuthCredential {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val result = CredentialManager.create(context).getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        }
        throw IllegalStateException("Credencial do Google inesperada na reautenticação.")
    }

    // Permite a tela "descartar" a mensagem de erro depois de mostrá-la.
    fun clearError() {
        errorMessage = null
    }
}
