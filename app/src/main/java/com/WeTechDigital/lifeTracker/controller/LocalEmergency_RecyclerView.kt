package com.WeTechDigital.lifeTracker.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.WeTechDigital.lifeTracker.databinding.LocalEmergencyItemRecyclerBinding
import com.WeTechDigital.lifeTracker.Model.EmergencyModel.LocalEmergencyModel
import com.WeTechDigital.lifeTracker.utils.Constant.TAG

class LocalEmergency_RecyclerView(context: Context) :
    RecyclerView.Adapter<LocalEmergency_RecyclerView.MyViewHolder>() {
    private var localItem = ArrayList<LocalEmergencyModel>()
    private val context = context

    class MyViewHolder(val binding: LocalEmergencyItemRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, localEmergencyModel: LocalEmergencyModel) {
            binding.apply {
                Log.d(TAG, "bind is called : $localEmergencyModel")
                thanaName.text = localEmergencyModel.thana
                hospitalName.text = localEmergencyModel.hospital
                hospitalNumber.text = localEmergencyModel.hospitalNumber.drop(1)
                fireServiceName.text = localEmergencyModel.fireService
                fireServiceNumber.text = localEmergencyModel.fireServiceNumber.drop(1)
                numberOS.text = localEmergencyModel.osNumber.drop(1)
                DhakaMetropolitanPolice.text = localEmergencyModel.Metropolitan_Police
                policeContact.text = localEmergencyModel.Police_Number.drop(1)
                emailOrFax.text = localEmergencyModel.Email_or_Fax_Number.drop(1)
                hospitalCall.setOnClickListener {
                    callNumber(localEmergencyModel.hospitalNumber.drop(1),context)
                }
                fireServiceCall.setOnClickListener {
                    callNumber(localEmergencyModel.fireServiceNumber.drop(1),context)
                }

            }
        }

        private fun callNumber(number: String, context: Context) {
            val intent =
                Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = Uri.parse("tel:$number")

            context.startActivity(intent)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(
            LocalEmergencyItemRecyclerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    fun setData(localItem: ArrayList<LocalEmergencyModel>) {
        this.localItem = localItem
        notifyDataSetChanged()
    }

    fun clear() {
        localItem.clear()
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(context, localItem[position])
    }

    override fun getItemCount(): Int {
        return localItem.size
    }
}