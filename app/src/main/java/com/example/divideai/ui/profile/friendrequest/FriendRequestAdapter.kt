package com.example.divideai.ui.profile.friendrequest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.data.model.FriendRequest
import com.example.divideai.databinding.ItemUserRequestBinding

/**
 * Adapter dedicado a exibir a lista de solicitacoes de amizade recebidas pelo usuario.
 * Fornece botoes para aceitar ou recusar cada solicitacao diretamente na interface.
 *
 * @param onAccept Callback disparado ao aceitar a solicitacao.
 * @param onReject Callback disparado ao recusar a solicitacao.
 */
class FriendRequestAdapter(
    private val onAccept: (FriendRequest) -> Unit,
    private val onReject: (FriendRequest) -> Unit
) : ListAdapter<FriendRequest, FriendRequestAdapter.RequestViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemUserRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder que gerencia a exibicao de uma unica solicitacao pendente.
     */
    inner class RequestViewHolder(private val binding: ItemUserRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        
        /**
         * Preenche os dados visuais do remetente (nome e email) e atrela as acoes
         * de aceitacao/recusa aos respectivos botoes.
         *
         * @param request A entidade [FriendRequest] correspondente a este item.
         */
        fun bind(request: FriendRequest) {
            binding.tvUserName.text = request.senderName.ifEmpty { request.senderEmail.split("@")[0] }
            binding.tvUserEmail.text = request.senderEmail

            binding.btnAccept.setOnClickListener { onAccept(request) }
            binding.btnReject.setOnClickListener { onReject(request) }
        }
    }

    /**
     * Callback utilitario para o ListAdapter processar atualizacoes de lista de forma 
     * mais eficiente, checando identificadores unicos e igualdade de atributos.
     */
    class RequestDiffCallback : DiffUtil.ItemCallback<FriendRequest>() {
        override fun areItemsTheSame(oldItem: FriendRequest, newItem: FriendRequest) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FriendRequest, newItem: FriendRequest) = oldItem == newItem
    }
}