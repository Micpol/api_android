package pl.netigen.core.splash

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import pl.netigen.coreapi.ads.IAds
import pl.netigen.coreapi.gdpr.AdConsentStatus
import pl.netigen.coreapi.gdpr.AdConsentStatus.*
import pl.netigen.coreapi.gdpr.CheckGDPRLocationStatus
import pl.netigen.coreapi.gdpr.IGDPRConsent
import pl.netigen.coreapi.main.IAppConfig
import pl.netigen.coreapi.network.INetworkStatus
import pl.netigen.coreapi.payments.INoAds
import pl.netigen.coreapi.splash.SplashState
import pl.netigen.coreapi.splash.SplashVM
import pl.netigen.extensions.launch
import pl.netigen.extensions.launchIO
import timber.log.Timber.d
import timber.log.Timber.e

class CoreSplashVMImpl(
    application: Application,
    private val gdprConsent: IGDPRConsent,
    private val ads: IAds,
    private val noAdsPurchases: INoAds,
    private val networkStatus: INetworkStatus,
    private val appConfig: IAppConfig,
    val coroutineDispatcherIo: CoroutineDispatcher = Dispatchers.IO,
    val coroutineDispatcherMain: CoroutineDispatcher = Dispatchers.Main
) : SplashVM(application), INoAds by noAdsPurchases {
    override val splashState: MutableLiveData<SplashState> = MutableLiveData(SplashState.UNINITIALIZED)
    override val isFirstLaunch: MutableLiveData<Boolean> = MutableLiveData(false)
    override val isNoAdsAvailable: Boolean = appConfig.isNoAdsAvailable
    private var isRunning = false
    private var finished = false

    override fun start() {
        d("TRY FIX 10")
        d(isRunning.toString())
        if (!isRunning) init()
    }

    private fun init() {
        isRunning = true
        try {
            launch(coroutineDispatcherIo) {
                try {
                    noAdsPurchases.noAdsActive.collect {
                        try {
                            onAdsFlowChanged(it)
                        } catch (e: Exception) {
                            e(e)
                        }
                    }
                } catch (e: Exception) {
                    e(e)
                }
            }
            try {
                launchWithTimeout(appConfig.maxConsentWaitTime, gdprConsent.adConsentStatus) {
                    when {
                        finished -> finish()
                        it == UNINITIALIZED -> onFirstLaunch()
                        else -> onNextLaunch(it)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                onFirstLaunch()
            }
        } catch (e: Exception) {
            e(e)
        }
    }

    private fun <T> launchWithTimeout(
        timeOut: Long,
        flow: Flow<T>,
        coroutineDispatcher: CoroutineDispatcher = coroutineDispatcherIo,
        action: suspend (value: T) -> Unit
    ) {
        launch(coroutineDispatcher) {
            try {
                withTimeout(timeOut) {
                    try {
                        flow.collect(action)
                    } catch (e: Exception) {
                        e(e)
                    }
                }
            } catch (e: Exception) {
                e(e)
                if (e is TimeoutCancellationException) throw e
            }
        }
    }

    private fun onAdsFlowChanged(purchased: Boolean) {
        d("purchased = [$purchased]")
        if (purchased) {
            finish()
        }
    }

    private fun finish() {
        d("()")
        cleanUp()
        finished = true
        updateState(SplashState.FINISHED)
    }

    private fun cleanUp() {
        try {
            viewModelScope.coroutineContext.cancelChildren()
        } catch (e: Exception) {
            e(e)
        }
    }

    private fun updateState(splashState: SplashState) = this.splashState.postValue(splashState)

    private fun onFirstLaunch() {
        d("()")
        if (finished) return finish()
        if (!networkStatus.isConnectedOrConnecting) return showGdprPopUp()
        isFirstLaunch.postValue(true)
        try {
            try {
                launchWithTimeout(appConfig.maxConsentWaitTime, gdprConsent.requestGDPRLocation()) { onFirstLaunchCheckGdpr(it) }
            } catch (e: TimeoutCancellationException) {
                showGdprPopUp()
            }
        } catch (e: Exception) {
            e(e)
        }
    }

    private fun showGdprPopUp() {
        d("()")
        try {
            launch(coroutineDispatcherIo) {
                try {
                    noAdsPurchases.noAdsActive.collect { onAdsFlowChanged(it) }
                } catch (e: Exception) {
                    e(e)
                }
            }
        } catch (e: Exception) {
            e(e)
        }

        updateState(SplashState.SHOW_GDPR_CONSENT)
    }

    private fun onFirstLaunchCheckGdpr(it: CheckGDPRLocationStatus) {
        d("it = [$it]")
        when (it) {
            CheckGDPRLocationStatus.NON_UE -> initOnNonUeLocation()
            CheckGDPRLocationStatus.UE -> showGdprPopUp()
            CheckGDPRLocationStatus.ERROR -> showGdprPopUp()
        }
    }

    private fun onNextLaunch(adConsentStatus: AdConsentStatus) {
        d("it = [$adConsentStatus]")
        startLoadingInterstitial()
        if (adConsentStatus == PERSONALIZED_NON_UE) checkConsentNextLaunch()
    }

    private fun initOnNonUeLocation() {
        d("()")
        ads.personalizedAdsEnabled = true
        gdprConsent.saveAdConsentStatus(PERSONALIZED_NON_UE)
        startLoadingInterstitial()
    }

    private fun startLoadingInterstitial() {
        d("()")
        if (!networkStatus.isConnectedOrConnecting || finished) return finish()
        updateState(SplashState.LOADING)
        try {
            launchIO {
                try {
                    try {
                        withTimeout(appConfig.maxInterstitialWaitTime) {
                            try {
                                withContext(coroutineDispatcherMain) {
                                    when {
                                        finished -> finish()
                                        ads.interstitialAd.isLoaded -> onLoadInterstitialResult(true)
                                        else -> ads.interstitialAd.load().collect {
                                            try {
                                                onLoadInterstitialResult(it)
                                            } catch (e: Exception) {
                                                e(e)
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e(e)
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        d(e)
                        withContext(coroutineDispatcherMain) {
                            onLoadInterstitialResult(false)
                        }
                    }
                } catch (e: Exception) {
                    e(e)
                }
            }
        } catch (e: Exception) {
            e(e)
        }

    }

    private fun onLoadInterstitialResult(success: Boolean) = if (success) onInterstitialLoaded() else finish()

    private fun onInterstitialLoaded() {
        d("()")
        cleanUp()
        ads.interstitialAd.showIfCanBeShowed { finish() }
    }

    private fun checkConsentNextLaunch() {
        try {
            launchWithTimeout(appConfig.maxConsentWaitTime, gdprConsent.requestGDPRLocation()) { onFirstLaunchCheckGdpr(it) }
        } catch (e: Exception) {
            e(e)
        }
    }

    override fun setPersonalizedAds(personalizedAdsApproved: Boolean) {
        d("personalizedAdsApproved = [$personalizedAdsApproved]")
        val adConsentStatus: AdConsentStatus = if (personalizedAdsApproved) PERSONALIZED_SHOWED else NON_PERSONALIZED_SHOWED
        gdprConsent.saveAdConsentStatus(adConsentStatus)
        ads.personalizedAdsEnabled = personalizedAdsApproved
        startLoadingInterstitial()
    }

    override fun onCleared() {
        d("()")
        try {
            if (isRunning) {
                if (!finished) {
                    cleanUp()
                }
                updateState(SplashState.UNINITIALIZED)
                isRunning = false
                finished = false
            }
        } catch (e: Exception) {
            e(e)
        }
    }
}