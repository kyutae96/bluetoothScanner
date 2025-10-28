package com.kyutae.applicationtest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyutae.applicationtest.adapters.UserAdapter
import com.kyutae.applicationtest.bluetooth.BLEController
import com.kyutae.applicationtest.bluetooth.Utils
import com.kyutae.applicationtest.databinding.FragmentMainBinding
import com.kyutae.applicationtest.dataclass.BluetoothDataClass
import com.kyutae.applicationtest.viewmodel.MainViewModel
import com.kyutae.applicationtest.viewmodel.SortType
import com.kyutae.applicationtest.utils.AdManager
import com.kyutae.applicationtest.utils.ErrorDialogManager
import com.kyutae.applicationtest.utils.FilterDialogManager

class MainFragment : Fragment() {
    private lateinit var bind: FragmentMainBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var adManager: AdManager
    private val TAG = "MainFragment"

    // ViewModel 사용 (Activity 범위로 공유)
    private val viewModel: MainViewModel by activityViewModels()

    // 하위 호환성을 위한 companion object (SettingFragment에서 접근)
    companion object {
        var bleGatt: BluetoothGatt? = null
        var bluetoothDataClass: BluetoothDataClass? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentMainBinding.inflate(inflater, container, false)

        return bind.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel을 Utils에 설정
        Utils.setViewModel(viewModel)

        // 광고 관리자 초기화
        adManager = AdManager(requireContext())
        adManager.loadInterstitialAd(
            onAdLoaded = {
                Log.d(TAG, "Initial ad loaded successfully")
            },
            onAdFailed = {
                Log.w(TAG, "Initial ad failed to load")
            }
        )

        // 스캔 상태 관찰
        viewModel.isScanning.observe(viewLifecycleOwner) { isScanning ->
            if (isScanning) {
                bind.scanButton.text = getString(R.string.scan_stop)
                bind.scanButton.setOnClickListener {
                    Utils.scanDevice(false)
                }
                bind.progressCircular.visibility = View.VISIBLE
                bind.swipeRefresh.isRefreshing = false
            } else {
                bind.scanButton.text = getString(R.string.scan_start)
                bind.scanButton.setOnClickListener {
                    Utils.scanDevice(true)
                }
                bind.progressCircular.visibility = View.GONE
                bind.swipeRefresh.isRefreshing = false
            }
        }

        // 장치 목록 관찰
        viewModel.bluetoothDevices.observe(viewLifecycleOwner) { devices ->
            userAdapter.updateDevices(devices)
            updateEmptyView(devices.size)
            updateDeviceCount(devices.size)
        }

        // 스와이프 새로고침
        bind.swipeRefresh.setOnRefreshListener {
            if (viewModel.isScanning.value != true) {
                Utils.scanDevice(true)
            } else {
                bind.swipeRefresh.isRefreshing = false
            }
        }

        // 필터 버튼 클릭 리스너
        bind.filterButton.setOnClickListener {
            FilterDialogManager.showFilterDialog(
                requireContext(),
                viewModel.filterSettings.value ?: com.kyutae.applicationtest.dataclass.FilterSettings()
            ) { newSettings ->
                viewModel.updateFilterSettings(newSettings)
                if (newSettings.hasActiveFilters()) {
                    Toast.makeText(requireContext(), "필터가 적용되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "필터가 해제되었습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 정렬 버튼 클릭 리스너
        bind.sortButton.setOnClickListener { view ->
            showSortMenu(view)
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                // 에러 종류에 따라 적절한 다이얼로그 표시
                when {
                    it.contains("권한", ignoreCase = true) || it.contains("permission", ignoreCase = true) -> {
                        ErrorDialogManager.showPermissionError(requireContext())
                    }
                    it.contains("블루투스", ignoreCase = true) || it.contains("bluetooth", ignoreCase = true) -> {
                        ErrorDialogManager.showBluetoothError(requireContext(), it)
                    }
                    it.contains("연결", ignoreCase = true) || it.contains("connection", ignoreCase = true) -> {
                        ErrorDialogManager.showConnectionError(requireContext(), null)
                    }
                    else -> {
                        ErrorDialogManager.showError(requireContext(), "오류", it)
                    }
                }
                viewModel.clearError()
            }
        }

        // RecyclerView 설정
        userAdapter = UserAdapter(requireContext(), mutableListOf())
        userAdapter.mListener = object : UserAdapter.OnItemClickListener {
            @SuppressLint("MissingPermission")
            override fun onClick(view: View, position: Int) {
                val devices = viewModel.bluetoothDevices.value ?: return

                if (position >= devices.size) return

                // 장치 선택 후 처리하는 로직
                val handleDeviceClick = {
                    Utils.scanDevice(false) // scan 중지
                    val device = devices[position].device
                    Log.d(TAG, "Device selected: ${device.address}")

                    bleGatt = BLEController(context, bleGatt).connectGatt(device)
                    viewModel.setBleGatt(bleGatt)

                    bluetoothDataClass = BluetoothDataClass(
                        view.findViewById<TextView>(R.id.name_txt).text.toString(),
                        view.findViewById<TextView>(R.id.address_txt).text.toString(),
                        view.findViewById<TextView>(R.id.rssi_txt).text.toString(),
                        view.findViewById<TextView>(R.id.type_txt).text.toString(),
                        view.findViewById<TextView>(R.id.company_key_txt).text.toString(),
                        view.findViewById<TextView>(R.id.company_value_txt).text.toString(),
                        view.findViewById<TextView>(R.id.service_uuid_txt).text.toString(),
                        view.findViewById<TextView>(R.id.service_data_txt).text.toString(),
                    )
                    viewModel.setSelectedDeviceData(bluetoothDataClass)

                    Log.d(TAG, "Navigating to SettingFragment")

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFrameLayout, SettingFragment())
                        .addToBackStack(null)
                        .commit()
                }

                // 광고 표시 (광고가 없으면 바로 이동)
                val adShown = adManager.showInterstitialAd(
                    activity = requireActivity(),
                    onAdDismissed = {
                        handleDeviceClick()
                    },
                    onAdFailed = {
                        handleDeviceClick()
                    }
                )

                // 광고가 표시되지 않았으면 바로 이동
                if (!adShown) {
                    handleDeviceClick()
                }
            }

        }
        bind.recyclerview.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    /**
     * 빈 상태 뷰 표시/숨김
     */
    private fun updateEmptyView(deviceCount: Int) {
        if (deviceCount == 0) {
            bind.emptyView.visibility = View.VISIBLE
            bind.recyclerview.visibility = View.GONE
        } else {
            bind.emptyView.visibility = View.GONE
            bind.recyclerview.visibility = View.VISIBLE
        }
    }

    /**
     * 장치 개수 표시
     */
    private fun updateDeviceCount(count: Int) {
        bind.deviceCountText.text = "장치: ${count}개"
    }

    /**
     * 정렬 메뉴 표시
     */
    private fun showSortMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.sort_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.sort_by_rssi -> {
                    viewModel.setSortType(SortType.RSSI)
                    Toast.makeText(requireContext(), "신호 강도순 정렬", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.sort_by_name -> {
                    viewModel.setSortType(SortType.NAME)
                    Toast.makeText(requireContext(), "이름순 정렬", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.sort_by_address -> {
                    viewModel.setSortType(SortType.ADDRESS)
                    Toast.makeText(requireContext(), "주소순 정렬", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 광고 리소스 정리
        adManager.destroy()
    }
}