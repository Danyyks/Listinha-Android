# ============================================================
#  Regras do R8  —  otimização da versão da loja
# ------------------------------------------------------------
#  O app não usa reflexão no próprio código: os dados do Firestore
#  são lidos e gravados campo a campo. As regras abaixo cuidam do
#  que as bibliotecas precisam encontrar pelo nome.
# ============================================================

# Mantém o número da linha nos relatórios de falha do Android vitals.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Login com Google: o Credential Manager carrega o provedor do Play Services pelo nome.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
-keep class com.google.android.libraries.identity.googleid.** { *; }
