package com.WeTechDigital.lifeTracker.controller

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.databinding.TrustedPropleItemBinding
import com.bumptech.glide.Glide
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.TrustedContacts_Entity


class Contacts_RecyclerView(context: Context) :
    RecyclerView.Adapter<Contacts_RecyclerView.MyViewHolder>() {

    private var OfflinecontactsList = emptyList<TrustedContacts_Entity>()
    private val context = context

    class MyViewHolder(private val binding: TrustedPropleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            soscontactsEntity: TrustedContacts_Entity,
            context: Context
        ) {
            binding.apply {

                if (soscontactsEntity.Image != null) {
                    Glide.with(context)
                        .asBitmap()
                        .load(soscontactsEntity.Image)
                        .placeholder(R.drawable.ic_man)
                        .into(listContact)
                } else {
                    Glide.with(context)
                        .asBitmap()
                        .load(soscontactsEntity.Image)
                        .placeholder(R.drawable.ic_man)
                        .into(listContact)
                }


                trustedName.text = soscontactsEntity.Name

                // trusted_Name.text = trustedContactsModel.Name

            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(
            TrustedPropleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(OfflinecontactsList[position], context)
    }

    fun setData(soscontactsEntity: List<TrustedContacts_Entity>) {
        this.OfflinecontactsList = soscontactsEntity
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return OfflinecontactsList.size
    }
}