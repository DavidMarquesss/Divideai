package com.example.divideai.ui.profile.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.data.image.loadUserAvatar
import com.example.divideai.data.model.FriendRequest
import com.example.divideai.databinding.ItemUserListBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Adapter para a lista de amigos e solicitacoes de amizade.
 * Responsavel por vincular os dados de relacionamento a visualizacao na lista (RecyclerView).
 *
 * @param onItemClick Callback acionado quando um item da lista e clicado, recebendo o ID do outro usuario.
 */
class FriendsAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<FriendRequest, FriendsAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemUserListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder que representa um item individual na lista de relacionamentos (amigos/solicitacoes).
     */
    inner class FriendViewHolder(private val binding: ItemUserListBinding) : RecyclerView.ViewHolder(binding.root) {
        
        /**
         * Associa os dados de uma solicitacao de amizade (ou amizade existente) a view correspondente.
         * Exibe o nome e email corretos dependendo se o usuario atual foi o remetente ou o destinatario.
         *
         * @param request O objeto que contem os dados da solicitacao ou amizade.
         */
        fun bind(request: FriendRequest) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            var otherUserId = ""

            // If I sent the request, show the receiver's name. If I received it, show sender's name.
            if (request.senderId == currentUserId) {
                binding.tvUserName.text = request.receiverName.ifEmpty { request.receiverEmail.split("@")[0] }
                binding.tvUserEmail.text = request.receiverEmail
                otherUserId = request.receiverId
            } else {
                binding.tvUserName.text = request.senderName.ifEmpty { request.senderEmail.split("@")[0] }
                binding.tvUserEmail.text = request.senderEmail
                otherUserId = request.senderId
            }

            // Hide the Add button since they are already friends
            binding.btnAddFriend.visibility = android.view.View.GONE
            binding.ivAvatar.loadUserAvatar(otherUserId)

            binding.root.setOnClickListener {
                onItemClick(otherUserId)
            }
        }
    }

    /**
     * Callback utilitario para calcular as diferencas entre duas listas de amigos,
     * permitindo que o RecyclerView atualize apenas os itens que sofreram alteracoes.
     */
    class FriendDiffCallback : DiffUtil.ItemCallback<FriendRequest>() {
        override fun areItemsTheSame(oldItem: FriendRequest, newItem: FriendRequest) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FriendRequest, newItem: FriendRequest) = oldItem == newItem
    }
}