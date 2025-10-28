package com.kyutae.applicationtest.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 에러 다이얼로그 관리 클래스
 * 일관된 에러 표시를 위한 유틸리티
 */
object ErrorDialogManager {

    /**
     * 일반 에러 다이얼로그 표시
     */
    fun showError(
        context: Context,
        title: String = "오류",
        message: String,
        onDismiss: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * 권한 관련 에러 다이얼로그
     */
    fun showPermissionError(
        context: Context,
        onSettings: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle("권한 필요")
            .setMessage("블루투스 스캔을 위해서는 필요한 권한을 허용해야 합니다.\n\n설정에서 권한을 허용해주세요.")
            .setPositiveButton("설정으로 이동") { dialog, _ ->
                dialog.dismiss()
                onSettings?.invoke()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * 블루투스 관련 에러 다이얼로그
     */
    fun showBluetoothError(
        context: Context,
        message: String = "블루투스를 사용할 수 없습니다.\n\n블루투스가 켜져 있는지 확인해주세요.",
        onRetry: (() -> Unit)? = null
    ) {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle("블루투스 오류")
            .setMessage(message)
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)

        if (onRetry != null) {
            builder.setPositiveButton("재시도") { dialog, _ ->
                dialog.dismiss()
                onRetry.invoke()
            }
        } else {
            builder.setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
        }

        builder.show()
    }

    /**
     * GATT 연결 에러 다이얼로그
     */
    fun showConnectionError(
        context: Context,
        deviceName: String?,
        onRetry: (() -> Unit)? = null
    ) {
        val message = if (deviceName != null) {
            "'$deviceName' 장치에 연결할 수 없습니다.\n\n장치가 가까이 있는지 확인하고 다시 시도해주세요."
        } else {
            "장치에 연결할 수 없습니다.\n\n장치가 가까이 있는지 확인하고 다시 시도해주세요."
        }

        val builder = MaterialAlertDialogBuilder(context)
            .setTitle("연결 실패")
            .setMessage(message)
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)

        if (onRetry != null) {
            builder.setPositiveButton("재시도") { dialog, _ ->
                dialog.dismiss()
                onRetry.invoke()
            }
        } else {
            builder.setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
        }

        builder.show()
    }

    /**
     * 확인 다이얼로그
     */
    fun showConfirmation(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "확인",
        negativeText: String = "취소",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                dialog.dismiss()
                onConfirm.invoke()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                dialog.dismiss()
                onCancel?.invoke()
            }
            .setCancelable(true)
            .show()
    }
}
