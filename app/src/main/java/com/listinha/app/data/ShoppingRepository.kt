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
//  ShoppingRepository.kt  —  o "cérebro" que fala com o banco
// ------------------------------------------------------------
//  Este arquivo sozinho substitui, no app Android:
//    - o backend Flask inteiro (Python)
//    - os hooks useShoppingList, useProfile e usePriceHistory
//
//  A ideia é a MESMA de antes: cada usuário tem sua própria
//  "gaveta" no Firestore, isolada pelo uid:
//    users/{uid}                -> perfil (orçamento, tema)
//    users/{uid}/items          -> itens da lista
//    users/{uid}/priceHistory   -> último preço de cada produto
//
//  "suspend" nas funções = elas rodam em segundo plano (esperam
//  a nuvem responder) sem travar a tela. O .await() é quem espera.
// ============================================================

class ShoppingRepository {

    // Atalhos para o banco (Firestore) e o login (Auth).
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // uid = identificador único do usuário logado (vem do login com Google).
    private val uid: String? get() = auth.currentUser?.uid

    // "Gaveta" do usuário e suas subcoleções. Só use quando houver login.
    private fun userDoc() = db.collection("users").document(uid!!)
    private fun itemsCol() = userDoc().collection("items")
    private fun historyCol() = userDoc().collection("priceHistory")

    // --------------------------------------------------------
    //  ITENS DA LISTA
    // --------------------------------------------------------

    // observeItems() devolve um "fluxo" (Flow) que se ATUALIZA SOZINHO.
    // Sempre que a lista muda no banco (aqui ou em outro aparelho), a tela
    // recebe a nova lista na hora. É a grande vantagem sobre o React antigo,
    // que precisava buscar e atualizar tudo na mão.
    fun observeItems(): Flow<List<Item>> = callbackFlow {
        val currentUid = uid
        if (currentUid == null) {
            // Sem login: manda uma lista vazia e encerra.
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        // addSnapshotListener = "me avise toda vez que esses dados mudarem".
        val registration = db.collection("users").document(currentUid)
            .collection("items")
            .orderBy("createdAt") // do mais antigo para o mais novo
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // deu erro: encerra o fluxo avisando
                    return@addSnapshotListener
                }
                // Converte cada documento num Item e envia a lista para a tela.
                val items = snapshot?.documents?.map { Item.fromDoc(it) } ?: emptyList()
                trySend(items)
            }

        // Quando a tela deixa de ouvir (ex: usuário sai), removemos o listener.
        awaitClose { registration.remove() }
    }

    // Cria um novo item. Regra de negócio (igual ao backend): se veio com
    // preço > 0, também registra no histórico de preços.
    suspend fun addItem(name: String, category: String, price: Double, quantity: Double) {
        val data = hashMapOf(
            "name" to name.trim(),
            "category" to category.ifBlank { "outros" },
            "price" to price,
            "quantity" to quantity,
            "bought" to false,
            // serverTimestamp = o próprio servidor anota a hora exata,
            // sem confiar no relógio do celular.
            "createdAt" to FieldValue.serverTimestamp(),
        )
        itemsCol().add(data).await()
        upsertPriceHistory(name, price)
    }

    // Marca/desmarca um item como comprado.
    suspend fun toggleBought(item: Item) {
        itemsCol().document(item.id).update("bought", !item.bought).await()
    }

    // Edita um item por completo (nome, categoria, preço, quantidade).
    suspend fun editItem(id: String, name: String, category: String, price: Double, quantity: Double) {
        itemsCol().document(id).update(
            mapOf(
                "name" to name.trim(),
                "category" to category,
                "price" to price,
                "quantity" to quantity,
            )
        ).await()
        // Preço/nome novos também atualizam o histórico.
        upsertPriceHistory(name, price)
    }

    // Remove um item da lista.
    suspend fun deleteItem(id: String) {
        itemsCol().document(id).delete().await()
    }

    // Apaga todos os itens já marcados como comprados.
    suspend fun clearBought() {
        deleteDocuments(itemsCol().whereEqualTo("bought", true).get().await().documents)
    }

    // Apaga TODOS os itens (recomeçar do zero).
    suspend fun clearAll() {
        deleteDocuments(itemsCol().get().await().documents)
    }

    // Apaga uma lista de documentos EM LOTES de no máximo 450.
    // Por quê 450 e não tudo de uma vez? O WriteBatch do Firestore aceita no
    // máximo 500 operações por commit; passar disso dá erro e a exclusão para
    // no meio. Quebrando em lotes, funciona mesmo com milhares de documentos.
    private suspend fun deleteDocuments(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        documents.chunked(450).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    // Apaga TODOS os dados do usuário: itens, histórico de preços, receitas
    // e o próprio perfil. Usado no "Excluir minha conta" (exigência do Google
    // Play para apps com login).
    suspend fun deleteAllUserData() {
        if (uid == null) return
        clearCollection(itemsCol())
        clearCollection(historyCol())
        clearCollection(userDoc().collection("recipes"))
        userDoc().delete().await()
    }

    // Apaga todos os documentos de uma coleção (em lotes seguros de 450).
    private suspend fun clearCollection(col: com.google.firebase.firestore.CollectionReference) {
        deleteDocuments(col.get().await().documents)
    }

    // --------------------------------------------------------
    //  PERFIL (orçamento e tema)
    // --------------------------------------------------------

    // Busca o perfil; se for o primeiro acesso, cria usando os dados que o
    // login com Google já trouxe (nome, foto, e-mail). Igual ao backend.
    suspend fun getOrCreateProfile(): Profile {
        val ref = userDoc()
        val snapshot = ref.get().await()

        if (!snapshot.exists()) {
            val user = auth.currentUser
            ref.set(
                hashMapOf(
                    "email" to user?.email,
                    "displayName" to user?.displayName,
                    "photoURL" to user?.photoUrl?.toString(),
                    "budget" to null,
                    "theme" to "auto",
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            return getOrCreateProfile() // relê o que acabou de criar
        }

        return Profile(
            email = snapshot.getString("email"),
            displayName = snapshot.getString("displayName"),
            photoURL = snapshot.getString("photoURL"),
            budget = snapshot.getDouble("budget"),
            theme = snapshot.getString("theme") ?: "auto",
        )
    }

    // Salva o orçamento. Aceita null para "zerar" (usado ao apagar a lista).
    // merge = true -> mexe só neste campo, sem apagar o resto do perfil.
    suspend fun setBudget(budget: Double?) {
        userDoc().set(mapOf("budget" to budget), com.google.firebase.firestore.SetOptions.merge()).await()
    }

    // Salva o tema escolhido ("light", "dark" ou "auto").
    suspend fun setTheme(theme: String) {
        userDoc().set(mapOf("theme" to theme), com.google.firebase.firestore.SetOptions.merge()).await()
    }

    // --------------------------------------------------------
    //  HISTÓRICO DE PREÇOS
    // --------------------------------------------------------

    // Devolve o histórico como um mapa { nomeNormalizado -> preço }.
    // Serve para sugerir o último preço quando o usuário digita um produto.
    suspend fun getPriceHistory(): Map<String, Double> {
        val snapshot = historyCol().get().await()
        return snapshot.documents
            .mapNotNull { doc ->
                val key = doc.getString("nameKey")
                val price = doc.getDouble("price")
                if (key != null && price != null) key to price else null
            }
            .toMap()
    }

    // Cria ou atualiza o preço de um produto pelo nome.
    // Mesma lógica do backend: normaliza o nome (minúsculo, sem espaços)
    // e procura se já existe; se sim, atualiza; se não, cria.
    suspend fun upsertPriceHistory(name: String, price: Double) {
        if (name.isBlank() || price <= 0) return

        val key = name.trim().lowercase()
        val existing = historyCol().whereEqualTo("nameKey", key).limit(1).get().await()

        if (!existing.isEmpty) {
            existing.documents[0].reference.update(
                mapOf(
                    "price" to price,
                    "name" to name,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        } else {
            historyCol().add(
                hashMapOf(
                    "nameKey" to key,
                    "name" to name,
                    "price" to price,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }

    // Apaga todo o histórico de preços do usuário.
    suspend fun clearPriceHistory() {
        deleteDocuments(historyCol().get().await().documents)
    }
}
