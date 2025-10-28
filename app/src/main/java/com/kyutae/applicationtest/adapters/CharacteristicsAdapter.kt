package com.kyutae.applicationtest.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kyutae.applicationtest.R
import com.kyutae.applicationtest.utils.GattAttributes

/**
 * GATT 특성 목록을 표시하는 어댑터
 * 표준 GATT 특성은 이름으로 표시하고, 커스텀 특성은 UUID로 표시
 */
class CharacteristicsAdapter(
    private val mContext: Context,
    private val mList: List<*>,
) : RecyclerView.Adapter<CharacteristicsAdapter.ViewHolder>() {

    var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(view: View, position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var characteristicTxt: TextView = itemView.findViewById(R.id.characteristic_txt)
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
        val uuid = mList[position].toString()
        val characteristicName = GattAttributes.lookup(uuid)

        // 표준 GATT 특성인 경우 이름과 UUID를 함께 표시
        val displayText = if (GattAttributes.isStandardGatt(uuid)) {
            "$characteristicName\n$uuid"
        } else {
            "Custom Characteristic\n$uuid"
        }

        // SpannableString을 사용하여 특성 이름을 강조
        val spannableString = SpannableString(displayText)
        val nameEndIndex = displayText.indexOf('\n')

        // 특성 이름을 볼드로 표시
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            nameEndIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // UUID는 회색으로 표시
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.gray)),
            nameEndIndex,
            displayText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        holder.characteristicTxt.text = spannableString

        if (mListener != null) {
            holder.itemView.setOnClickListener { v ->
                mListener?.onClick(v, position)
            }
        }
    }
}