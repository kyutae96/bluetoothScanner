package com.kyutae.applicationtest.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import com.kyutae.applicationtest.R
import com.kyutae.applicationtest.bluetooth.Utils
import com.kyutae.applicationtest.dataclass.BluetoothDataClass

class ServiceAdapter(
    private val mContext: Context,
    private val mList: List<*>,
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var serviceTxt = itemView.findViewById<TextView>(R.id.service_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.service_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.serviceTxt.text = mList[position].toString()


        if (mListener != null) {
            holder.itemView.setOnClickListener { v ->
                mListener?.onClick(v, position)
            }
        }

    }


}