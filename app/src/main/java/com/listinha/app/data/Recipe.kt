package com.listinha.app.data

import com.google.firebase.firestore.DocumentSnapshot

// ============================================================
//  Recipe.kt  ===  o modelo de uma RECEITA
// ------------------------------------------------------------
//  Guarda nome, porções, um ícone (índice 0..3), os ingredientes
//  e o modo de preparo. Cada ingrediente sabe a sua CATEGORIA
//  (as mesmas chaves de Category.kt) — é isso que vai permitir
//  jogar tudo na lista de compras já agrupado.
//
//  Fica salvo no Firestore em: users/{uid}/recipes/{recipeId}
//  (os ingredientes vão como uma lista de mapinhas dentro do doc).
// ============================================================

// Um ingrediente da receita.
data class RecipeIngredient(
    val name: String = "",         // ex: "Peito de frango"
    val category: String = "outros", // chave de categoria (ex: "carnes")
    val quantity: String = "",     // texto livre (ex: "500 g", "2 dentes")
) {
    // Vira um mapinha para salvar dentro do documento no Firestore.
    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "category" to category,
        "quantity" to quantity,
    )
}

// A receita em si.
data class Recipe(
    val id: String = "",               // ID do documento no Firestore
    val name: String = "",
    val servings: Int = 1,             // porções que rende
    val iconIndex: Int = 0,            // qual miniatura (usada quando não há foto)
    val photo: String = "",            // foto compactada (base64) ou "" se não tiver
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<String> = emptyList(), // passos do modo de preparo
    val createdAt: Long = 0L,          // quando foi criada (ms desde 1970)
) {
    // Quantos itens (ingredientes) a receita tem — usado no card.
    val itemCount: Int get() = ingredients.size

    companion object {
        // Converte um documento cru do Firestore numa Recipe "arrumada".
        fun fromDoc(doc: DocumentSnapshot): Recipe {
            // Os ingredientes vêm como uma lista de mapinhas: converte um a um.
            val ingredientsRaw = doc.get("ingredients") as? List<*> ?: emptyList<Any>()
            val ingredients = ingredientsRaw.mapNotNull { raw ->
                val m = raw as? Map<*, *> ?: return@mapNotNull null
                RecipeIngredient(
                    name = m["name"] as? String ?: "",
                    category = m["category"] as? String ?: "outros",
                    quantity = m["quantity"] as? String ?: "",
                )
            }
            // Os passos vêm como uma lista de textos.
            val stepsRaw = doc.get("steps") as? List<*> ?: emptyList<Any>()
            val steps = stepsRaw.mapNotNull { it as? String }

            return Recipe(
                id = doc.id,
                name = doc.getString("name") ?: "",
                servings = (doc.getLong("servings") ?: 1L).toInt(),
                iconIndex = (doc.getLong("iconIndex") ?: 0L).toInt(),
                photo = doc.getString("photo") ?: "",
                ingredients = ingredients,
                steps = steps,
                createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
            )
        }
    }
}
