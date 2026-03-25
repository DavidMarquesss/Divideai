package com.example.divideai.ui.profile.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.data.model.User
import com.example.divideai.databinding.ItemUserListBinding

/**
 * Adapter para a lista de usuarios resultantes de uma pesquisa.
 * Vincula os dados dos usuarios encontrados aos itens visuais da lista no RecyclerView.
 *
 * @param onAddFriendClick Callback acionado ao clicar no botao de adicionar amigo, recebendo o usuario selecionado.
 * @param onItemClick Callback acionado ao clicar no corpo do item, recebendo o usuario selecionado.
 */
class SearchUserAdapter(
    private val onAddFriendClick: (User) -> Unit,
    private val onItemClick: (User) -> Unit
) : ListAdapter<User, SearchUserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder que representa um usuario individual nos resultados da busca.
     */
    inner class UserViewHolder(private val binding: ItemUserListBinding) : RecyclerView.ViewHolder(binding.root) {
        
        /**
         * Preenche a interface grafica do item com as informacoes do usuario (nome e email),
         * e configura os eventos de clique.
         *
         * @param user O objeto contendo os dados do usuario a ser exibido.
         */
        fun bind(user: User) {
            binding.tvUserName.text = user.name.ifEmpty { "Sem Nome" }
            binding.tvUserEmail.text = user.email

            binding.btnAddFriend.setOnClickListener {
                onAddFriendClick(user)
            }

            binding.root.setOnClickListener {
                onItemClick(user)
            }
        }
    }

    /**
     * Callback utilitario utilizado para otimizar as atualizacoes e animacoes da lista de usuarios,
     * verificando se os itens da lista representam o mesmo usuario e possuem o mesmo conteudo.
     */
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }
}
