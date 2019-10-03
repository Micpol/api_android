package pl.netigen.core.netigenapi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.netigen.core.config.Config
import pl.netigen.payments.PaymentManager

abstract class NetigenViewModel(application: Application) : AndroidViewModel(application) {

    var isSplashInBackground: Boolean = false
    abstract fun prepareConfigBuilder(): Config
    open val config: Config by lazy {
        prepareConfigBuilder()
    }

    var isInEea = false
    val isMultiFullscreenApp: Boolean = config.isMultiScreen
    var noAdsLiveData = MutableLiveData<Boolean>()
    var delayBetweenInterstitialAds = 60L * 1000L
    var noAdsSku = application.packageName + ".noads"
        get() {
            return if (config.inDebugMode) {
                PaymentManager.TEST_PURCHASED
            } else {
                getApplication<Application>().packageName + ".noads"
            }
        }

    var isRewardedAdLoading: Boolean = false
    var isNoAdsPaymentAvailable: Boolean = config.isNoAdsPaymentAvailable
    var isDesignedForFamily: Boolean = config.isDesignedForFamily
    var publishersIds = config.publishersId
    var isNoAdsBought: Boolean = false
        get() {
            return config.isNoAdsBought
        }
        set(isNoAdsBought) {
            noAdsLiveData.value = isNoAdsBought
            config.isNoAdsBought = isNoAdsBought
            field = isNoAdsBought
        }

    fun getInterstitialAdId() = config.interstitialAdId
    fun getRewardedAdId() = config.rewardedAdId
    fun getBannerId() = config.bannerAdId
    fun isInDebugMode() = config.inDebugMode
    fun getTestDevices() = config.testDevices
    fun isSamsung() = config.isSamsung
}