package com.example.divideai.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.databinding.ActivityDashboardBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private val breakdownAdapter = CategoryBreakdownAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvBreakdown.adapter = breakdownAdapter
        configurePieChart()

        viewModel.state.observe(this) { state ->
            val currency = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            val isEmpty = !state.isLoading && state.breakdown.isEmpty()
            binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE

            val hasData = !state.isLoading && state.breakdown.isNotEmpty()
            binding.tvTotalLabel.visibility = if (hasData) View.VISIBLE else View.GONE
            binding.tvTotalAmount.visibility = binding.tvTotalLabel.visibility
            binding.tvChartLabel.visibility = binding.tvTotalLabel.visibility
            binding.pieChart.visibility = binding.tvTotalLabel.visibility
            binding.rvBreakdown.visibility = binding.tvTotalLabel.visibility

            binding.tvTotalAmount.text = currency.format(state.total)
            breakdownAdapter.submitList(state.breakdown)
            if (hasData) renderPie(state)
        }

        viewModel.load()
    }

    private fun configurePieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.TRANSPARENT)
            holeRadius = 58f
            transparentCircleRadius = 62f
            setUsePercentValues(true)
            setEntryLabelColor(android.graphics.Color.WHITE)
            setEntryLabelTextSize(11f)
            legend.isEnabled = false
        }
    }

    private fun renderPie(state: DashboardState) {
        val entries = state.breakdown.map { item ->
            PieEntry(item.total.toFloat(), getString(item.category.labelRes))
        }
        val colors = state.breakdown.map { it.colorArgb }
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 2f
            valueTextSize = 12f
            valueTextColor = android.graphics.Color.WHITE
            valueFormatter = PercentFormatter(binding.pieChart)
        }
        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.animateY(800, Easing.EaseInOutCubic)
        binding.pieChart.invalidate()
    }
}
