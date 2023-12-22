package com.kyutae.applicationtest.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import com.kyutae.applicationtest.R
import com.kyutae.applicationtest.bluetooth.Utils
import com.kyutae.applicationtest.dataclass.BluetoothDataClass

class UserAdapter(
    private val mContext: Context,
    private val mList: MutableList<ScanResult>,
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    var mListener: OnItemClickListener? = null

    var bluetoothDataClass : MutableList<BluetoothDataClass>? = null

    interface OnItemClickListener {
        fun onClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemLinear = itemView.findViewById<LinearLayout>(R.id.item_linear)
        var NameTxt = itemView.findViewById<TextView>(R.id.name_txt)
        var AddressTxt = itemView.findViewById<TextView>(R.id.address_txt)
        var RssiTxt = itemView.findViewById<TextView>(R.id.rssi_txt)
        var TypeTxt = itemView.findViewById<TextView>(R.id.type_txt)
        var CompanyKeyTxt = itemView.findViewById<TextView>(R.id.company_key_txt)
        var CompanyValueTxt = itemView.findViewById<TextView>(R.id.company_value_txt)
        var UuidTxt = itemView.findViewById<TextView>(R.id.service_uuid_txt)
        var DataTxt = itemView.findViewById<TextView>(R.id.service_data_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        initBindViewHolder(holder)
        holder.NameTxt.text = mList[position].device.name ?: "N/A"
        holder.AddressTxt.text = mList[position].device.address
        holder.RssiTxt.text = mList[position].rssi.toString()
        when(mList[position].device.type){
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> holder.TypeTxt.text = "UNKNOWN 장치"
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> holder.TypeTxt.text = "Bluetooth Classic 장치"
            BluetoothDevice.DEVICE_TYPE_LE -> {
                holder.TypeTxt.text = "Bluetooth LE 장치"
                holder.itemLinear.setBackgroundResource(R.drawable.border_bluetooth_item_blue)
            }
            BluetoothDevice.DEVICE_TYPE_DUAL -> {
                holder.TypeTxt.text = "Bluetooth Classic & LE 장치"
                holder.itemLinear.setBackgroundResource(R.drawable.border_bluetooth_item_blue)
            }
        }
        holder.CompanyKeyTxt.text = "N/A"
        holder.CompanyValueTxt.text = "N/A"
        val manufacturerSpecificData = mList[position].scanRecord?.manufacturerSpecificData
        if (manufacturerSpecificData != null && manufacturerSpecificData.isNotEmpty()) {
            // 제조사 식별자를 통해 제조사 정보를 식별
            val manufacturerId = manufacturerSpecificData.keyAt(0)
            val manufacturerData = manufacturerSpecificData.valueAt(0)

            holder.CompanyKeyTxt.text = Utils.manufacureID(manufacturerId)
//            holder.CompanyValueTxt.text = manufacturerData.toList().toString()
            holder.CompanyValueTxt.text = Utils.bytesToHex(manufacturerData)
        }
        val serviceUuids = mList[position].scanRecord?.serviceUuids

        if (serviceUuids.isNullOrEmpty()) {
            holder.UuidTxt.text = "N/A"
            holder.DataTxt.text = "N/A"
        } else {
            holder.UuidTxt.text = serviceUuids[0].toString()
            val firstServiceUuid = serviceUuids[0]
            if (mList[position].scanRecord?.serviceData?.containsKey(firstServiceUuid) == true) {
                holder.DataTxt.text = mList[position].scanRecord?.serviceData!![firstServiceUuid]?.toList().toString()
            } else {
                holder.DataTxt.text = "N/A"
            }
        }

//        val appearance = scanRecord?.appearance

        if (mListener != null) {
            holder.itemView.setOnClickListener { v ->
                mListener?.onClick(v, position)
            }
        }

    }

    fun initBindViewHolder(holder: ViewHolder){
        holder.itemLinear.setBackgroundResource(R.drawable.border_user_item)
        holder.NameTxt.text = null
        holder.AddressTxt.text = null
        holder.RssiTxt.text = null
        holder.TypeTxt.text = null
        holder.CompanyKeyTxt.text = null
        holder.CompanyValueTxt.text = null
        holder.UuidTxt.text = null
        holder.DataTxt.text = null
    }



}