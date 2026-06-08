package com.example.divideai.ui.receivables

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divideai.databinding.FragmentReceivablesBinding
import com.example.divideai.ui.balances.SimplifiedBalancesActivity
import com.google.android.material.transition.MaterialFadeThrough

class ReceivablesFragment : Fragment() {

    private var _binding: FragmentReceivablesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReceivablesViewModel by viewModels()
    private lateinit var adapter: ReceivablesUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReceivablesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        binding.btnSimplify.setOnClickListener {
            startActivity(Intent(requireContext(), SimplifiedBalancesActivity::class.java))
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadReceivableUsers() }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadReceivableUsers()
    }

    private fun setupRecyclerView() {
        adapter = ReceivablesUserAdapter { debtorId, debtorName ->
            val intent = Intent(requireContext(), UserReceivablesActivity::class.java).apply {
                putExtra("DEBTOR_ID", debtorId)
                putExtra("DEBTOR_NAME", debtorName)
            }
            startActivity(intent)
        }
        binding.rvReceivables.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReceivables.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.receivableUsers.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.layoutEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = false
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