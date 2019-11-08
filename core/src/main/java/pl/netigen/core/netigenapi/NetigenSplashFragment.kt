package pl.netigen.core.netigenapi

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Observer
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentStatus
import pl.netigen.core.gdpr.ConstGDPR
import pl.netigen.gdpr.GDPRDialogFragment

abstract class NetigenSplashFragment<ViewModel : NetigenViewModel> : NetigenFragment(), GDPRDialogFragment.GDPRClickListener {

    var shouldShowHomeFragmentOnResume = false
        private set
    private var consentNotShowed: Boolean = false
    open lateinit var viewModel: ViewModel
    lateinit var netigenMainActivity: NetigenMainActivity<NetigenViewModel>
    private var gdprDialogFragment: GDPRDialogFragment? = null
    private lateinit var initAdsHandler: Handler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setupParentActivity(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        startNoAdsLogic()
        netigenMainActivity.hideBanner()
        netigenMainActivity.hideAds()
    }

    private fun setupParentActivity(context: Context) {
        if (context is NetigenMainActivity<*>) {
            netigenMainActivity = context as NetigenMainActivity<NetigenViewModel>
            viewModel = netigenMainActivity.viewModel as ViewModel
        } else {
            throw IllegalStateException("Parent activity should be of type NetigenMainActivity<VM: NetigenViewModel>")
        }
    }

    private fun startNoAdsLogic() {
        if (viewModel.isNoAdsPaymentAvailable) {
            netigenMainActivity.checkIfNoAdsBought()
            observeNoAds()
        } else {
            onNoAdsPaymentNotAvailable()
        }
    }

    override fun onResume() {
        super.onResume()
        if (consentNotShowed) {
            onConsentInfoUpdated(netigenMainActivity)
            consentNotShowed = false
        }
        if (shouldShowHomeFragmentOnResume) {
            showHomeFragment()
        }
        shouldShowHomeFragmentOnResume = false
    }

    private fun observeNoAds() {
        viewModel.noAdsLiveData.observe(this, Observer {
            if (it) {
                onCreateWithoutAds()
            } else {
                onCreateWithAds()
            }
        })
    }

    private fun onCreateWithoutAds() {
        showHomeFragment()
        gdprDialogFragment?.dialog?.dismiss()
    }

    abstract fun showHomeFragment()

    private fun onCreateWithAds() {
        if (viewModel.isDesignedForFamily) {
            onDesignedForFamily()
        } else {
            showConsent()
        }
    }

    private fun onNoAdsPaymentNotAvailable() {
        viewModel.isNoAdsBought = false
        onCreateWithAds()
    }

    private fun onDesignedForFamily() {
        showHomeFragment()
        clickNo()
        initAds()
    }

    private fun showConsent() {
        gdprDialogFragment?.let {
            if (it.isAdded) return
        }

        netigenMainActivity.consentInformation.requestConsentInfoUpdate(viewModel.publishersIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                if (netigenMainActivity.canCommitFragments()) {
                    consentNotShowed = false
                    onConsentInfoUpdated(netigenMainActivity)
                } else {
                    consentNotShowed = true
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                startAdsSplash()
            }
        })
    }

    private fun onConsentInfoUpdated(it: NetigenMainActivity<NetigenViewModel>) {
        ConstGDPR.isInEea = it.consentInformation.isRequestLocationInEeaOrUnknown
        if (ConstGDPR.isInEea && it.consentInformation.consentStatus == ConsentStatus.UNKNOWN) {
            initGDPRFragment()
        } else {
            startAdsSplash()
        }
    }

    override fun clickNo() {
        netigenMainActivity.consentInformation.consentStatus = ConsentStatus.NON_PERSONALIZED
    }

    private fun initGDPRFragment() {
        val fragment = netigenMainActivity.supportFragmentManager.findFragmentByTag(GDPR_POP_UP_TAG) as GDPRDialogFragment?
        if (fragment != null) {
            gdprDialogFragment = fragment
        } else {
            gdprDialogFragment = GDPRDialogFragment.newInstance()
            gdprDialogFragment?.show(netigenMainActivity.supportFragmentManager.beginTransaction().addToBackStack(null), GDPR_POP_UP_TAG)
        }
        gdprDialogFragment?.setIsPayOptions(viewModel.isNoAdsPaymentAvailable)
        gdprDialogFragment?.bindGDPRListener(this)
    }

    private fun startAdsSplash() {
        if (viewModel.isDesignedForFamily) {
            clickNo()
            showHomeFragment()
        } else {
            if (!::initAdsHandler.isInitialized) {
                initAdsHandler = Handler()
                initAdsHandler.post { this.initAds() }
            }
        }
    }

    internal open fun initAds() {
        netigenMainActivity.initAdsManager()
        val adsManager = netigenMainActivity.adsManager
        if (adsManager != null) {
            adsManager.launchSplashLoaderOrOpenFragment { showHomeFragment() }
        } else {
            showHomeFragment()
        }
    }

    override fun clickAcceptPolicy() {
        startAdsSplash()
    }

    override fun onDestroyView() {
        if (!viewModel.isNoAdsBought) {
            netigenMainActivity.showAds()
            netigenMainActivity.showBanner()
        }
        super.onDestroyView()
    }

    override fun clickYes() {
        netigenMainActivity.consentInformation.consentStatus = ConsentStatus.PERSONALIZED
        startAdsSplash()
    }

    override fun onPause() {
        super.onPause()
        shouldShowHomeFragmentOnResume = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        shouldShowHomeFragmentOnResume = true
    }
}

private const val GDPR_POP_UP_TAG = "GDPR_POP_UP"