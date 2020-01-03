package pl.netigen.core.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import pl.netigen.core.netigenapi.NetigenViewModel
import pl.netigen.core.rewards.RewardItem
import pl.netigen.core.rewards.RewardListenersList
import pl.netigen.core.rewards.RewardsListener

class AdmobAds(
    private val viewModel: NetigenViewModel,
    private val activity: AppCompatActivity,
    private val bannerLayout: RelativeLayout?,
    private val isInDebugMode: Boolean = viewModel.isInDebugMode(),
    private val testDevices: ArrayList<String> = viewModel.getTestDevices()
) : IAds, LifecycleObserver {
    private var personalizedAdsApproved: Boolean = false
    private val interstitialAdListeners: MutableList<InterstitialAdListener> = mutableListOf()
    val bannerId: String = TODO("not implemented")
    var rewardedAdManager: RewardedAd? = null
    var admobBanner: AdmobBanner
    var admobInterstitial: AdmobInterstitial

    init {
        MobileAds.initialize(activity)
        this.admobBanner = AdmobBanner(activity, this)
        this.admobInterstitial = AdmobInterstitial(viewModel, activity, this)
        activity.lifecycle.addObserver(this)
    }

    fun loadInterstitialIfPossible() {
        admobInterstitial.loadIfShouldBeLoaded()
    }

    fun initRewardedVideoAd(rewardsListener: RewardsListener) {
        rewardedAdManager = RewardedAd(viewModel, activity, rewardsListener)
    }

    fun loadRewardedVideoForAdId(rewardedAdId: String?) {
        if (rewardedAdManager == null) throw NullPointerException("Trying to load RewardedAd without initialization")
        rewardedAdManager?.loadIfNotLoadingForAdId(rewardedAdId)
    }

    fun loadRewardedVideo() {
        if (rewardedAdManager == null) throw NullPointerException("Trying to load RewardedAd without initialization")
        rewardedAdManager?.loadIfNotLoading()
    }

    fun resetCustomRewardedAdId() {
        if (rewardedAdManager == null) throw NullPointerException("Trying to reset RewardedAd without initialization")
        rewardedAdManager?.customRewardedAdId = null
    }

    fun showRewardedVideoForItems(rewardItems: MutableList<out RewardItem>, listeners: RewardListenersList? = null) {
        if (rewardedAdManager == null) throw NullPointerException("Trying to show RewardedAd without initialization")
        listeners?.let {
            rewardedAdManager?.addListeners(it)
        }
        rewardedAdManager?.showRewardedVideoForItems(rewardItems)
    }

    fun reloadRewardedAd() {
        rewardedAdManager?.reloadAd()
    }

    fun removeRewardedVideoCallbacks(listeners: RewardListenersList) {
        rewardedAdManager?.removeListeners(listeners)
    }

    fun removeRewardedVideoCallback(listener: RewardsListener) {
        rewardedAdManager?.removeListener(listener)
    }

    fun showRewardedVideo() {
        if (rewardedAdManager == null) throw NullPointerException("Trying to show RewardedAd without initialization")
        rewardedAdManager?.showRewardedVideoAd()
    }

    fun getAdRequest(): AdRequest {
        val builder = AdRequest.Builder()
        if (isInDebugMode) {
            builder.addTestDevice("F1F415DDE480395A4D21C26D6C6A9619")
                .addTestDevice("9F65EEB1B6AED06CBE01CFEDA106BD29")
                .addTestDevice("0F4B0296B48D2C6478D7E9A89DDD07F8")
                .addTestDevice("593C1BC2C754805F5EFBCD8D4E288805")
                .addTestDevice("E4347B3256669EAB2235222F475C8492")
                .addTestDevice("0BFB00BFF8850BE0B8D40286BEDF317E")
                .addTestDevice("59E58CCD5AB9F4ED41033F114E088239")
                .addTestDevice("E42C3769BD763551CAC733B6AD662C0D")
                .addTestDevice("14D51CBB5AB10BDC1FF61BAECA19C9AA")
                .addTestDevice("8A575BCD3FC92B5719700193610FF48D")
                .addTestDevice("8B1306F1E7DBD7B656E55F89DFC222D7")
                .addTestDevice("3F520FF0CA7D49829C8E876C954D8E3D")
                .addTestDevice("CFB9B2A279566BB6577918A8A8C8F849")
                .addTestDevice("65364CA3C9CF87116F1D374660DF1235")
                .addTestDevice("209772FAC421F8EC3FF0D98B7A72FAD2")
                .addTestDevice("14D51CBB5AB10BDC1FF61BAECA19C9AA")
                .addTestDevice("3D1FCDD4B6DC7E453846A04D214FD12D")
                .addTestDevice("43AAFCE5A6B9E8FCDC58E58087AEC4EF")
                .addTestDevice("AD2180512DE8B1EE611AB4645A69E470")
                .addTestDevice("379BED7628AE4885B439939575F9F292")


            for (i in testDevices.indices) {
                builder.addTestDevice(testDevices[i])
            }
        }
        if (personalizedAdsApproved) return builder.build()

        val extras = Bundle()
        extras.putString("npa", "1")
        return builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
    }

    fun isRewardedVideoAdLoaded(): Boolean = rewardedAdManager?.isRewardedVideoAdLoaded() ?: false

    fun isOnline(): Boolean {
        val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo: NetworkInfo? = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        rewardedAdManager?.onDestroy()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        admobInterstitial.onResume()
        rewardedAdManager?.onResume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        admobInterstitial.onPause()
        rewardedAdManager?.onPause()
    }

    override fun addInterstitialListener(interstitialAdListener: InterstitialAdListener) {
        interstitialAdListeners.add(interstitialAdListener)
    }

    override fun removeInterstitialListener(interstitialAdListener: InterstitialAdListener) {
        interstitialAdListeners.remove(interstitialAdListener)
    }

    override fun loadInterstitialAd() = TODO()

    override fun showInterstitialAd(onClosedOrNotShowed: (Boolean) -> Unit) {
        admobInterstitial.show(object : AdmobInterstitial.ShowInterstitialListener {
            override fun onShowedOrNotLoaded(success: Boolean) {
                onClosedOrNotShowed(success)
            }
        })
    }

    override fun enable() {
        admobBanner.enabled = true
    }

    override fun disable() {
        admobBanner.enabled = false
    }

    override fun setConsentStatus(personalizedAdsApproved: Boolean) {
        this.personalizedAdsApproved = personalizedAdsApproved
    }

}
