package com.listinha.app.recipes

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listinha.app.data.Recipe
import com.listinha.app.data.RecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// ============================================================
//  RecipeViewModel.kt  ===  o "gerente" da tela de receitas
// ------------------------------------------------------------
//  Espelha o ListViewModel: guarda a lista de receitas e oferece
//  as ações (adicionar, editar, excluir). Toda ação passa por
//  "safeLaunch", que captura erros e nunca deixa o app quebrar.
// ============================================================

class RecipeViewModel : ViewModel() {

    private val repo = RecipeRepository()

    // As receitas do usuário, observadas pela tela.
    var recipes by mutableStateOf<List<Recipe>>(emptyList())
        private set

    // Mensagem de erro para a tela mostrar (ex: falhou ao salvar a receita).
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Guarda a "assinatura" atual das receitas para podermos CANCELÁ-LA
    // quando o usuário troca (logout + login, ou outra conta no aparelho).
    private var observeJob: Job? = null

    // Roda em segundo plano capturando qualquer erro. Além do log, AVISA o
    // usuário via errorMessage (a receita não some "calada" se a escrita falhar).
    private fun safeLaunch(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                errorMessage = "Não foi possível salvar. Verifique sua conexão e tente de novo."
                Log.e("RecipeViewModel", "Erro ao falar com o banco", e)
            }
        }
    }

    // A tela chama isto depois de mostrar a mensagem, para não repeti-la.
    fun clearError() {
        errorMessage = null
    }

    // (Re)inicia o ViewModel para o usuário logado AGORA. A tela chama isto
    // sempre que o uid muda (ver LaunchedEffect na RecipesTab). Assim as
    // receitas de uma conta nunca aparecem para a conta seguinte.
    fun start() {
        // Cancela a assinatura da conta anterior (se houver) e zera o estado.
        observeJob?.cancel()
        recipes = emptyList()

        // "Assina" as receitas: qualquer mudança no banco atualiza a tela.
        observeJob = viewModelScope.launch {
            repo.observeRecipes()
                .catch { e -> Log.e("RecipeViewModel", "Erro ao observar receitas", e) }
                .collect { novaLista -> recipes = novaLista }
        }
    }

    fun add(recipe: Recipe) = safeLaunch { repo.addRecipe(recipe) }

    fun update(id: String, recipe: Recipe) = safeLaunch { repo.updateRecipe(id, recipe) }

    fun delete(recipe: Recipe) = safeLaunch { repo.deleteRecipe(recipe.id) }
}
