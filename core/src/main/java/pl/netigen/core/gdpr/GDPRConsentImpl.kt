package pl.netigen.core.gdpr

import android.app.Application
import android.content.Context
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import pl.netigen.coreapi.gdpr.AdConsentStatus
import pl.netigen.coreapi.gdpr.CheckGDPRLocationStatus
import pl.netigen.coreapi.gdpr.IGDPRConsent
import pl.netigen.coreapi.gdpr.IGDPRConsentConfig
import timber.log.Timber

class GDPRConsentImpl(private val application: Application, private val config: IGDPRConsentConfig) : IGDPRConsent {
    val consentInformation: ConsentInformation = ConsentInformation.getInstance(application)
    override val adConsentStatus: Flow<AdConsentStatus> = flow {
        val value =
                application.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(PREFERENCES_KEY, AdConsentStatus.UNINITIALIZED.ordinal)
        emit(AdConsentStatus.values().getOrElse(value) { AdConsentStatus.UNINITIALIZED })
    }

    override fun requestGDPRLocation(): Flow<CheckGDPRLocationStatus> =
            callbackFlow {
                val callback = object : ConsentInfoUpdateListener {
                    override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                        if (isActive) {
                            val isInEea = consentInformation.isRequestLocationInEeaOrUnknown
                            offer(if (isInEea) CheckGDPRLocationStatus.UE else CheckGDPRLocationStatus.NON_UE)
                            channel.close()
                        }
                    }

                    override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                        if (isActive) {
                            offer(CheckGDPRLocationStatus.ERROR)
                            channel.close()
                        }
                    }
                }
                consentInformation.requestConsentInfoUpdate(config.adMobPublisherIds, callback)
                try {
                    awaitClose {}
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

    override fun saveAdConsentStatus(adConsentStatus: AdConsentStatus) {
        application.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(PREFERENCES_KEY, adConsentStatus.ordinal)
                .apply()
    }

    companion object {
        const val PREFERENCES_NAME = "GDPRConsent"
        const val PREFERENCES_KEY = "AdConsentStatus"
    }
}