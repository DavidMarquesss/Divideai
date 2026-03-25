package com.example.divideai.ui.groups

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.divideai.R
import com.example.divideai.databinding.FragmentGroupsBinding
import com.example.divideai.ui.groups.form.GroupFormActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GroupsFragment : Fragment() {

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private val groupsViewModel: GroupsViewModel by viewModels()


    private val adapter = GroupsAdapter(
        onItemClick = { group ->
            if (groupsViewModel.isSelectionMode.value == true) {
                groupsViewModel.toggleGroupSelection(group.id)
            } else {
                val intent = Intent(context, GroupActivity::class.java).apply {
                    putExtra("GROUP_ID", group.id)
                    putExtra("GROUP_TITLE", group.title)
                }
                startActivity(intent)
            }
        },
        onLongClick = { group ->
            if (groupsViewModel.isSelectionMode.value == false) {
                groupsViewModel.setSelectionMode(true)
                groupsViewModel.toggleGroupSelection(group.id)
            }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
        handleBackPress()
    }

    override fun onResume() {
        super.onResume()
        groupsViewModel.fetchGroups()
    }

    private fun setupRecyclerView() {
        binding.rvGroups.adapter = adapter
    }

    private fun setupObservers() {
        groupsViewModel.groupList.observe(viewLifecycleOwner) { groups ->
            adapter.submitList(groups)
        }

        groupsViewModel.isSelectionMode.observe(viewLifecycleOwner) { isSelectionMode ->
            updateUIForSelectionMode(isSelectionMode)
            adapter.updateSelectionState(
                isSelectionMode,
                groupsViewModel.selectedIds.value ?: emptySet()
            )
        }

        groupsViewModel.selectedIds.observe(viewLifecycleOwner) { selectedIds ->
            adapter.updateSelectionState(
                groupsViewModel.isSelectionMode.value ?: false,
                selectedIds
            )
            val totalItems = groupsViewModel.groupList.value?.size ?: 0
            binding.chkSelectAll.isChecked = totalItems > 0 && selectedIds.size == totalItems
        }

        groupsViewModel.deleteStatus.observe(viewLifecycleOwner) { status ->
            status?.let { (success, message) ->
                if (success) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
                groupsViewModel.clearDeleteStatus()
            }
        }
    }

    private fun setupListeners() {
        binding.btnAddGroup.setOnClickListener {
            val intent = Intent(context, GroupFormActivity::class.java)
            startActivity(intent)
        }

        binding.chkSelectAll.setOnClickListener {
            groupsViewModel.toggleSelectAll(binding.chkSelectAll.isChecked)
        }

        binding.btnDeleteSelected.setOnClickListener {
            val count = groupsViewModel.selectedIds.value?.size ?: 0

            if (count > 0) {
                val message = resources.getQuantityString(R.plurals.dialog_delete_groups_message, count, count)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_confirmation_title)
                    .setMessage(message)
                    .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.dialog_confirm) { dialog, _ ->
                        groupsViewModel.deleteSelectedGroups()
                    }
                    .show()
            } else {
                Toast.makeText(requireContext(), R.string.error_select_something_to_delete, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.inputSearch.addTextChangedListener { text ->
            groupsViewModel.filterGroups(text.toString())
        }
    }

    private fun updateUIForSelectionMode(isSelectionMode: Boolean) {
        if (isSelectionMode) {
            binding.btnAddGroup.visibility = View.GONE
            binding.layoutSelection.visibility = View.VISIBLE
        } else {
            binding.btnAddGroup.visibility = View.VISIBLE
            binding.layoutSelection.visibility = View.GONE
            binding.chkSelectAll.isChecked = false
        }
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (groupsViewModel.isSelectionMode.value == true) {
                        groupsViewModel.setSelectionMode(false)
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}