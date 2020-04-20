package pl.netigen.sampleapp.flavour

import android.app.Application
import pl.netigen.core.config.AppConfig
import pl.netigen.core.main.CoreMainActivity
import pl.netigen.coreapi.payments.IPayments
import pl.netigen.hms.ads.HMSAds
import pl.netigen.hms.gdpr.GDPRConsentImpl
import pl.netigen.hms.payments.HMSPayments

object FlavoursConst {
    const val bannerAdId: String = ""
    const val interstitialAdId: String = ""
    const val rewardedAdId: String = ""


    fun getPaymentsImpl(coreMainActivity: CoreMainActivity): IPayments = HMSPayments(coreMainActivity)
    fun getAdsImpl(coreMainActivity: CoreMainActivity, appConfig: AppConfig) = HMSAds(coreMainActivity, appConfig)
    fun getGDPRConsentImpl(application: Application, appConfig: AppConfig) = GDPRConsentImpl(application, appConfig)
}