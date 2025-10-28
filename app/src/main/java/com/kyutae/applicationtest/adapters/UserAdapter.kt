package com.kyutae.applicationtest.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.DiffUtil
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
        var itemLinear = itemView.findViewById<View>(R.id.item_linear)
        var NameTxt = itemView.findViewById<TextView>(R.id.name_txt)
        var AddressTxt = itemView.findViewById<TextView>(R.id.address_txt)
        var RssiTxt = itemView.findViewById<TextView>(R.id.rssi_txt)
        var TypeTxt = itemView.findViewById<TextView>(R.id.type_txt)
        var CompanyKeyTxt = itemView.findViewById<TextView>(R.id.company_key_txt)
        var CompanyValueTxt = itemView.findViewById<TextView>(R.id.company_value_txt)
        var UuidTxt = itemView.findViewById<TextView>(R.id.service_uuid_txt)
        var DataTxt = itemView.findViewById<TextView>(R.id.service_data_txt)
        var DetailsLayout = itemView.findViewById<LinearLayout>(R.id.details_layout)
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
        onBindViewHolder(holder, position, mutableListOf())
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // 전체 업데이트
            initBindViewHolder(holder)
            val device = mList[position]

            // 장치 이름
            holder.NameTxt.text = device.device.name ?: "Unknown Device"

            // MAC 주소
            holder.AddressTxt.text = device.device.address

            // RSSI (신호 강도) - 색상으로 강도 표시
            val rssi = device.rssi
            holder.RssiTxt.text = "$rssi dBm"
            holder.RssiTxt.setBackgroundColor(getRssiColor(rssi))

            // 장치 타입 (간소화)
            val deviceType = when(device.device.type){
                BluetoothDevice.DEVICE_TYPE_LE -> "LE"
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
                BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
                else -> "Unknown"
            }
            holder.TypeTxt.text = deviceType

            // 제조사 정보
            val manufacturerSpecificData = device.scanRecord?.manufacturerSpecificData
            if (manufacturerSpecificData != null && manufacturerSpecificData.isNotEmpty()) {
                val manufacturerId = manufacturerSpecificData.keyAt(0)
                val manufacturerData = manufacturerSpecificData.valueAt(0)
                holder.CompanyKeyTxt.text = Utils.manufacureID(manufacturerId)
                holder.CompanyValueTxt.text = Utils.bytesToHex(manufacturerData)
                holder.DetailsLayout.visibility = View.VISIBLE
            } else {
                holder.CompanyKeyTxt.text = "Unknown"
                holder.CompanyValueTxt.text = "N/A"
                holder.DetailsLayout.visibility = View.GONE
            }

            // Service UUID (숨김 처리, SettingFragment에서 사용)
            val serviceUuids = device.scanRecord?.serviceUuids
            if (!serviceUuids.isNullOrEmpty()) {
                holder.UuidTxt.text = serviceUuids[0].toString()
                val firstServiceUuid = serviceUuids[0]
                if (device.scanRecord?.serviceData?.containsKey(firstServiceUuid) == true) {
                    holder.DataTxt.text = device.scanRecord?.serviceData!![firstServiceUuid]?.toList().toString()
                } else {
                    holder.DataTxt.text = "N/A"
                }
            } else {
                holder.UuidTxt.text = "N/A"
                holder.DataTxt.text = "N/A"
            }

            // 클릭 리스너
            if (mListener != null) {
                holder.itemView.setOnClickListener { v ->
                    mListener?.onClick(v, position)
                }
            }
        } else {
            // 부분 업데이트 (RSSI만 변경)
            for (payload in payloads) {
                if (payload == BluetoothDeviceDiffCallback.PAYLOAD_RSSI_CHANGED) {
                    val device = mList[position]
                    val rssi = device.rssi
                    holder.RssiTxt.text = "$rssi dBm"
                    holder.RssiTxt.setBackgroundColor(getRssiColor(rssi))
                }
            }
        }
    }

    /**
     * RSSI 값에 따른 신호 강도 색상 반환
     */
    private fun getRssiColor(rssi: Int): Int {
        return when {
            rssi >= -50 -> ContextCompat.getColor(mContext, R.color.ewhagreen)  // 강함
            rssi >= -70 -> ContextCompat.getColor(mContext, R.color.ewhayellow)  // 보통
            else -> ContextCompat.getColor(mContext, R.color.ewhacoral)          // 약함
        }
    }

    fun initBindViewHolder(holder: ViewHolder){
        // MaterialCardView styling is handled in XML, no need to set background
        holder.NameTxt.text = null
        holder.AddressTxt.text = null
        holder.RssiTxt.text = null
        holder.RssiTxt.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey))
        holder.TypeTxt.text = null
        holder.CompanyKeyTxt.text = null
        holder.CompanyValueTxt.text = null
        holder.UuidTxt.text = null
        holder.DataTxt.text = null
    }

    /**
     * 장치 목록 업데이트 (DiffUtil 사용)
     */
    fun updateDevices(newDevices: MutableList<ScanResult>) {
        val diffCallback = BluetoothDeviceDiffCallback(mList.toList(), newDevices)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        mList.clear()
        mList.addAll(newDevices)

        diffResult.dispatchUpdatesTo(this)
    }

}