package com.WeTechDigital.lifeTracker.controller

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.WeTechDigital.lifeTracker.Model.ProminentModel.ProminentModel
import com.WeTechDigital.lifeTracker.databinding.ProminentdisclosureItemBinding
import com.WeTechDigital.lifeTracker.utils.Constant.TAG


class ProminentAdapter(item:List<ProminentModel>) : RecyclerView.Adapter<ProminentAdapter.MyViewHolder>() {
    private var item = item

    class MyViewHolder(private val binding: ProminentdisclosureItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(prominentModel: ProminentModel) {
            binding.apply {
                permissionLogo.setBackgroundResource(prominentModel.PermissionIcon)
                permissionName.text = prominentModel.permissionName
                permissionDes.text = prominentModel.permissionDescription
                permissionDescriptionApps.text = prominentModel.permissionDescriptionApps
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ProminentdisclosureItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       holder.bind(item[position])
        Log.d(TAG, "onBindViewHolder: ${item[position]}")
    }

    override fun getItemCount(): Int {
      return item.size
    }


}