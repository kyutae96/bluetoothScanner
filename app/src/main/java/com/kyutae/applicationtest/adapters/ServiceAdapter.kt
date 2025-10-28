package com.kyutae.applicationtest.adapters

import android.animation.ObjectAnimator
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyutae.applicationtest.R
import com.kyutae.applicationtest.utils.GattAttributes
import java.util.UUID

/**
 * GATT 서비스 목록을 표시하는 어댑터
 * 각 서비스를 클릭하면 해당 서비스의 특성(Characteristics)이 확장됨
 */
class ServiceAdapter(
    private val mContext: Context,
    private val mList: List<*>,
    private val characteristicsMap: Map<*, *>? = null
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    // 확장 상태를 추적하는 Set (position)
    private val expandedPositions = mutableSetOf<Int>()

    // CharacteristicsAdapter 참조 저장 (서비스 UUID -> Adapter)
    private val characteristicsAdapters = mutableMapOf<String, CharacteristicsAdapter>()

    var mListener: OnItemClickListener? = null
    var characteristicActionListener: CharacteristicsAdapter.OnCharacteristicActionListener? = null

    interface OnItemClickListener {
        fun onClick(view: View, position: Int)
    }

    /**
     * 특정 Characteristic의 값 업데이트
     */
    fun updateCharacteristicValue(uuid: String, value: ByteArray) {
        characteristicsAdapters.values.forEach { adapter ->
            adapter.updateCharacteristicValue(uuid, value)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var serviceTxt: TextView = itemView.findViewById(R.id.service_txt)
        var expandIcon: ImageView = itemView.findViewById(R.id.expand_icon)
        var characteristicsContainer: LinearLayout = itemView.findViewById(R.id.characteristics_container)
        var characteristicsRecyclerview: RecyclerView = itemView.findViewById(R.id.characteristics_recyclerview)
        var characteristicCount: TextView = itemView.findViewById(R.id.characteristic_count)
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
        val uuid = mList[position].toString()
        val serviceName = GattAttributes.lookup(uuid)

        // 표준 GATT 서비스인 경우 이름과 UUID를 함께 표시
        val displayText = if (GattAttributes.isStandardGatt(uuid)) {
            "$serviceName\n$uuid"
        } else {
            "Custom Service\n$uuid"
        }

        // SpannableString을 사용하여 서비스 이름을 강조
        val spannableString = SpannableString(displayText)
        val nameEndIndex = displayText.indexOf('\n')

        // 서비스 이름을 볼드로 표시
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

        holder.serviceTxt.text = spannableString

        // Characteristics 데이터 가져오기
        val serviceUUID = try {
            UUID.fromString(uuid)
        } catch (e: Exception) {
            null
        }

        val characteristics = if (serviceUUID != null && characteristicsMap != null) {
            characteristicsMap[serviceUUID] as? List<*> ?: emptyList<Any>()
        } else {
            emptyList<Any>()
        }

        // Characteristic 개수 표시
        holder.characteristicCount.text = "${characteristics.size}개"

        // Characteristics가 없으면 확장 불가
        if (characteristics.isEmpty()) {
            holder.expandIcon.visibility = View.GONE
            holder.itemView.isClickable = false
        } else {
            holder.expandIcon.visibility = View.VISIBLE
            holder.itemView.isClickable = true

            // Characteristics RecyclerView 설정
            val charAdapter = CharacteristicsAdapter(mContext, characteristics)
            charAdapter.actionListener = characteristicActionListener // 리스너 설정

            // Adapter 참조 저장
            characteristicsAdapters[uuid] = charAdapter

            holder.characteristicsRecyclerview.apply {
                adapter = charAdapter
                layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            }

            // 확장 상태에 따라 UI 업데이트
            val isExpanded = expandedPositions.contains(position)
            holder.characteristicsContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            holder.expandIcon.rotation = if (isExpanded) 270f else 90f

            // 클릭 리스너
            holder.itemView.setOnClickListener {
                toggleExpansion(holder, position)
                mListener?.onClick(it, position)
            }
        }
    }

    /**
     * 확장/축소 토글
     */
    private fun toggleExpansion(holder: ViewHolder, position: Int) {
        val isExpanded = expandedPositions.contains(position)

        if (isExpanded) {
            // 축소
            expandedPositions.remove(position)
            holder.characteristicsContainer.visibility = View.GONE

            // 화살표 회전 애니메이션 (270° → 90°)
            ObjectAnimator.ofFloat(holder.expandIcon, "rotation", 270f, 90f).apply {
                duration = 200
                start()
            }
        } else {
            // 확장
            expandedPositions.add(position)
            holder.characteristicsContainer.visibility = View.VISIBLE

            // 화살표 회전 애니메이션 (90° → 270°)
            ObjectAnimator.ofFloat(holder.expandIcon, "rotation", 90f, 270f).apply {
                duration = 200
                start()
            }
        }
    }
}
