package pl.netigen.gms.payments

import android.app.Activity
import android.app.Application
import pl.netigen.coreapi.payments.Payments
import pl.netigen.coreapi.payments.model.NetigenSkuDetails
import timber.log.Timber.d

class GMSPayments(
    application: Application,
    inAppSkuList: List<String> = listOf("${application.packageName}.noads"),
    private val noAdsInAppSkuList: List<String> = listOf("${application.packageName}.noads"),
    consumablesInAppSkuList: List<String> = emptyList()
) : Payments() {
    override val paymentsRepo = GMSPaymentsRepo(application, inAppSkuList, noAdsInAppSkuList, consumablesInAppSkuList)

    override fun makePurchase(activity: Activity, netigenSkuDetails: NetigenSkuDetails) {
        paymentsRepo.launchBillingFlow(activity, netigenSkuDetails)
    }

    override fun makeNoAdsPayment(activity: Activity, noAdsString: String) {
        d("activity = [$activity], noAdsString = [$noAdsString]")
        if (noAdsString in noAdsInAppSkuList) {
            paymentsRepo.makeNoAdsPurchase(activity, noAdsString)
        } else {
            paymentsRepo.makeNoAdsPurchase(activity, noAdsInAppSkuList[0])
        }
    }

}