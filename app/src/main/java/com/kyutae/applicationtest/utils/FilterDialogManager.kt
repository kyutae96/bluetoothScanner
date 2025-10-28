package com.kyutae.applicationtest.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.kyutae.applicationtest.R
import com.kyutae.applicationtest.dataclass.FilterSettings

/**
 * 필터 다이얼로그 관리 클래스
 */
object FilterDialogManager {

    /**
     * 필터 설정 다이얼로그 표시
     */
    fun showFilterDialog(
        context: Context,
        currentSettings: FilterSettings,
        onApply: (FilterSettings) -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_filter, null)

        // UI 요소 찾기
        val nameEdit = view.findViewById<TextInputEditText>(R.id.filter_name_edit)
        val rssiSlider = view.findViewById<Slider>(R.id.filter_rssi_slider)
        val rssiValueText = view.findViewById<android.widget.TextView>(R.id.filter_rssi_value)
        val uuidEdit = view.findViewById<TextInputEditText>(R.id.filter_uuid_edit)
        val connectableCheckbox = view.findViewById<MaterialCheckBox>(R.id.filter_connectable_checkbox)

        // 현재 설정값 적용
        nameEdit.setText(currentSettings.nameFilter)
        rssiSlider.value = currentSettings.minRssi.toFloat()
        rssiValueText.text = "${currentSettings.minRssi} dBm"
        uuidEdit.setText(currentSettings.serviceUuidFilter)
        connectableCheckbox.isChecked = currentSettings.connectableOnly

        // RSSI 슬라이더 변경 리스너
        rssiSlider.addOnChangeListener { _, value, _ ->
            rssiValueText.text = "${value.toInt()} dBm"
        }

        // 다이얼로그 생성
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()

        // 버튼 리스너 설정
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.filter_reset_button).setOnClickListener {
            // 초기화
            nameEdit.setText("")
            rssiSlider.value = -100f
            uuidEdit.setText("")
            connectableCheckbox.isChecked = false
        }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.filter_cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.filter_apply_button).setOnClickListener {
            // 필터 설정 생성
            val newSettings = FilterSettings(
                nameFilter = nameEdit.text?.toString()?.trim() ?: "",
                minRssi = rssiSlider.value.toInt(),
                serviceUuidFilter = uuidEdit.text?.toString()?.trim() ?: "",
                connectableOnly = connectableCheckbox.isChecked,
                enabled = true
            )

            onApply(newSettings)
            dialog.dismiss()
        }

        dialog.show()
    }
}
