package com.example.divideai.ui.profile.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divideai.databinding.FragmentFriendListBinding

/**
 * Fragmento que exibe a lista de amigos aceitos pelo usuario.
 * Utiliza o [FriendsViewModel] compartilhado com a atividade para carregar
 * e observar os dados dos amigos e controlar o estado de carregamento.
 */
class FriendListFragment : Fragment() {

    private var _binding: FragmentFriendListBinding? = null
    private val binding get() = _binding!!
    
    // Using activityViewModels to share the ViewModel with ProfileActivity
    private val viewModel: FriendsViewModel by activityViewModels()
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFriendListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    /**
     * Configura o RecyclerView, definindo seu LayoutManager e instanciando
     * o [FriendsAdapter] com o callback de clique que abre o [FriendProfileActivity].
     */
    private fun setupRecyclerView() {
        friendsAdapter = FriendsAdapter { userId ->
            val intent = android.content.Intent(requireContext(), com.example.divideai.ui.profile.FriendProfileActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriends.adapter = friendsAdapter
    }

    /**
     * Configura os observadores do ViewModel para atualizar a lista do adapter quando
     * os dados mudam e para gerenciar a visibilidade da barra de progresso e texto de lista vazia.
     */
    private fun setupObservers() {
        viewModel.friends.observe(viewLifecycleOwner) { friends ->
            friendsAdapter.submitList(friends)
            binding.tvEmptyFriends.visibility = if (friends.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
