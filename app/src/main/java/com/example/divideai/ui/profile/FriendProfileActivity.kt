package com.example.divideai.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.divideai.R
import com.example.divideai.data.image.Base64Image
import com.example.divideai.data.model.User
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.FriendRepository
import com.example.divideai.data.repository.UserRepository
import com.example.divideai.databinding.ActivityFriendProfileBinding
import kotlinx.coroutines.launch

/**
 * Activity responsavel por exibir o perfil de um usuario e gerenciar a relacao de amizade.
 * Permite visualizar os detalhes do usuario, enviar solicitacoes de amizade e desfazer amizades existentes.
 */
class FriendProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendProfileBinding
    private val friendRepository = FriendRepository()
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    private var targetUserId: String? = null
    private var targetUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetUserId = intent.getStringExtra("USER_ID")
        
        setupListeners()

        if (targetUserId != null) {
            loadUserProfile()
        } else {
            Toast.makeText(this, R.string.friend_user_not_found, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Carrega os dados do perfil do usuario alvo a partir do repositorio.
     * Atualiza a interface da tela com o nome do usuario e verifica o status da amizade.
     */
    private fun loadUserProfile() {
        // Ideally we fetch the user directly from the repository
        userRepository.getAllUsers { users ->
            targetUser = users.find { it.id == targetUserId }
            targetUser?.let { user ->
                binding.tvUserName.text = user.name.ifEmpty { user.email.split("@")[0] }
                Base64Image.decode(user.profileImageBase64)?.let { bmp ->
                    binding.ivAvatar.setImageBitmap(bmp)
                }
                checkFriendshipStatus(user.id)
            }
        }
    }

    /**
     * Verifica o status de amizade entre o usuario atual e o usuario alvo.
     * Atualiza a visibilidade dos botoes de adicionar e desfazer amizade com base no resultado.
     *
     * @param otherUserId O ID do usuario cujo status de amizade sera verificado.
     */
    private fun checkFriendshipStatus(otherUserId: String) {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        
        lifecycleScope.launch {
            val friends = friendRepository.getFriends(currentUserId)
            val isFriend = friends.any { it.senderId == otherUserId || it.receiverId == otherUserId }
            
            if (isFriend) {
                binding.btnAddFriend.visibility = View.GONE
                binding.btnUnfriend.visibility = View.VISIBLE
            } else {
                binding.btnAddFriend.visibility = View.VISIBLE
                binding.btnUnfriend.visibility = View.GONE
            }
        }
    }

    /**
     * Configura os event listeners para os botoes da interface, como fechar a tela,
     * adicionar amigo e desfazer amizade.
     */
    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnAddFriend.setOnClickListener {
            targetUser?.let { receiver ->
                val currentUser = authRepository.getCurrentUser() ?: return@setOnClickListener
                val sender = User(id = currentUser.uid, email = currentUser.email ?: "")
                
                lifecycleScope.launch {
                    val result = friendRepository.sendFriendRequest(sender, receiver)
                    if (result.isSuccess) {
                        Toast.makeText(this@FriendProfileActivity, R.string.friend_request_sent, Toast.LENGTH_SHORT).show()
                        binding.btnAddFriend.visibility = View.GONE // Optimistic update
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message
                            ?: getString(R.string.friend_request_generic_error)
                        Toast.makeText(this@FriendProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnUnfriend.setOnClickListener {
            targetUser?.let { receiver ->
                val currentUser = authRepository.getCurrentUser() ?: return@setOnClickListener
                
                lifecycleScope.launch {
                    val result = friendRepository.removeFriendship(currentUser.uid, receiver.id)
                    if (result.isSuccess) {
                        Toast.makeText(this@FriendProfileActivity, R.string.friend_unfriend_success, Toast.LENGTH_SHORT).show()
                        binding.btnUnfriend.visibility = View.GONE
                        binding.btnAddFriend.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@FriendProfileActivity, R.string.friend_unfriend_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
