package com.example.divideai.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.LocaleListCompat
import com.example.divideai.DivideAiApplication
import com.example.divideai.R
import com.example.divideai.databinding.ActivityProfileBinding
import com.example.divideai.ui.auth.LoginActivity
import com.example.divideai.ui.profile.search.SearchUserActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
            val fallbackUser = getString(R.string.default_user)
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: ""
                    binding.tvUserName.text = name.ifEmpty { user.email?.split("@")?.get(0) ?: fallbackUser }
                }
                .addOnFailureListener {
                    binding.tvUserName.text = user.email?.split("@")?.get(0) ?: fallbackUser
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

        binding.btnMore.setOnClickListener { anchor ->
            showProfileMenu(anchor)
        }
    }

    /**
     * Exibe um menu suspenso ancorado no botão de opções da toolbar, com as
     * ações de troca de idioma e logout.
     */
    private fun showProfileMenu(anchor: android.view.View) {
        PopupMenu(this, anchor).apply {
            menuInflater.inflate(R.menu.profile_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_language -> {
                        showLanguageDialog()
                        true
                    }
                    R.id.action_theme -> {
                        showThemeDialog()
                        true
                    }
                    R.id.action_logout -> {
                        viewModel.logout()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    /**
     * Exibe um diálogo para escolher o modo de tema do app (claro / escuro /
     * padrão do sistema). A escolha é persistida via [DivideAiApplication]
     * e aplicada imediatamente — as Activities visíveis são recriadas.
     */
    private fun showThemeDialog() {
        val modes = intArrayOf(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
        )
        val displayNames = resources.getStringArray(R.array.theme_display_names)
        val currentMode = DivideAiApplication.getSavedThemeMode(this)
        val checkedIndex = modes.indexOf(currentMode).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_choose_theme)
            .setSingleChoiceItems(displayNames, checkedIndex) { dialog, which ->
                DivideAiApplication.setThemeMode(this, modes[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    /**
     * Exibe um diálogo para o usuário escolher o idioma do app (per-app language).
     * A seleção é aplicada com [AppCompatDelegate.setApplicationLocales] e persiste
     * entre execuções graças ao AppLocalesMetadataHolderService declarado no manifest.
     */
    private fun showLanguageDialog() {
        // Índice 0 = padrão do sistema; demais seguem a ordem de [languageTags].
        val languageTags = arrayOf("", "pt-BR", "en")
        val displayNames = resources.getStringArray(R.array.language_display_names)

        val currentTag = AppCompatDelegate.getApplicationLocales()
            .takeUnless { it.isEmpty }?.get(0)?.toLanguageTag()
        val checkedIndex = when {
            currentTag == null -> 0
            currentTag.startsWith("pt") -> 1
            currentTag.startsWith("en") -> 2
            else -> 0
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_choose_language)
            .setSingleChoiceItems(displayNames, checkedIndex) { dialog, which ->
                val tag = languageTags[which]
                val locales = if (tag.isEmpty()) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(tag)
                }
                AppCompatDelegate.setApplicationLocales(locales)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
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