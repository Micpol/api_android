package pl.netigen.core.gdpr

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import kotlinx.android.parcel.Parcelize
import pl.netigen.core.R
import pl.netigen.coreapi.gdpr.GDPRConfig

@Parcelize
object NetigenGDPRConfig : GDPRConfig() {

    override fun privacyOfflineText(context: Context?): CharSequence {
        val ss1 = SpannableString(ConstGDPR.text1)
        ss1.setSpan(StyleSpan(Typeface.BOLD), 0, ss1.length, 0)
        val builder = SpannableStringBuilder(ss1)
        val ss2 = SpannableString(ConstGDPR.text3)
        ss2.setSpan(StyleSpan(Typeface.BOLD), 0, ss2.length, 0)
        builder.append(ConstGDPR.text2 + "\n")
        builder.append(ss2)
        builder.append(ConstGDPR.text4 + "\n")
        builder.append(ConstGDPR.text5 + "\n")
        return builder
    }

    override fun gdprOfflineText(context: Context?): CharSequence {
        return StringBuilder(ConstGDPR.textPolicy1 + "\n" + ConstGDPR.textPolicy2).toString()
    }

    override fun gdprLink(context: Context?): String {
        val mobilePrivacyUrlForApp =
            "https://www.netigen.pl/privacy/only-for-mobile-apps-name?app="
        val webViewMarginValue = "&containerPadding=0&bodyMargin=0"
        val parametrizedColor = "&color="

        var link = mobilePrivacyUrlForApp

        context?.let {
            val netigenApiAccentColor =
                String.format(
                    "#%06x",
                    ContextCompat.getColor(it, R.color.dialog_accent) and 0xffffff
                ).replace("#", "")
            link =
                mobilePrivacyUrlForApp + getApplicationName(it) + parametrizedColor + netigenApiAccentColor + webViewMarginValue
        }

        return link
    }

    override fun privacyLink(context: Context?): String {
        val webViewMarginValue = "&containerPadding=0&bodyMargin=0"
        val mobilePrivacyUrl = "https://www.netigen.pl/privacy/only-for-mobile-apps?app=2"
        return mobilePrivacyUrl + webViewMarginValue
    }

    private fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(
            stringId
        )
    }

}