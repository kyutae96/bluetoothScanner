package com.kyutae.applicationtest.adapters

import android.bluetooth.le.ScanResult
import androidx.recyclerview.widget.DiffUtil

/**
 * BLE 장치 목록 변경 감지를 위한 DiffUtil.Callback
 * 변경된 항목만 업데이트하여 RecyclerView 깜빡임 방지
 */
class BluetoothDeviceDiffCallback(
    private val oldList: List<ScanResult>,
    private val newList: List<ScanResult>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // MAC 주소로 같은 장치인지 판단
        return oldList[oldItemPosition].device.address == newList[newItemPosition].device.address
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        // RSSI 값과 이름이 같으면 내용이 같다고 판단
        return oldItem.rssi == newItem.rssi &&
                oldItem.device.name == newItem.device.name
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        // RSSI만 변경된 경우 RSSI만 업데이트하도록 페이로드 반환
        if (oldItem.rssi != newItem.rssi) {
            return PAYLOAD_RSSI_CHANGED
        }

        return null
    }

    companion object {
        const val PAYLOAD_RSSI_CHANGED = "rssi_changed"
    }
}
