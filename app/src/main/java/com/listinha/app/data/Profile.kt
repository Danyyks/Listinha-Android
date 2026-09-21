package com.listinha.app.data

// ============================================================
//  Profile.kt  —  o perfil do usuário
// ------------------------------------------------------------
//  Guarda os dados que ficam em users/{uid} no Firestore.
//  Nome, foto e e-mail vêm prontos do login com Google.
//  Orçamento (budget) e tema o usuário escolhe dentro do app.
// ============================================================

data class Profile(
    val email: String? = null,
    val displayName: String? = null,
    val photoURL: String? = null,
    val budget: Double? = null,   // orçamento; null = ainda não definido
    val theme: String = "auto",   // "light", "dark" ou "auto"
)
