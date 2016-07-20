package io.octo.bear.pago;

import android.content.Context;

import java.util.List;

import io.octo.bear.pago.model.entity.Inventory;
import io.octo.bear.pago.model.entity.Order;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.PurchasedItem;
import rx.Completable;
import rx.Single;

/**
 * Created by shc on 14.07.16.
 * <p>
 * This class is entry point to the wonderful world of Play Store in-app purchases. <br/>
 * There's couple of methods for each purchase-related action (for products and subscriptions).
 * </p>
 */
public class Pago {

    static final int BILLING_API_VERSION = 3;

    private final Context context;

    /**
     * @param context the context is needed to start IAB-related services
     */
    public Pago(Context context) {
        this.context = context;
    }

    /**
     * Check if target version of billing API supports <i>in-app purchases</i>.
     *
     * @return single that emits {@code true} value if supported and Exception otherwise
     */
    public Single<Boolean> checkPurchasesAvailability() {
        return new BillingAvailabilitySingle(context, PurchaseType.INAPP);
    }

    /**
     * Check if target version of billing API supports <i>subscriptions</i>.
     *
     * @return single that emits {@code true} value if supported and Exception otherwise
     */
    public Single<Boolean> checkSubscriptionAvailability() {
        return new BillingAvailabilitySingle(context, PurchaseType.SUBSCRIPTION);
    }

    /**
     * Use this method to query details about desired <i>products</i> (title, price, descriptions etc).
     *
     * @param skus list of desired items' product IDs
     * @return {@link Inventory}, that represents collection of described products
     */
    public Single<Inventory> obtainProductsDetails(final List<String> skus) {
        return new ProductDetailsSingle(context, PurchaseType.INAPP, skus);
    }

    /**
     * Use this method to query details about desired <i>subscriptions</i> (title, price, descriptions etc).
     *
     * @param skus list of desired items' product IDs
     * @return {@link Inventory}, that represents collection of described products
     */
    public Single<Inventory> obtainSubscriptionsDetails(final List<String> skus) {
        return new ProductDetailsSingle(context, PurchaseType.SUBSCRIPTION, skus);
    }

    /**
     * Here you can start to spend your money. This method starts <i>product</i> purchase flow and performs purchase.
     *
     * @param sku     product id of desired product
     * @param payload some arbitrary data, that purchased item info will contain
     * @return {@link Single} emits {@link Order} containing purchased item info and signature
     */
    public Single<Order> purchaseProduct(final String sku, final String payload) {
        return new PerformPurchaseSingle(context, PurchaseType.INAPP, sku, payload);
    }

    /**
     * Here you can start to spend your money. This method starts <i>subscription</i> purchase flow and performs purchase.
     *
     * @param sku     product id of desired product
     * @param payload some arbitrary data, that purchased item info will contain
     * @return {@link Single} that emits {@link Order} containing purchased item info and signature
     */
    public Single<Order> purchaseSubscription(final String sku, final String payload) {
        return new PerformPurchaseSingle(context, PurchaseType.SUBSCRIPTION, sku, payload);
    }

    /**
     * Use this method to check <i>products</i>, that user has already bought.
     *
     * @return {@link Single} that emits {@link PurchasedItem} containing purchased products data
     */
    public Single<List<PurchasedItem>> obtainPurchasedProducts() {
        return new PurchasedItemsSingle(context, PurchaseType.INAPP);
    }

    /**
     * Use this method to check <i>subscription</i>, that user has already bought.
     *
     * @return {@link Single} that emits {@link PurchasedItem} containing purchased subscriptions data
     */
    public Single<List<PurchasedItem>> obtainPurchasedSubscriptions() {
        return new PurchasedItemsSingle(context, PurchaseType.SUBSCRIPTION);
    }

    /**
     * If user already owns some product, Google Play would prevent user from purchasing another product with the same
     * product id. You have to consume this purchase and make the product available again.
     * And yes, you can do it with this method. <br/>
     * <b>Note:</b> subscriptions cannot be consumed.
     *
     * @param purchaseToken token of purchased product, can be taken from {@link Order} or {@link PurchasedItem}
     * @return {@link Completable} that notifies you about either successful consumption, or error
     */
    public Completable consumeProduct(final String purchaseToken) {
        return new ConsumePurchaseCompletable(context, purchaseToken);
    }

}
