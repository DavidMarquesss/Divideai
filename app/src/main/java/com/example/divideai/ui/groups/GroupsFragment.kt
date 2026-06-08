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
import androidx.lifecycle.lifecycleScope
import com.example.divideai.R
import com.example.divideai.data.invite.GroupInviteCode
import com.example.divideai.data.model.User
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.GroupRepository
import com.example.divideai.data.repository.MemberRepository
import com.example.divideai.data.repository.UserRepository
import com.example.divideai.databinding.FragmentGroupsBinding
import com.example.divideai.ui.groups.form.GroupFormActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class GroupsFragment : Fragment() {

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private val groupsViewModel: GroupsViewModel by viewModels()

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val memberRepository = MemberRepository()
    private val groupRepository = GroupRepository()

    private val scanQrLauncher = registerForActivityResult(ScanContract()) { result ->
        val groupId = GroupInviteCode.decode(result?.contents)
        if (groupId == null) {
            if (result?.contents != null) {
                Toast.makeText(requireContext(), R.string.group_qr_invalid, Toast.LENGTH_SHORT).show()
            }
            return@registerForActivityResult
        }
        joinGroupFromInvite(groupId)
    }


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Material fade-through gives a subtle transition when switching bottom-nav tabs.
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()
    }

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
            binding.layoutEmpty.visibility = if (groups.isEmpty()) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = false
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
        binding.swipeRefresh.setOnRefreshListener { groupsViewModel.fetchGroups() }

        binding.btnAddGroup.setOnClickListener {
            val intent = Intent(context, GroupFormActivity::class.java)
            startActivity(intent)
        }

        binding.btnScanQr.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt(getString(R.string.group_qr_scan_prompt))
                setBeepEnabled(false)
                setOrientationLocked(true)
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            }
            scanQrLauncher.launch(options)
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

    /**
     * Carrega o grupo escaneado, adiciona o usuário atual como membro e exibe
     * feedback. Casos tratados:
     *  - grupo inexistente
     *  - usuário já é membro
     *  - falha de rede ao adicionar
     */
    private fun joinGroupFromInvite(groupId: String) {
        val firebaseUser = authRepository.getCurrentUser() ?: return
        groupRepository.getGroupById(groupId) { group ->
            val context = context ?: return@getGroupById
            if (group == null) {
                Toast.makeText(context, R.string.group_qr_group_missing, Toast.LENGTH_SHORT).show()
                return@getGroupById
            }
            if (group.memberIds.contains(firebaseUser.uid)) {
                Toast.makeText(context, R.string.group_qr_already_member, Toast.LENGTH_SHORT).show()
                return@getGroupById
            }
            userRepository.getUserById(firebaseUser.uid) { fetchedUser ->
                val userToAdd = fetchedUser ?: User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: ""
                )
                memberRepository.addMembersToGroup(groupId, listOf(userToAdd)) { success ->
                    val ctx = this.context ?: return@addMembersToGroup
                    if (success) {
                        Toast.makeText(
                            ctx,
                            getString(R.string.group_qr_joined, group.title),
                            Toast.LENGTH_SHORT
                        ).show()
                        groupsViewModel.fetchGroups()
                    } else {
                        Toast.makeText(ctx, R.string.group_qr_join_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}