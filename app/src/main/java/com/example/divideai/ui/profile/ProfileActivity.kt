package com.example.divideai.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.R
import com.example.divideai.databinding.ActivityProfileBinding
import com.example.divideai.ui.auth.LoginActivity
import com.example.divideai.ui.profile.search.SearchUserActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.example.divideai.data.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity principal de perfil do usuario.
 * Exibe as abas de Amigos e Solicitacoes usando um ViewPager2 e um TabLayout.
 * Fornece acesso a edicao de perfil e busca de outros usuarios.
 */
class ProfileActivity : AppCompatActivity(){
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadUserProfile()
        setupTabs()
        setupListeners()
        observeViewModel()
    }

    /**
     * Configura a barra de ferramentas (toolbar) customizada, ocultando
     * o titulo padrao do sistema para utilizar o layout definido no XML.
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        // Oculta o título padrão da ToolBar para usarmos a AppBar desenhada
        supportActionBar?.setDisplayShowTitleEnabled(false) 
    }

    /**
     * Busca os dados basicos do usuario atual no Firestore, focado
     * principalmente em exibir o nome de exibicao na interface.
     */
    private fun loadUserProfile() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            // Buscamos o nome completo direto do Firestore para aparecer bonito
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: ""
                    binding.tvUserName.text = name.ifEmpty { user.email?.split("@")?.get(0) ?: "Usuário" }
                }
                .addOnFailureListener {
                    binding.tvUserName.text = user.email?.split("@")?.get(0) ?: "Usuário"
                }
        }
    }

    /**
     * Prepara as abas de navegacao (Amigos e Solicitacoes) vinculando
     * o ViewPager com o TabLayout por meio do TabLayoutMediator.
     */
    private fun setupTabs() {
        val pagerAdapter = ProfilePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.friends)
                1 -> getString(R.string.requests)
                else -> ""
            }
        }.attach()
    }

    /**
     * Configura ouvintes de clique para os botoes de navegacao principais,
     * como pesquisa, edicao e retorno (voltar).
     */
    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchUserActivity::class.java))
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, com.example.divideai.ui.profile.EditProfileActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                viewModel.logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Observa o estado global (ex: processo de logout) emitido pelo [ProfileViewModel].
     * Em caso de logout bem sucedido, redireciona o usuario para a tela de Login.
     */
    private fun observeViewModel() {
        viewModel.logoutState.observe(this) { state ->
            when (state) {
                is ProfileViewModel.LogoutState.Success -> {
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}