package com.WeTechDigital.lifeTracker.controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.WeTechDigital.lifeTracker.view.fragment.notificationTabItem.Information
import com.WeTechDigital.lifeTracker.view.fragment.notificationTabItem.Promotion
import com.WeTechDigital.lifeTracker.view.fragment.notificationTabItem.YourPlan


class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {


    override fun createFragment(position: Int): Fragment {
        when (position) {
            1 -> return Promotion()
            2 -> return YourPlan()
        }
        return Information()
    }

    override fun getItemCount(): Int {
        return 3
    }
}