package pl.netigen.sampleapp.flavour

import android.app.Application
import pl.netigen.core.config.AppConfig
import pl.netigen.core.main.CoreMainActivity
import pl.netigen.coreapi.payments.IPayments
import pl.netigen.hms.ads.HMSAds
import pl.netigen.hms.ads.HMSAds.Companion.TEST_REWARDED_ID
import pl.netigen.hms.gdpr.GDPRConsentImpl
import pl.netigen.hms.payments.HMSPayments

object FlavoursConst {
    const val bannerAdId: String = "q3g3nchj3i"
    const val interstitialAdId: String = "h1c04ba3pb"
    const val rewardedAdId: String = TEST_REWARDED_ID


    fun getPaymentsImpl(coreMainActivity: CoreMainActivity): IPayments = HMSPayments(coreMainActivity)
    fun getAdsImpl(coreMainActivity: CoreMainActivity, appConfig: AppConfig) = HMSAds(coreMainActivity, appConfig)
    fun getGDPRConsentImpl(application: Application, appConfig: AppConfig) = GDPRConsentImpl(application, appConfig)
}