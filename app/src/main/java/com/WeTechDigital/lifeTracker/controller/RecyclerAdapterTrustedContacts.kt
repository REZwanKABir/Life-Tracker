package com.WeTechDigital.lifeTracker.controller

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.databinding.ReadContactsListBinding
import com.bumptech.glide.Glide
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.TrustedContacts_Entity
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.WeTechDigital.lifeTracker.view.fragment.ContactsAdd.ListContactsDirections


class RecyclerAdapterTrustedContacts(requireContext: Context) :
    RecyclerView.Adapter<RecyclerAdapterTrustedContacts.MyViewHolder>() {
    private val context = requireContext
    private var listInfoOffline =
        mutableListOf<TrustedContacts_Entity>()

    class MyViewHolder(private val binding: ReadContactsListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            soscontactsEntity: TrustedContacts_Entity,
            context: Context
        ) {
            binding.apply {
                if (soscontactsEntity.Image != null) {
                    //trustPeopleImage.setImageBitmap(soscontactsEntity.Image)
                    Glide.with(context)
                        .asBitmap()
                        .load(soscontactsEntity.Image)
                        .placeholder(R.drawable.ic_man)
                        .into(trustPeopleImage)
                } else {
                    Glide.with(context)
                        .asBitmap()
                        .load(soscontactsEntity.Image)
                        .placeholder(R.drawable.ic_man)
                        .into(trustPeopleImage)
                }

                contactsName.text = soscontactsEntity.Name
                contactsNumber.text = soscontactsEntity.Phone
                toUpdate.setOnClickListener {
                    val action = ListContactsDirections.actionListContactsToUpdateContacts(soscontactsEntity)
                    itemView.findNavController().navigate(action)
                }
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(
            ReadContactsListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun deleteItem(position: Int): TrustedContacts_Entity {
        Log.d(TAG, "deleteItem: is called")

        val tobeDelete = TrustedContacts_Entity(

            listInfoOffline[position].Phone,
            listInfoOffline[position].Priority,
            listInfoOffline[position].Name,
            listInfoOffline[position].Image
        )

        listInfoOffline.removeAt(position)
        notifyDataSetChanged()
        return tobeDelete
    }


    fun setValue(list: List<TrustedContacts_Entity>) {
        this.listInfoOffline = list as MutableList<TrustedContacts_Entity>
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(listInfoOffline[position], context)
    }

    override fun getItemCount(): Int {
        return listInfoOffline.size
    }


}