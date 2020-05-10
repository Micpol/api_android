package pl.netigen.core.gdpr

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDialogFragment
import kotlinx.android.synthetic.main.dialog_fragment_gdpr.*
import pl.netigen.core.R
import pl.netigen.coreapi.gdpr.GDPRConfig
import pl.netigen.extensions.setDialogSizeAsMatchParent
import pl.netigen.extensions.setTint

class GDPRDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val CONFIG_ID = "CONFIG_ID"

        fun newInstance(config: GDPRConfig): GDPRDialogFragment {
            val dialogFragment = GDPRDialogFragment()
            dialogFragment.isCancelable = false
            val args = Bundle().apply {
                putParcelable(CONFIG_ID, config)
            }
            dialogFragment.arguments = args
            return dialogFragment
        }
    }

    private var isNoAdsAvailable = false
    private var gdprClickListener: GDPRClickListener? = null
    private var admobText: Boolean = false
    private var webViewGdpr: WebView? = null
    private lateinit var gdprConfig: GDPRConfig

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (dialog != null) {
            val window = dialog?.window
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE)
                dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }

        arguments?.getParcelable<GDPRConfig>(CONFIG_ID)?.let {
            gdprConfig = it
        }
        val view = inflater.inflate(R.layout.dialog_fragment_gdpr, container, false)
        createWebView(view)
        return view
    }

    private fun createWebView(view: View) {
        activity?.let {
            webViewGdpr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                WebView(it.createConfigurationContext(Configuration()))
            else
                WebView(it.applicationContext)
        }
        view.findViewById<FrameLayout>(R.id.containerGDPRInfo).addView(webViewGdpr)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity == null) {
            dismiss()
            return
        }
        setIcon()

        appNameTextViewGdpr.text = getApplicationName(activity!!)
        setButtons()
        showGDPRText()
    }

    override fun onStart() {
        super.onStart()
        setDialogSizeAsMatchParent()
        setButtonsBackgroundTints()
    }

    private fun setButtonsBackgroundTints() {
        context?.let {
            buttonYes.background.setTint(it, R.color.dialog_accent, PorterDuff.Mode.MULTIPLY)
            buttonPolicy.background.setTint(it, R.color.dialog_accent, PorterDuff.Mode.MULTIPLY)

            buttonNo.background.setTint(
                    it,
                    R.color.dialog_neutral_button_bg,
                    PorterDuff.Mode.MULTIPLY
            )
            buttonPay.background.setTint(
                    it,
                    R.color.dialog_neutral_button_bg,
                    PorterDuff.Mode.MULTIPLY
            )
            buttonBack.background.setTint(
                    it,
                    R.color.dialog_neutral_button_bg,
                    PorterDuff.Mode.MULTIPLY
            )
        }
    }

    private fun setButtons() {
        buttonYes.setOnClickListener {
            gdprClickListener?.onConsentAccepted(true)
            dismiss()
        }
        buttonNo.setOnClickListener {
            showPrivacyPolicy()
        }
        buttonBack.setOnClickListener {
            showAdmobText()
        }
        if (isNoAdsAvailable) {
            buttonPay.visibility = View.VISIBLE
            buttonPay.setOnClickListener { gdprClickListener?.clickPay() }
        } else {
            buttonPay.visibility = View.GONE
        }
        buttonPolicy.setOnClickListener {
            gdprClickListener?.onConsentAccepted(false)
            dismiss()
        }
    }

    private fun setIcon() {
        try {
            val icon = activity?.packageManager?.getApplicationIcon(activity!!.packageName)
            appIconImageViewGdpr.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(
                stringId
        )
    }

    fun setIsPayOptions(isNoAdsAvailable: Boolean) {
        this.isNoAdsAvailable = isNoAdsAvailable
        if (isAdded) {
            setButtons()
        }
    }

    private fun showGDPRText() {
        if (isNoAdsAvailable) {
            buttonPay.visibility = View.VISIBLE
        }
        buttonNo.visibility = View.VISIBLE
        buttonYes.visibility = View.VISIBLE
        buttonPolicy.visibility = View.GONE
        buttonBack.visibility = View.GONE

        admobText = true
        if (isNetworkOn()) {
            offlinePrivacyPolicyTextView.visibility = View.GONE
            webViewGdpr?.visibility = View.VISIBLE
            webViewGdpr?.loadUrl(gdprConfig.gdprLink(context))
        } else {
            webViewGdpr?.visibility = View.GONE
            offlinePrivacyPolicyTextView.visibility = View.VISIBLE
            setOfflineText()
        }
    }

    private fun isNetworkOn(): Boolean {
        if (context == null) {
            return false
        }
        val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                        ?: return false
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    private fun setOfflineText() {
        offlinePrivacyPolicyTextView.text = gdprConfig.privacyOfflineText(context)
    }

    private fun showPrivacyPolicy() {
        if (isNoAdsAvailable) {
            buttonPay.visibility = View.INVISIBLE
        }
        buttonYes.visibility = View.GONE
        buttonNo.visibility = View.INVISIBLE
        buttonPolicy.visibility = View.VISIBLE
        buttonBack.visibility = View.VISIBLE
        admobText = false
        if (isNetworkOn()) {
            offlinePrivacyPolicyTextView.visibility = View.GONE
            webViewGdpr?.visibility = View.VISIBLE
            webViewGdpr?.loadUrl(gdprConfig.privacyLink(context))
        } else {
            webViewGdpr?.visibility = View.GONE
            offlinePrivacyPolicyTextView.visibility = View.VISIBLE
            setScrollToOfflinePolicy()
            onNoInternetConnection()
        }
    }

    private fun onNoInternetConnection() {
        offlinePrivacyPolicyTextView.append(gdprConfig.gdprOfflineText(context))
    }

    private fun setScrollToOfflinePolicy() {
        offlinePrivacyPolicyTextView.movementMethod = ScrollingMovementMethod()
    }

    override fun onDetach() {
        super.onDetach()
        gdprClickListener = null
    }

    private fun showAdmobText() {
        if (!admobText) {
            showGDPRText()
        }
    }

    fun bindGDPRListener(gdprClickListener: GDPRClickListener) {
        this.gdprClickListener = gdprClickListener
    }

    interface GDPRClickListener {
        fun onConsentAccepted(personalizedAds: Boolean)

        fun clickPay()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.getParcelable<GDPRConfig>(CONFIG_ID)?.let {
            gdprConfig = it
        }
    }
}