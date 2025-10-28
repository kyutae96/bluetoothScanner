package com.kyutae.applicationtest.adapters

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.kyutae.applicationtest.R
import com.kyutae.applicationtest.utils.GattAttributes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * GATT 특성 목록을 표시하는 어댑터
 * Read/Write/Notify 기능 포함
 */
class CharacteristicsAdapter(
    private val mContext: Context,
    private val mList: List<*>,
) : RecyclerView.Adapter<CharacteristicsAdapter.ViewHolder>() {

    private val TAG = "CharacteristicsAdapter"

    // 확장 상태를 추적하는 Set
    private val expandedPositions = mutableSetOf<Int>()

    // Characteristic별 현재 값 저장
    private val characteristicValues = mutableMapOf<String, ByteArray>()
    private val characteristicTimestamps = mutableMapOf<String, Long>()

    // 리스너 인터페이스
    interface OnCharacteristicActionListener {
        fun onReadCharacteristic(characteristic: BluetoothGattCharacteristic)
        fun onWriteCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray)
        fun onNotifyCharacteristic(characteristic: BluetoothGattCharacteristic, enable: Boolean)
    }

    var actionListener: OnCharacteristicActionListener? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val characteristicTxt: TextView = itemView.findViewById(R.id.characteristic_txt)
        val expandIcon: ImageView = itemView.findViewById(R.id.expand_icon)
        val characteristicHeader: LinearLayout = itemView.findViewById(R.id.characteristic_header)

        // 속성 뱃지들
        val badgeRead: TextView = itemView.findViewById(R.id.badge_read)
        val badgeWrite: TextView = itemView.findViewById(R.id.badge_write)
        val badgeNotify: TextView = itemView.findViewById(R.id.badge_notify)
        val badgeIndicate: TextView = itemView.findViewById(R.id.badge_indicate)

        // 확장 영역
        val actionsContainer: LinearLayout = itemView.findViewById(R.id.actions_container)

        // 값 표시 영역
        val valueHex: TextView = itemView.findViewById(R.id.value_hex)
        val valueDecimal: TextView = itemView.findViewById(R.id.value_decimal)
        val valueAscii: TextView = itemView.findViewById(R.id.value_ascii)
        val valueTimestamp: TextView = itemView.findViewById(R.id.value_timestamp)

        // 액션 버튼들
        val btnRead: MaterialButton = itemView.findViewById(R.id.btn_read)
        val btnWrite: MaterialButton = itemView.findViewById(R.id.btn_write)
        val btnNotify: MaterialButton = itemView.findViewById(R.id.btn_notify)

        // Write 입력 영역
        val writeInputLayout: LinearLayout = itemView.findViewById(R.id.write_input_layout)
        val inputTypeToggle: MaterialButtonToggleGroup = itemView.findViewById(R.id.input_type_toggle)
        val inputValue: TextInputEditText = itemView.findViewById(R.id.input_value)
        val btnSendWrite: MaterialButton = itemView.findViewById(R.id.btn_send_write)
        val btnCancelWrite: MaterialButton = itemView.findViewById(R.id.btn_cancel_write)

        // Toggle 버튼들
        val btnHexInput: MaterialButton = itemView.findViewById(R.id.btn_hex_input)
        val btnDecimalInput: MaterialButton = itemView.findViewById(R.id.btn_decimal_input)
        val btnAsciiInput: MaterialButton = itemView.findViewById(R.id.btn_ascii_input)
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
        val characteristic = mList[position] as? BluetoothGattCharacteristic ?: return
        val uuid = characteristic.uuid.toString()

        // Characteristic UUID 표시
        holder.characteristicTxt.text = uuid

        // 속성 확인
        val properties = characteristic.properties
        val canRead = (properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0
        val canWrite = (properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                       (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
        val canNotify = (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
        val canIndicate = (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0

        // 속성 뱃지 표시/숨김
        holder.badgeRead.visibility = if (canRead) View.VISIBLE else View.GONE
        holder.badgeWrite.visibility = if (canWrite) View.VISIBLE else View.GONE
        holder.badgeNotify.visibility = if (canNotify) View.VISIBLE else View.GONE
        holder.badgeIndicate.visibility = if (canIndicate) View.VISIBLE else View.GONE

        // 확장 상태에 따라 UI 업데이트
        val isExpanded = expandedPositions.contains(position)
        holder.actionsContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.expandIcon.rotation = if (isExpanded) 270f else 90f

        // 헤더 클릭 리스너 (확장/축소)
        holder.characteristicHeader.setOnClickListener {
            toggleExpansion(holder, position)
        }

        // 액션 버튼들 표시/숨김
        holder.btnRead.visibility = if (canRead) View.VISIBLE else View.GONE
        holder.btnWrite.visibility = if (canWrite) View.VISIBLE else View.GONE
        holder.btnNotify.visibility = if (canNotify || canIndicate) View.VISIBLE else View.GONE

        // 현재 값 표시
        updateValueDisplay(holder, uuid)

        // Read 버튼 클릭
        holder.btnRead.setOnClickListener {
            Log.d(TAG, "Read button clicked for $uuid")
            actionListener?.onReadCharacteristic(characteristic)
        }

        // Write 버튼 클릭 (입력 영역 표시/숨김)
        holder.btnWrite.setOnClickListener {
            val isVisible = holder.writeInputLayout.visibility == View.VISIBLE

            if (isVisible) {
                // 숨기기
                holder.writeInputLayout.visibility = View.GONE
                // 키보드 숨기기
                val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(holder.inputValue.windowToken, 0)
            } else {
                // 표시하기
                holder.writeInputLayout.visibility = View.VISIBLE

                // 입력 영역이 표시되면 입력 필드에 포커스를 주고 키보드 표시
                Handler(Looper.getMainLooper()).postDelayed({
                    holder.inputValue.requestFocus()

                    // 키보드 표시
                    val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(holder.inputValue, InputMethodManager.SHOW_IMPLICIT)

                    // 키보드가 나타난 후 스크롤
                    Handler(Looper.getMainLooper()).postDelayed({
                        // RecyclerView 찾기
                        var parent = holder.itemView.parent
                        while (parent != null) {
                            if (parent is RecyclerView) {
                                // 해당 아이템으로 스크롤
                                parent.smoothScrollToPosition(holder.bindingAdapterPosition)
                                break
                            }
                            parent = parent.parent
                        }

                        // 추가로 입력 필드가 보이도록 요청
                        holder.inputValue.requestRectangleOnScreen(Rect(0, 0, holder.inputValue.width, holder.inputValue.height), false)
                    }, 300)
                }, 100)
            }
        }

        // Notify 버튼 클릭
        var notifyEnabled = false
        holder.btnNotify.setOnClickListener {
            notifyEnabled = !notifyEnabled
            actionListener?.onNotifyCharacteristic(characteristic, notifyEnabled)

            // 버튼 상태 변경
            holder.btnNotify.text = if (notifyEnabled) "구독 해제" else "구독"
            holder.btnNotify.icon = if (notifyEnabled) {
                mContext.getDrawable(android.R.drawable.ic_delete)
            } else {
                mContext.getDrawable(android.R.drawable.ic_popup_reminder)
            }
        }

        // 입력 타입 선택 리스너
        setupInputTypeToggle(holder)

        // Send 버튼 클릭
        holder.btnSendWrite.setOnClickListener {
            val inputText = holder.inputValue.text.toString()
            if (inputText.isBlank()) {
                Toast.makeText(mContext, "값을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val bytes = when (holder.inputTypeToggle.checkedButtonId) {
                    R.id.btn_hex_input -> {
                        // Hex 입력 (예: "01 02 03" 또는 "010203")
                        inputText.replace(" ", "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                    }
                    R.id.btn_decimal_input -> {
                        // Decimal 입력 (예: "1 2 3" 또는 "123")
                        inputText.split(" ").filter { it.isNotBlank() }.map { it.toInt().toByte() }.toByteArray()
                    }
                    R.id.btn_ascii_input -> {
                        // ASCII 입력
                        inputText.toByteArray(Charsets.UTF_8)
                    }
                    else -> return@setOnClickListener
                }

                actionListener?.onWriteCharacteristic(characteristic, bytes)
                holder.writeInputLayout.visibility = View.GONE
                holder.inputValue.text?.clear()

                Toast.makeText(mContext, "데이터 전송 완료", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing input: ${e.message}")
                Toast.makeText(mContext, "입력 형식 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Cancel 버튼 클릭
        holder.btnCancelWrite.setOnClickListener {
            holder.writeInputLayout.visibility = View.GONE
            holder.inputValue.text?.clear()
            // 키보드 숨기기
            val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(holder.inputValue.windowToken, 0)
        }

        // 입력 필드 포커스 리스너 - 키보드가 나타날 때 스크롤
        holder.inputValue.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                Handler(Looper.getMainLooper()).postDelayed({
                    view.requestRectangleOnScreen(Rect(0, 0, view.width, view.height), false)
                }, 300)
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
            holder.actionsContainer.visibility = View.GONE

            // 화살표 회전 애니메이션 (270° → 90°)
            ObjectAnimator.ofFloat(holder.expandIcon, "rotation", 270f, 90f).apply {
                duration = 200
                start()
            }
        } else {
            // 확장
            expandedPositions.add(position)
            holder.actionsContainer.visibility = View.VISIBLE

            // 화살표 회전 애니메이션 (90° → 270°)
            ObjectAnimator.ofFloat(holder.expandIcon, "rotation", 90f, 270f).apply {
                duration = 200
                start()
            }
        }
    }

    /**
     * 입력 타입 토글 설정
     */
    private fun setupInputTypeToggle(holder: ViewHolder) {
        holder.inputTypeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            holder.inputValue.text?.clear()

            when (checkedId) {
                R.id.btn_hex_input -> {
                    holder.inputValue.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    holder.inputValue.hint = "예: 01 02 03"
                }
                R.id.btn_decimal_input -> {
                    holder.inputValue.inputType = InputType.TYPE_CLASS_NUMBER
                    holder.inputValue.hint = "예: 1 2 3"
                }
                R.id.btn_ascii_input -> {
                    holder.inputValue.inputType = InputType.TYPE_CLASS_TEXT
                    holder.inputValue.hint = "예: Hello"
                }
            }
        }
    }

    /**
     * Characteristic 값 업데이트 (외부에서 호출)
     */
    fun updateCharacteristicValue(uuid: String, value: ByteArray) {
        characteristicValues[uuid] = value
        characteristicTimestamps[uuid] = System.currentTimeMillis()
        notifyDataSetChanged()
    }

    /**
     * 값 표시 업데이트
     */
    private fun updateValueDisplay(holder: ViewHolder, uuid: String) {
        val value = characteristicValues[uuid]
        val timestamp = characteristicTimestamps[uuid]

        if (value != null) {
            // Hex 표시
            val hexString = value.joinToString(" ") { "%02X".format(it) }
            holder.valueHex.text = "Hex: $hexString"

            // Decimal 표시
            val decimalString = value.joinToString(" ") { it.toInt().and(0xFF).toString() }
            holder.valueDecimal.text = "Decimal: $decimalString"

            // ASCII 표시
            val asciiString = value.map {
                val byte = it.toInt().and(0xFF)
                if (byte in 32..126) byte.toChar() else '.'
            }.joinToString("")
            holder.valueAscii.text = "ASCII: $asciiString"

            // 시간 표시
            if (timestamp != null) {
                val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                holder.valueTimestamp.text = "읽은 시간: ${dateFormat.format(Date(timestamp))}"
            }
        } else {
            holder.valueHex.text = "Hex: N/A"
            holder.valueDecimal.text = "Decimal: N/A"
            holder.valueAscii.text = "ASCII: N/A"
            holder.valueTimestamp.text = "읽은 시간: -"
        }
    }
}