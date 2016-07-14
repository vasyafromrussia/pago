package io.octo.bear.pago;

import com.google.gson.Gson;

import java.util.List;

import io.octo.bear.pago.model.entity.Purchase;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.Sku;
import io.octo.bear.pago.model.service.PurchasingObservable;
import io.octo.bear.pago.model.service.SkuDetailsObservable;
import rx.Single;

/**
 * Created by shc on 14.07.16.
 */
public class Pago {

    public static final int BILLING_API_VERSION = 3;

    private static Pago pagoInstance;
    private static Gson gson = new Gson();

    private final BillingActivity billingActivity;

    public static Pago getInstance() {
        if (pagoInstance == null) {
            pagoInstance = new Pago();
        }
        return pagoInstance;
    }

    private Pago() {
        this.billingActivity = new BillingActivity();
    }

    public Single<List<Sku>> getSkuItemDetails(List<String> skuIds) {
        return new SkuDetailsObservable(billingActivity, gson, PurchaseType.INAPP, skuIds);
    }

    public Single<List<Sku>> getSkuSubscriptionDetails(List<String> skuIds) {
        return new SkuDetailsObservable(billingActivity, gson, PurchaseType.SUBSCRIPTION, skuIds);
    }

    public Single<Purchase> purchaseProduct(String sku) {
        return new PurchasingObservable(billingActivity, gson, PurchaseType.INAPP, sku);
    }

    public Single<Purchase> purchaseSubscription(String sku) {
        return new PurchasingObservable(billingActivity, gson, PurchaseType.SUBSCRIPTION, sku);
    }

}
