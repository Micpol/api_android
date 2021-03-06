package pl.netigen.gms.payments

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import pl.netigen.coreapi.payments.model.NetigenSkuDetails

@Dao
interface NetigenSkuDetailsDao {
    @Query("SELECT * FROM NetigenSkuDetails WHERE type = '${BillingClient.SkuType.SUBS}'")
    fun subscriptionSkuDetailsLiveData(): LiveData<List<NetigenSkuDetails>>

    @Query("SELECT * FROM NetigenSkuDetails WHERE type = '${BillingClient.SkuType.INAPP}'")
    fun inAppSkuDetailsLiveData(): LiveData<List<NetigenSkuDetails>>

    @Transaction
    fun insertOrUpdate(skuDetails: SkuDetails, isNoAds: Boolean = false) = skuDetails.apply {
        val originalJson = toString().substring("SkuDetails: ".length)
        val detail = NetigenSkuDetails(sku, type, isNoAds, price, title, description, originalJson)
        insert(detail)
    }

    @Transaction
    fun insertOrUpdate(sku: String, isNoAds: Boolean = false) {
        val result = getById(sku)
        if (result != null) {
            update(sku, isNoAds)
        } else {
            insert(NetigenSkuDetails(sku, null, isNoAds, null, null, null, null))
        }
    }

    @Query("SELECT * FROM NetigenSkuDetails WHERE sku = :sku")
    fun getById(sku: String): NetigenSkuDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(NetigenSkuDetails: NetigenSkuDetails)

    @Query("UPDATE NetigenSkuDetails SET isNoAds = :isNoAds WHERE sku = :sku")
    fun update(sku: String, isNoAds: Boolean)
}