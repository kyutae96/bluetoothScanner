package com.kyutae.applicationtest.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kyutae.applicationtest.dataclass.BluetoothDataClass
import com.kyutae.applicationtest.dataclass.FilterSettings
import com.kyutae.applicationtest.database.AppDatabase
import com.kyutae.applicationtest.database.DeviceRepository
import kotlinx.coroutines.launch

/**
 * 정렬 방식
 */
enum class SortType {
    RSSI,      // 신호 강도순 (강한 순)
    NAME,      // 이름순 (가나다순)
    ADDRESS    // 주소순
}

/**
 * GATT 연결 상태
 */
enum class ConnectionState {
    DISCONNECTED,  // 연결 끊김
    CONNECTING,    // 연결 중
    CONNECTED,     // 연결됨
    DISCONNECTING  // 연결 해제 중
}

/**
 * MainFragment의 상태를 관리하는 ViewModel
 * Configuration Change 시에도 데이터를 유지하고 메모리 누수를 방지합니다
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Repository 초기화
    private val repository: DeviceRepository

    init {
        val deviceHistoryDao = AppDatabase.getDatabase(application).deviceHistoryDao()
        repository = DeviceRepository(deviceHistoryDao)
    }

    // 히스토리 및 즐겨찾기 데이터
    val allDeviceHistory = repository.allDevices
    val favoriteDevices = repository.favoriteDevices

    // BLE 스캔 상태
    private val _isScanning = MutableLiveData<Boolean>(false)
    val isScanning: LiveData<Boolean> = _isScanning

    // 스캔된 장치 목록 (원본)
    private val _allBluetoothDevices = mutableListOf<ScanResult>()

    // 필터링된 장치 목록 (화면에 표시)
    private val _bluetoothDevices = MutableLiveData<MutableList<ScanResult>>(mutableListOf())
    val bluetoothDevices: LiveData<MutableList<ScanResult>> = _bluetoothDevices

    // 필터 설정
    private val _filterSettings = MutableLiveData<FilterSettings>(FilterSettings())
    val filterSettings: LiveData<FilterSettings> = _filterSettings

    // GATT 연결 객체
    private val _bleGatt = MutableLiveData<BluetoothGatt?>(null)
    val bleGatt: LiveData<BluetoothGatt?> = _bleGatt

    // 선택된 장치의 데이터
    private val _selectedDeviceData = MutableLiveData<BluetoothDataClass?>(null)
    val selectedDeviceData: LiveData<BluetoothDataClass?> = _selectedDeviceData

    // 에러 메시지
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // 현재 정렬 방식 (기본값: 신호 강도순)
    private val _sortType = MutableLiveData<SortType>(SortType.RSSI)
    val sortType: LiveData<SortType> = _sortType

    // GATT 연결 상태 (기본값: 연결 끊김)
    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: LiveData<ConnectionState> = _connectionState

    /**
     * 스캔 상태 업데이트
     */
    fun setScanning(isScanning: Boolean) {
        _isScanning.value = isScanning
    }

    /**
     * 장치 목록 초기화
     */
    fun clearDevices() {
        _allBluetoothDevices.clear()
        _bluetoothDevices.value = mutableListOf()
    }

    /**
     * 스캔된 장치 추가
     */
    @SuppressLint("MissingPermission")
    fun addDevice(device: ScanResult) {
        // 중복 체크 (MAC 주소 기반)
        val exists = _allBluetoothDevices.any { it.device.address == device.device.address }
        if (!exists) {
            _allBluetoothDevices.add(device)
            applyFiltersAndSort()

            // 히스토리에 저장
            viewModelScope.launch {
                repository.addOrUpdateDevice(device)
            }
        }
    }

    /**
     * 필터 설정 업데이트
     */
    fun updateFilterSettings(settings: FilterSettings) {
        _filterSettings.value = settings
        applyFiltersAndSort()
    }

    /**
     * 필터 및 정렬 적용
     */
    @SuppressLint("MissingPermission")
    private fun applyFiltersAndSort() {
        val settings = _filterSettings.value ?: FilterSettings()

        // 필터 적용
        val filtered = if (settings.hasActiveFilters()) {
            _allBluetoothDevices.filter { scanResult ->
                var passFilter = true

                // 이름 필터
                if (settings.nameFilter.isNotEmpty()) {
                    val deviceName = scanResult.device.name ?: ""
                    passFilter = passFilter && deviceName.contains(settings.nameFilter, ignoreCase = true)
                }

                // RSSI 필터
                if (settings.minRssi > -100) {
                    passFilter = passFilter && scanResult.rssi >= settings.minRssi
                }

                // 서비스 UUID 필터
                if (settings.serviceUuidFilter.isNotEmpty()) {
                    val serviceUuids = scanResult.scanRecord?.serviceUuids?.map { it.toString() } ?: emptyList()
                    passFilter = passFilter && serviceUuids.any {
                        it.contains(settings.serviceUuidFilter, ignoreCase = true)
                    }
                }

                // 연결 가능 필터
                if (settings.connectableOnly) {
                    passFilter = passFilter && (scanResult.isConnectable ?: false)
                }

                passFilter
            }
        } else {
            _allBluetoothDevices
        }

        // 정렬 적용
        val sorted = when (_sortType.value ?: SortType.RSSI) {
            SortType.RSSI -> filtered.sortedByDescending { it.rssi }
            SortType.NAME -> filtered.sortedWith(compareBy(
                { it.device.name.isNullOrEmpty() },
                { it.device.name ?: "" }
            ))
            SortType.ADDRESS -> filtered.sortedBy { it.device.address }
        }

        _bluetoothDevices.value = sorted.toMutableList()
    }

    /**
     * GATT 연결 객체 설정
     */
    fun setBleGatt(gatt: BluetoothGatt?) {
        _bleGatt.value = gatt
    }

    /**
     * 선택된 장치 데이터 설정
     */
    fun setSelectedDeviceData(data: BluetoothDataClass?) {
        _selectedDeviceData.value = data
    }

    /**
     * 에러 메시지 설정
     */
    fun setError(message: String?) {
        _errorMessage.value = message
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * GATT 연결 상태 설정
     */
    fun setConnectionState(state: ConnectionState) {
        _connectionState.value = state
    }

    /**
     * 정렬 방식 설정 및 장치 목록 정렬
     */
    @SuppressLint("MissingPermission")
    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
        applyFiltersAndSort()
    }

    /**
     * 즐겨찾기 토글
     */
    fun toggleFavorite(address: String) {
        viewModelScope.launch {
            repository.toggleFavorite(address)
        }
    }

    /**
     * 즐겨찾기 상태 확인
     */
    suspend fun isFavorite(address: String): Boolean {
        return repository.isFavorite(address)
    }

    /**
     * 연결 시간 업데이트
     */
    fun updateConnectionTime(address: String) {
        viewModelScope.launch {
            repository.updateConnectionTime(address)
        }
    }

    /**
     * 히스토리 정리
     */
    fun cleanOldHistory() {
        viewModelScope.launch {
            repository.cleanOldHistory()
        }
    }

    /**
     * 모든 히스토리 삭제
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    /**
     * ViewModel 정리 시 GATT 연결 해제
     */
    override fun onCleared() {
        super.onCleared()
        _bleGatt.value?.close()
        _bleGatt.value = null
    }
}
