package com.example.divideai.ui.groups

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.divideai.R
import com.example.divideai.databinding.ActivityGroupBinding // Assumindo que o nome do XML é activity_group.xml
import com.example.divideai.ui.groups.details.GroupDetailsActivity
import com.example.divideai.ui.groups.expenses.GroupExpensesFragment
import com.example.divideai.ui.groups.members.GroupMembersFragment
import com.google.android.material.tabs.TabLayout

class GroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ao abrir a tela, fragmento de despesas é o default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GroupExpensesFragment())
                .commit()
        }

        val groupTitle = intent.getStringExtra("GROUP_TITLE") ?: ""
        binding.toolbar.title = groupTitle

        setupTabs()
        setupListeners()
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // alterna entre os fragments
                when (tab?.position) {
                    0 -> supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, GroupExpensesFragment())
                        .commit()

                    1 -> supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, GroupMembersFragment())
                        .commit()


                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        // botao de voltar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // botao de informacoes
        binding.btnInfo.setOnClickListener {
            val groupId = intent.getStringExtra("GROUP_ID")

            val intentDetails = Intent(this, GroupDetailsActivity::class.java).apply {
                putExtra("GROUP_ID", groupId)
            }
            startActivity(intentDetails)
        }
    }

}