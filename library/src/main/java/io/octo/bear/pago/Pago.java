package io.octo.bear.pago;

import android.content.Context;

import com.google.gson.Gson;

import java.util.List;

import io.octo.bear.pago.model.entity.Order;
import io.octo.bear.pago.model.entity.Purchase;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.PurchasedItem;
import io.octo.bear.pago.model.entity.Sku;
import io.octo.bear.pago.model.service.BillingAvailabiliyObservable;
import io.octo.bear.pago.model.service.ConsumePurchaseObservable;
import io.octo.bear.pago.model.service.PurchasedItemsObservable;
import io.octo.bear.pago.model.service.PurchasingObservable;
import io.octo.bear.pago.model.service.SkuDetailsObservable;
import rx.Completable;
import rx.Single;

/**
 * Created by shc on 14.07.16.
 */
public class Pago {

    public static final int BILLING_API_VERSION = 3;

    private static Gson gson = new Gson();
    private final Context context;

    public Pago(Context context) {
        this.context = context;
    }

    public Single<Boolean> checkPurchasesAvailability() {
        return new BillingAvailabiliyObservable(context, PurchaseType.INAPP);
    }

    public Single<Boolean> checkSubscriptionAvailability() {
        return new BillingAvailabiliyObservable(context, PurchaseType.SUBSCRIPTION);
    }

    public Single<List<Sku>> getSkuProductsDetails(final List<String> skuIds) {
        return new SkuDetailsObservable(context, PurchaseType.INAPP, skuIds);
    }

    public Single<List<Sku>> getSkuSubscriptionDetails(final List<String> skuIds) {
        return new SkuDetailsObservable(context, PurchaseType.SUBSCRIPTION, skuIds);
    }

    public Single<Order> purchaseProduct(final String sku) {
        return new PurchasingObservable(context, PurchaseType.INAPP, sku);
    }

    public Single<Order> purchaseSubscription(final String sku) {
        return new PurchasingObservable(context, PurchaseType.SUBSCRIPTION, sku);
    }

    public Single<List<PurchasedItem>> getPurchasedProducts() {
        return new PurchasedItemsObservable(context, PurchaseType.INAPP);
    }

    public Single<List<PurchasedItem>> getPurchasedSubscriptions() {
        return new PurchasedItemsObservable(context, PurchaseType.SUBSCRIPTION);
    }

    public Completable consumeProduct(final String purchaseToken) {
        return new ConsumePurchaseObservable(context, purchaseToken);
    }

    public static Gson gson() {
        return gson;
    }

}
