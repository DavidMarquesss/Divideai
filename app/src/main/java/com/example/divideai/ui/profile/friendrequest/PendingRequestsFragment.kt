package com.example.divideai.ui.profile.friendrequest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divideai.databinding.FragmentPendingRequestsBinding
import com.example.divideai.ui.profile.friends.FriendsViewModel

/**
 * Fragmento responsavel por exibir todas as solicitacoes de amizade que o usuario
 * logado recebeu e que ainda estao pendentes (aguardando aprovacao ou recusa).
 */
class PendingRequestsFragment : Fragment() {

    private var _binding: FragmentPendingRequestsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FriendsViewModel by activityViewModels()
    private lateinit var requestsAdapter: FriendRequestAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPendingRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        // FriendsViewModel loadData handles both friends and requests
        viewModel.loadData()
    }

    /**
     * Instancia o adapter de solicitacoes configurando os lambdas de aceitar e recusar
     * para retransmitir a decisao ao [FriendsViewModel].
     */
    private fun setupRecyclerView() {
        requestsAdapter = FriendRequestAdapter(
            onAccept = { request -> viewModel.respondToRequest(request.id, true) },
            onReject = { request -> viewModel.respondToRequest(request.id, false) }
        )
        binding.rvFriendRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriendRequests.adapter = requestsAdapter
    }

    /**
     * Observa as variaveis do viewModel e reage mudando a lista do adapter,
     * ocultando/mostrando listagens vazias e barras de carregamento de precessamento.
     */
    private fun setupObservers() {
        viewModel.pendingRequests.observe(viewLifecycleOwner) { requests ->
            requestsAdapter.submitList(requests)
            binding.tvEmptyRequests.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
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
