package com.listinha.app.debug

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

// ============================================================
//  EmulatorBootProvider.kt  —  roda o app REAL contra o Emulator Suite
// ------------------------------------------------------------
//  SÓ EXISTE NA BUILD DE DEBUG (src/debug). Não vai para a loja.
//
//  Como funciona:
//   1) Só liga se existir o arquivo  files/emulator_mode  dentro do app.
//      (sem o arquivo, o app de debug se comporta como sempre)
//      Criar:  adb shell run-as com.danyks.listinha sh -c 'echo "teste@listinha.test" > files/emulator_mode'
//   2) Inicia o FirebaseApp PADRÃO com opções FALSAS (projeto demo-listinha,
//      chave de API inventada), antes do FirebaseInitProvider. Assim o
//      google-services.json de produção nem é lido.
//   3) Aponta Auth e Firestore para 10.0.2.2 (o PC do emulador) e entra com
//      um usuário falso criado no Auth Emulator. Nunca usa a sessão do Google.
// ============================================================

class EmulatorBootProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        val marker = File(ctx.filesDir, "emulator_mode")
        if (!marker.exists()) return false

        val partes = marker.readText().trim().split("|")
        val email = partes.getOrNull(0)?.ifBlank { null } ?: "teste@listinha.test"

        // Opções falsas: nenhum dado de produção.
        if (FirebaseApp.getApps(ctx).none { it.name == FirebaseApp.DEFAULT_APP_NAME }) {
            FirebaseApp.initializeApp(
                ctx,
                FirebaseOptions.Builder()
                    .setProjectId(PROJETO_FALSO)
                    .setApplicationId("1:000000000000:android:0000000000000000")
                    .setApiKey("chave-falsa-de-teste")
                    .build(),
            )
        }
        val app = FirebaseApp.getInstance()
        check(app.options.projectId == PROJETO_FALSO) { "FirebaseApp padrão não é o projeto falso: ${app.options.projectId}" }

        val auth = FirebaseAuth.getInstance(app)
        val db = FirebaseFirestore.getInstance(app)
        auth.useEmulator(HOST, 9099)
        db.useEmulator(HOST, 8080)
        Log.w(TAG, "EMULADOR LIGADO projeto=${app.options.projectId} firestore=${db.firestoreSettings.host} ssl=${db.firestoreSettings.isSslEnabled} auth=$HOST:9099")

        // Troca de identidade: se o usuário atual não for o do arquivo, sai e entra no certo.
        val atual = auth.currentUser
        if (atual != null && atual.email != email) auth.signOut()
        if (auth.currentUser == null) {
            auth.signInWithEmailAndPassword(email, SENHA_FALSA).addOnFailureListener {
                // Se a conta ainda não existe, cria sem nome (contas com nome são criadas
                // antes, pelo script de teste, direto na API do Auth Emulator).
                auth.createUserWithEmailAndPassword(email, SENHA_FALSA)
                    .addOnFailureListener { Log.e(TAG, "Falha ao criar usuário falso", it) }
            }
        }
        return true
    }

    override fun query(u: Uri, p: Array<String>?, s: String?, a: Array<String>?, o: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, a: Array<String>?): Int = 0
    override fun update(uri: Uri, v: ContentValues?, s: String?, a: Array<String>?): Int = 0

    private companion object {
        const val TAG = "EmuBoot"
        const val PROJETO_FALSO = "demo-listinha"
        const val HOST = "10.0.2.2"
        const val SENHA_FALSA = "senha-de-teste-local-123" // existe só no Auth Emulator
    }
}
