package io.octo.bear.pago;

import android.content.Context;

import com.google.gson.Gson;

import java.util.List;

import io.octo.bear.pago.model.entity.Order;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.PurchasedItem;
import io.octo.bear.pago.model.entity.Sku;
import rx.Completable;
import rx.Single;

/**
 * Created by shc on 14.07.16.
 */
public class Pago {

    static final int BILLING_API_VERSION = 3;

    private static Gson gson = new Gson();
    private final Context context;

    public Pago(Context context) {
        this.context = context;
    }

    public Single<Boolean> checkPurchasesAvailability() {
        return new BillingAvailabilitySingle(context, PurchaseType.INAPP);
    }

    public Single<Boolean> checkSubscriptionAvailability() {
        return new BillingAvailabilitySingle(context, PurchaseType.SUBSCRIPTION);
    }

    public Single<List<Sku>> obtainProductsDetails(final List<String> skus) {
        return new ProductDetailsSingle(context, PurchaseType.INAPP, skus);
    }

    public Single<List<Sku>> obtainSubscriptionsDetails(final List<String> skus) {
        return new ProductDetailsSingle(context, PurchaseType.SUBSCRIPTION, skus);
    }

    public Single<Order> purchaseProduct(final String sku, final String payload) {
        return new PerformPurchaseSingle(context, PurchaseType.INAPP, sku, payload);
    }

    public Single<Order> purchaseSubscription(final String sku, final String payload) {
        return new PerformPurchaseSingle(context, PurchaseType.SUBSCRIPTION, sku, payload);
    }

    public Single<List<PurchasedItem>> obtainPurchasedProducts() {
        return new PurchasedItemsSingle(context, PurchaseType.INAPP);
    }

    public Single<List<PurchasedItem>> obtainPurchasedSubscriptions() {
        return new PurchasedItemsSingle(context, PurchaseType.SUBSCRIPTION);
    }

    public Completable consumeProduct(final String purchaseToken) {
        return new ConsumePurchaseCompletable(context, purchaseToken);
    }

    public static Gson gson() {
        return gson;
    }

}
