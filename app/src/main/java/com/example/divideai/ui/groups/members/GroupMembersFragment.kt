package com.example.divideai.ui.groups.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.divideai.R
import com.example.divideai.data.model.Member
import com.example.divideai.data.model.User
import com.example.divideai.databinding.FragmentGroupMembersBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GroupMembersFragment : Fragment() {

    private var _binding: FragmentGroupMembersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroupMembersViewModel by viewModels()
    private var groupId: String = ""

    private val adapter = MembersAdapter(
        onItemClick = { member ->
            if (viewModel.isSelectionMode.value == true) {
                viewModel.toggleSelection(member.id)
            } else {
                // TODO: Navegar para detalhes do membro futuramente
                Toast.makeText(requireContext(), R.string.member_details_not_implemented, Toast.LENGTH_SHORT).show()
            }
        },
        onLongClick = { member ->
            if (viewModel.isSelectionMode.value == false) {
                viewModel.setSelectionMode(true)
                viewModel.toggleSelection(member.id)
            }
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroupMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = requireActivity().intent.getStringExtra("GROUP_ID") ?: ""

        setupRecyclerView()
        setupObservers()
        setupListeners()
        handleBackPress()
    }

    override fun onResume() {
        super.onResume()
        if (groupId.isNotEmpty()) viewModel.fetchMembers(groupId)
    }

    private fun setupRecyclerView() {
        binding.rvMembers.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.memberList.observe(viewLifecycleOwner) { members ->
            adapter.submitList(members)
        }

        viewModel.isSelectionMode.observe(viewLifecycleOwner) { isSelectionMode ->
            updateUIForSelectionMode(isSelectionMode)
            adapter.updateSelectionState(isSelectionMode, viewModel.selectedIds.value ?: emptySet())
        }

        viewModel.selectedIds.observe(viewLifecycleOwner) { selectedIds ->
            adapter.updateSelectionState(viewModel.isSelectionMode.value ?: false, selectedIds)
            val totalItems = viewModel.memberList.value?.size ?: 0
            binding.chkSelectAll.isChecked = totalItems > 0 && selectedIds.size == totalItems
        }

        viewModel.deleteStatus.observe(viewLifecycleOwner) { status ->
            status?.let { (success, message) ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearDeleteStatus()
            }
        }

        viewModel.availableUsers.observe(viewLifecycleOwner) { users ->
            if (users != null) {
                if (users.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.all_users_already_in_group, Toast.LENGTH_SHORT).show()
                } else {
                    showAddMemberDialog(users)
                }
                viewModel.clearAvailableUsers()
            }
        }

        viewModel.addStatus.observe(viewLifecycleOwner) { status ->
            status?.let { (success, message) ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearAddStatus()
            }
        }
    }

    private fun setupListeners() {
        binding.btnAddMember.setOnClickListener {
            viewModel.fetchAvailableUsers(groupId)
        }

        binding.chkSelectAll.setOnClickListener {
            viewModel.toggleSelectAll(binding.chkSelectAll.isChecked)
        }

        binding.btnDeleteSelected.setOnClickListener {
            val count = viewModel.selectedIds.value?.size ?: 0
            if (count > 0) {
                val message = resources.getQuantityString(R.plurals.dialog_remove_members_message, count, count)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_remove_members_title)
                    .setMessage(message)
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                        viewModel.deleteSelectedMembers(groupId)
                    }
                    .show()
            }
        }

        binding.inputSearch.addTextChangedListener { text ->
            viewModel.filterMembers(text.toString())
        }
    }

    private fun showAddMemberDialog(users: List<User>) {
        val userNames = users.map { it.name.ifEmpty { it.email } }.toTypedArray()
        val checkedItems = BooleanArray(users.size) { false }
        val selectedUsers = mutableListOf<User>()
        MaterialAlertDialogBuilder(requireContext())

            .setTitle(R.string.dialog_add_users_title)
            .setMultiChoiceItems(userNames, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedUsers.add(users[which])
                } else {
                    selectedUsers.remove(users[which])
                }
            }

            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_save) { _, _ ->
                if (selectedUsers.isNotEmpty()) {
                    viewModel.addSelectedUsersToGroup(groupId, selectedUsers)
                }
            }
            .show()
    }
    private fun updateUIForSelectionMode(isSelectionMode: Boolean) {
        if (isSelectionMode) {
            binding.layoutDefault.visibility = View.VISIBLE
            binding.layoutSelection.visibility = View.VISIBLE
        } else {
            binding.layoutDefault.visibility = View.VISIBLE
            binding.layoutSelection.visibility = View.GONE
            binding.chkSelectAll.isChecked = false
        }
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isSelectionMode.value == true) {
                    viewModel.setSelectionMode(false)
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