package io.octo.bear.pago;

import android.content.Context;

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

    private final Context context;

    public static Pago getInstance(Context context) {
        if (pagoInstance == null) {
            pagoInstance = new Pago(context);
        }
        return pagoInstance;
    }

    private Pago(Context context) {
        this.context = context;
    }

    public Single<List<Sku>> getSkuItemDetails(List<String> skuIds) {
        return new SkuDetailsObservable(context, PurchaseType.INAPP, skuIds);
    }

    public Single<List<Sku>> getSkuSubscriptionDetails(List<String> skuIds) {
        return new SkuDetailsObservable(context, PurchaseType.SUBSCRIPTION, skuIds);
    }

    public Single<Purchase> purchaseProduct(String sku) {
        return new PurchasingObservable(context, PurchaseType.INAPP, sku);
    }

    public Single<Purchase> purchaseSubscription(String sku) {
        return new PurchasingObservable(context, PurchaseType.SUBSCRIPTION, sku);
    }

    public static Gson gson() {
        return gson;
    }

}
