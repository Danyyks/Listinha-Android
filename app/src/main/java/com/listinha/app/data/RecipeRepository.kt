package com.listinha.app.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// ============================================================
//  RecipeRepository.kt  ===  o "cérebro" das receitas no banco
// ------------------------------------------------------------
//  Espelha o ShoppingRepository, mas para as receitas. Cada
//  usuário guarda as próprias receitas na sua gaveta:
//    users/{uid}/recipes/{recipeId}
//  As regras de segurança (firestore.rules) já protegem esse
//  caminho — só o dono lê e escreve.
// ============================================================

class RecipeRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val uid: String? get() = auth.currentUser?.uid

    // A subcoleção de receitas do usuário logado.
    private fun recipesCol() = db.collection("users").document(uid!!).collection("recipes")

    // observeRecipes() devolve um fluxo que se ATUALIZA SOZINHO sempre que
    // as receitas mudam no banco (aqui ou em outro aparelho).
    fun observeRecipes(): Flow<List<Recipe>> = callbackFlow {
        val currentUid = uid
        if (currentUid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = db.collection("users").document(currentUid)
            .collection("recipes")
            .orderBy("createdAt") // da mais antiga para a mais nova
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val recipes = snapshot?.documents?.map { Recipe.fromDoc(it) } ?: emptyList()
                trySend(recipes)
            }

        awaitClose { registration.remove() }
    }

    // Cria uma nova receita. Os ingredientes viram uma lista de mapinhas.
    suspend fun addRecipe(recipe: Recipe) {
        recipesCol().add(
            hashMapOf(
                "name" to recipe.name.trim(),
                "servings" to recipe.servings,
                "iconIndex" to recipe.iconIndex,
                "photo" to recipe.photo,
                "ingredients" to recipe.ingredients.map { it.toMap() },
                "steps" to recipe.steps,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    // Edita uma receita existente (não mexe no createdAt).
    suspend fun updateRecipe(id: String, recipe: Recipe) {
        recipesCol().document(id).update(
            mapOf(
                "name" to recipe.name.trim(),
                "servings" to recipe.servings,
                "iconIndex" to recipe.iconIndex,
                "photo" to recipe.photo,
                "ingredients" to recipe.ingredients.map { it.toMap() },
                "steps" to recipe.steps,
            )
        ).await()
    }

    // Remove uma receita.
    suspend fun deleteRecipe(id: String) {
        recipesCol().document(id).delete().await()
    }
}
