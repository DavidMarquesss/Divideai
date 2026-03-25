package com.example.divideai.ui.profile.search

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divideai.databinding.ActivitySearchUserBinding

/**
 * Activity responsavel por buscar outros usuarios registrados na plataforma.
 * Apresenta uma barra de pesquisa e a lista de resultados, delegando
 * as acoes de adicao de amigo ao viewModel e adaptadores.
 */
class SearchUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchUserBinding
    private val viewModel: SearchUserViewModel by viewModels()
    private lateinit var adapter: SearchUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    /**
     * Inicializa a lista de usuarios com o [SearchUserAdapter], estipulando os
     * callbacks acionados quando se clica em um usuario ou se tenta adiciona-lo.
     */
    private fun setupRecyclerView() {
        adapter = SearchUserAdapter(
            onAddFriendClick = { user -> viewModel.sendFriendRequest(user) },
            onItemClick = { user ->
                val intent = android.content.Intent(this, com.example.divideai.ui.profile.FriendProfileActivity::class.java)
                intent.putExtra("USER_ID", user.id)
                startActivity(intent)
            }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
    }

    /**
     * Define o comportamento dos elementos interativos, principalmente o campo
     * de busca (TextWatcher) cujo texto aciona procuras no banco de dados.
     */
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnClear.setOnClickListener {
            binding.etSearchQuery.text?.clear()
            viewModel.searchUsers("")
        }

        // Optional: Search while typing
        binding.etSearchQuery.addTextChangedListener { text ->
            val query = text.toString()
            if (query.length >= 3) {
                viewModel.searchUsers(query)
            } else if (query.isEmpty()) {
                viewModel.searchUsers("")
            }
        }
    }

    /**
     * Observa as variaveis expostas pelo ViewModel, repassando listas de usuarios para
     * a interface, gerenciando loading e exibindo avisos no caso de envio de solicitacao.
     */
    private fun setupObservers() {
        viewModel.searchResults.observe(this) { users ->
            adapter.submitList(users)
            binding.tvEmptyState.visibility = if (users.isEmpty() && binding.etSearchQuery.text.toString().length >= 3) 
                android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.requestStatus.observe(this) { status ->
            status?.let { (success, message) ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                viewModel.clearStatus()
                
                // If success, we might want to clear the search or update UI. For now, just toast.
                if (success) {
                    binding.etSearchQuery.text?.clear()
                    viewModel.searchUsers("") // clear list
                }
            }
        }
    }
}