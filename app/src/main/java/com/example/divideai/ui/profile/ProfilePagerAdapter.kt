package com.example.divideai.ui.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter responsavel por gerenciar os fragmentos exibidos no ViewPager2 do perfil (guias).
 * Retorna as instâncias de [FriendListFragment] e [PendingRequestsFragment].
 */
class ProfilePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            // We will create FriendListFragment and PendingRequestsFragment based on the old FriendsFragment
            0 -> com.example.divideai.ui.profile.friends.FriendListFragment()
            1 -> com.example.divideai.ui.profile.friendrequest.PendingRequestsFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
