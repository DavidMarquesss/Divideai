package com.example.divideai.ui.balances

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.R
import com.example.divideai.databinding.ActivitySimplifiedBalancesBinding

/**
 * Mostra ao usuário atual a forma mínima de quitar todas as suas dívidas.
 *
 * Roda o [com.example.divideai.data.balance.DebtSimplifier] em cima de todas
 * as despesas onde o usuário aparece (como pagador ou participante) e exibe
 * o resultado em duas listas: "Você precisa pagar" e "Tem a receber".
 */
class SimplifiedBalancesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimplifiedBalancesBinding
    private val viewModel: SimplifiedBalancesViewModel by viewModels()

    private val youOweAdapter = SimplifiedBalanceAdapter(R.color.strawberry_red)
    private val owedToYouAdapter = SimplifiedBalanceAdapter(R.color.sage_green)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimplifiedBalancesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvYouOwe.adapter = youOweAdapter
        binding.rvOwedToYou.adapter = owedToYouAdapter

        viewModel.state.observe(this) { state ->
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            val nothingToShow = !state.isLoading && state.youOwe.isEmpty() && state.owedToYou.isEmpty()
            binding.tvEmpty.visibility = if (nothingToShow) View.VISIBLE else View.GONE

            val anySection = !state.isLoading && (state.youOwe.isNotEmpty() || state.owedToYou.isNotEmpty())
            binding.tvSubtitle.visibility = if (anySection) View.VISIBLE else View.GONE

            binding.tvSectionYouOwe.visibility = if (state.youOwe.isNotEmpty()) View.VISIBLE else View.GONE
            binding.rvYouOwe.visibility = binding.tvSectionYouOwe.visibility
            binding.tvSectionOwedToYou.visibility = if (state.owedToYou.isNotEmpty()) View.VISIBLE else View.GONE
            binding.rvOwedToYou.visibility = binding.tvSectionOwedToYou.visibility

            youOweAdapter.submitList(state.youOwe)
            owedToYouAdapter.submitList(state.owedToYou)
        }

        viewModel.load()
    }
}
