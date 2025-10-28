package com.kyutae.applicationtest.utils

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.concurrent.TimeUnit

/**
 * RSSI 그래프 관리 클래스
 */
class RssiGraphManager(private val chart: LineChart) {

    private val rssiEntries = mutableListOf<Entry>()
    private var startTime = System.currentTimeMillis()

    init {
        setupChart()
    }

    /**
     * 차트 초기 설정
     */
    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            // X축 설정
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                granularity = 1f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val seconds = value.toInt()
                        return "${seconds}s"
                    }
                }
            }

            // Y축 설정 (왼쪽)
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = -100f
                axisMaximum = 0f
            }

            // Y축 설정 (오른쪽 - 비활성화)
            axisRight.isEnabled = false

            legend.isEnabled = true
        }
    }

    /**
     * RSSI 데이터 추가
     */
    fun addRssiValue(rssi: Int) {
        val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(
            System.currentTimeMillis() - startTime
        ).toFloat()

        rssiEntries.add(Entry(elapsedSeconds, rssi.toFloat()))

        // 최대 100개 데이터 포인트만 유지
        if (rssiEntries.size > 100) {
            rssiEntries.removeAt(0)
        }

        updateChart()
    }

    /**
     * 차트 업데이트
     */
    private fun updateChart() {
        val dataSet = LineDataSet(rssiEntries, "RSSI (dBm)").apply {
            color = Color.BLUE
            lineWidth = 2f
            setCircleColor(Color.BLUE)
            circleRadius = 3f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    /**
     * 차트 초기화
     */
    fun reset() {
        rssiEntries.clear()
        startTime = System.currentTimeMillis()
        chart.clear()
    }

    /**
     * 평균 RSSI 계산
     */
    fun getAverageRssi(): Int {
        if (rssiEntries.isEmpty()) return 0
        return rssiEntries.map { it.y.toInt() }.average().toInt()
    }

    /**
     * 거리 추정 (RSSI 기반, 대략적)
     * 공식: distance = 10 ^ ((measuredPower - rssi) / (10 * n))
     * measuredPower: -59 (1m에서의 평균 RSSI)
     * n: 2.0 (환경 계수)
     */
    fun estimateDistance(rssi: Int): Double {
        val measuredPower = -59
        val n = 2.0
        return Math.pow(10.0, (measuredPower - rssi) / (10 * n))
    }
}
