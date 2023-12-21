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

class CharacteristicsAdapter(
    private val mContext: Context,
    private val mList: List<*>,
) : RecyclerView.Adapter<CharacteristicsAdapter.ViewHolder>() {

    var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var characteristicTxt = itemView.findViewById<TextView>(R.id.characteristic_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.characteristics_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.characteristicTxt.text = mList[position].toString()


        if (mListener != null) {
            holder.itemView.setOnClickListener { v ->
                mListener?.onClick(v, position)
            }
        }

    }


}