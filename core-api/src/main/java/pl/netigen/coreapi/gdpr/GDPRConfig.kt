package pl.netigen.coreapi.gdpr

import android.content.Context
import android.os.Parcelable

abstract class GDPRConfig : Parcelable {

    abstract fun privacyOfflineText(context: Context?): CharSequence?
    abstract fun privacyLink(context: Context?): String
    abstract fun gdprOfflineText(context: Context?): CharSequence?
    abstract fun gdprLink(context: Context?): String
}