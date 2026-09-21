package com.listinha.app.data

import com.google.firebase.firestore.DocumentSnapshot

// ============================================================
//  Item.kt  —  o modelo de UM item da lista de compras
// ------------------------------------------------------------
//  "data class" = uma classe feita para GUARDAR dados. O Kotlin
//  já cria sozinho coisas como comparação e cópia. É o mesmo
//  "shape" do item que seu app React usa hoje:
//    { id, name, category, price, quantity, bought, createdAt }
// ============================================================

data class Item(
    val id: String = "",              // ID do documento no Firestore
    val name: String = "",            // nome do produto (ex: "Leite")
    val category: String = "outros",  // chave da categoria (ex: "frutas")
    val price: Double = 0.0,          // preço unitário (0 se não definido)
    val quantity: Double = 1.0,       // quantidade (padrão 1)
    val bought: Boolean = false,      // já foi comprado?
    val createdAt: Long = 0L,         // quando foi criado (ms desde 1970)
) {
    // O total deste item = preço x quantidade. É um "campo calculado":
    // não fica salvo no banco, o Kotlin calcula na hora que a gente pede.
    val total: Double get() = price * quantity

    companion object {
        // Converte um documento cru do Firestore num Item "arrumado".
        // Fazemos manualmente (em vez do automático) para converter o
        // createdAt de Timestamp do Firestore para milissegundos —
        // exatamente como o backend Python fazia antes.
        fun fromDoc(doc: DocumentSnapshot): Item {
            return Item(
                id = doc.id,
                name = doc.getString("name") ?: "",
                category = doc.getString("category") ?: "outros",
                price = doc.getDouble("price") ?: 0.0,
                quantity = doc.getDouble("quantity") ?: 1.0,
                bought = doc.getBoolean("bought") ?: false,
                // getTimestamp devolve a hora do servidor; toDate().time = ms desde 1970.
                createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
            )
        }
    }
}
