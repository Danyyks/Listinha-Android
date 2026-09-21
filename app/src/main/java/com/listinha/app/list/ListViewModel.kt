package com.listinha.app.list

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listinha.app.data.Item
import com.listinha.app.data.Profile
import com.listinha.app.data.ShoppingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// ============================================================
//  ListViewModel.kt  —  o "gerente" da tela da lista
// ------------------------------------------------------------
//  Faz o papel do App.jsx + hook useShoppingList: guarda a
//  lista de itens e oferece as ações (adicionar, marcar, apagar).
//
//  IMPORTANTE: toda ação que fala com a nuvem pode falhar (rede,
//  permissão...). Por isso TUDO passa por "safeLaunch", que
//  captura o erro e apenas registra no log — o app nunca quebra.
// ============================================================

class ListViewModel : ViewModel() {

    private val repo = ShoppingRepository()

    // --- ESTADO observado pela tela ---
    var items by mutableStateOf<List<Item>>(emptyList())
        private set

    var profile by mutableStateOf(Profile())
        private set

    var priceHistory by mutableStateOf<Map<String, Double>>(emptyMap())
        private set

    var budget by mutableStateOf<Double?>(null)
        private set

    // Mensagem de erro para a tela mostrar (ex: falhou ao salvar sem internet).
    // Null = está tudo bem. A tela mostra e depois chama clearError().
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Guarda a "assinatura" atual da lista para podermos CANCELÁ-LA quando
    // o usuário troca (logout + login, ou outra conta no mesmo aparelho).
    private var observeJob: Job? = null

    // Roda um trecho em segundo plano capturando qualquer erro (sem derrubar o
    // app). Agora, além de registrar no log, AVISA o usuário via errorMessage.
    private fun safeLaunch(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                errorMessage = "Não foi possível salvar. Verifique sua conexão e tente de novo."
                Log.e("ListViewModel", "Erro ao falar com o banco", e)
            }
        }
    }

    // A tela chama isto depois de mostrar a mensagem, para não repeti-la.
    fun clearError() {
        errorMessage = null
    }

    // (Re)inicia o ViewModel para o usuário logado AGORA. A tela chama isto
    // sempre que o uid muda (ver LaunchedEffect na HomeScreen). Antes isto
    // ficava no `init` e rodava só uma vez — o que fazia o app mostrar dados
    // da conta ANTERIOR depois de um logout+login. Agora cada conta começa
    // do zero: cancelamos a assinatura antiga, zeramos o estado e reassinamos.
    fun start() {
        // Cancela a assinatura da conta anterior (se houver).
        observeJob?.cancel()

        // Zera o estado para NÃO "vazar" dados de quem estava logado antes.
        items = emptyList()
        profile = Profile()
        priceHistory = emptyMap()
        budget = null

        // "Assina" a lista: sempre que ela muda no banco, atualiza a tela.
        // O .catch impede que um erro no fluxo (ex: permissão) quebre o app.
        observeJob = viewModelScope.launch {
            repo.observeItems()
                .catch { e -> Log.e("ListViewModel", "Erro ao observar itens", e) }
                .collect { novaLista -> items = novaLista }
        }
        // Carrega perfil (orçamento/tema) e histórico de preços.
        safeLaunch {
            profile = repo.getOrCreateProfile()
            budget = profile.budget
        }
        safeLaunch { priceHistory = repo.getPriceHistory() }
    }

    fun addItem(name: String, category: String, price: Double, quantity: Double) = safeLaunch {
        repo.addItem(name, category, price, quantity)
        priceHistory = repo.getPriceHistory()
    }

    fun toggle(item: Item) = safeLaunch { repo.toggleBought(item) }

    fun editItem(id: String, name: String, category: String, price: Double, quantity: Double) = safeLaunch {
        repo.editItem(id, name, category, price, quantity)
        priceHistory = repo.getPriceHistory()
    }

    fun delete(item: Item) = safeLaunch { repo.deleteItem(item.id) }

    fun clearBought() = safeLaunch { repo.clearBought() }

    // Apagar a lista também zera o orçamento (regra do app original).
    fun clearAll() {
        budget = null
        safeLaunch {
            repo.clearAll()
            repo.setBudget(null)
        }
    }

    // Cria a lista a partir de uma receita. Se replace = true, LIMPA a lista
    // antes — e tudo roda EM SEQUÊNCIA dentro de uma única corrotina (primeiro
    // limpa, depois adiciona), evitando a "corrida" que apagava os itens novos.
    fun addItemsFromRecipe(itens: List<Pair<String, String>>, replace: Boolean) = safeLaunch {
        if (replace) {
            repo.clearAll()
            repo.setBudget(null)
            budget = null
        }
        itens.forEach { (nome, categoria) -> repo.addItem(nome, categoria, 0.0, 1.0) }
        priceHistory = repo.getPriceHistory()
    }

    // Define/atualiza o orçamento (nome "update" para não colidir com o
    // setBudget automático que a propriedade 'budget' já gera).
    fun updateBudget(value: Double?) {
        budget = value
        safeLaunch { repo.setBudget(value) }
    }

    // Salva o tema escolhido ("light", "dark" ou "auto") no perfil.
    fun updateTheme(theme: String) = safeLaunch { repo.setTheme(theme) }

    // Apaga todo o histórico de preços do usuário.
    fun clearPriceHistory() = safeLaunch {
        repo.clearPriceHistory()
        priceHistory = emptyMap()
    }
}
