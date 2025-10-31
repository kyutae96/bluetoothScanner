package com.kyutae.applicationtest.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.kyutae.applicationtest.BuildConfig
import com.kyutae.applicationtest.R

/**
 * Google AdMob 광고 관리 유틸리티 클래스
 * 전면 광고를 로드하고 표시하는 기능을 제공합니다
 */
class AdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private val TAG = "AdManager"

    /**
     * 전면 광고 로드
     * @param onAdLoaded 광고 로드 성공 시 콜백
     * @param onAdFailed 광고 로드 실패 시 콜백
     */
    fun loadInterstitialAd(
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null
    ) {
        val adRequest = AdRequest.Builder().build()

        // DEBUG 모드에서는 테스트 광고 ID 사용
        val adUnitId = if (BuildConfig.DEBUG) {
            context.getString(R.string.admob_test_interstitial_id)
        } else {
            context.getString(R.string.admob_interstitial_id)
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "Loading interstitial ad")
        Log.d(TAG, "Build Type: ${if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"}")
        Log.d(TAG, "Ad Type: ${if (BuildConfig.DEBUG) "TEST AD" else "REAL AD"}")
        Log.d(TAG, "Ad Unit ID: $adUnitId")
        Log.d(TAG, "========================================")

        // DEBUG 모드에서만 Toast 표시
        if (BuildConfig.DEBUG) {
            Toast.makeText(
                context,
                "테스트 광고 로드 중...",
                Toast.LENGTH_SHORT
            ).show()
        }

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Ad failed to load")
                    Log.e(TAG, "Error code: ${adError.code}")
                    Log.e(TAG, "Error message: ${adError.message}")
                    interstitialAd = null
                    onAdFailed?.invoke()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad loaded successfully!")
                    Log.d(TAG, "Ad is ready to show")
                    interstitialAd = ad

                    // DEBUG 모드에서만 Toast 표시
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(
                            context,
                            "테스트 광고 로드 완료!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    onAdLoaded?.invoke()
                }
            }
        )
    }

    /**
     * 전면 광고 표시
     * @param activity 광고를 표시할 Activity
     * @param onAdDismissed 광고 종료 후 콜백
     * @param onAdFailed 광고 표시 실패 시 콜백
     * @return 광고가 표시되었는지 여부
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit,
        onAdFailed: (() -> Unit)? = null
    ): Boolean {
        return if (interstitialAd != null) {
            Log.d(TAG, "📺 Showing interstitial ad...")

            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    Log.d(TAG, "👆 Ad was clicked")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "✅ Ad dismissed - User closed the ad")
                    interstitialAd = null
                    onAdDismissed()
                    // 다음 광고 미리 로드
                    Log.d(TAG, "🔄 Preloading next ad...")
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "❌ Ad failed to show")
                    Log.e(TAG, "Error: ${adError.message}")
                    interstitialAd = null
                    onAdFailed?.invoke()
                }

                override fun onAdImpression() {
                    Log.d(TAG, "📊 Ad impression recorded")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "✅ Ad showed full screen content")
                }
            }

            interstitialAd?.show(activity)
            true
        } else {
            Log.w(TAG, "⚠️ Interstitial ad is not ready yet")
            Log.w(TAG, "Proceeding without ad...")
            onAdFailed?.invoke()
            false
        }
    }

    /**
     * 광고가 준비되었는지 확인
     */
    fun isAdReady(): Boolean {
        return interstitialAd != null
    }

    /**
     * 광고 해제
     */
    fun destroy() {
        interstitialAd = null
    }

    companion object {
        /**
         * 테스트 장치 추가 (선택 사항)
         *
         * 참고: BuildConfig.DEBUG가 true일 때는 자동으로 테스트 광고가 표시되므로
         * 일반적으로 이 메서드를 사용할 필요가 없습니다.
         *
         * 특정 실제 기기에서 테스트 광고를 보려면:
         * 1. Logcat에서 "Use RequestConfiguration.Builder().setTestDeviceIds()" 메시지 확인
         * 2. Application 클래스의 onCreate()에서 아래 코드 추가:
         *
         * ```kotlin
         * MobileAds.setRequestConfiguration(
         *     RequestConfiguration.Builder()
         *         .setTestDeviceIds(listOf("YOUR_DEVICE_ID"))
         *         .build()
         * )
         * ```
         */
        const val TAG_INFO = "AdManager"
    }
}
