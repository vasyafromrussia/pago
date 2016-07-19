package io.octo.bear.pago;

import android.content.Context;

import io.octo.bear.pago.model.entity.Order;
import io.octo.bear.pago.model.entity.PurchaseType;
import rx.Single;

/**
 * Created by shc on 14.07.16.
 */

class PerformPurchaseSingle extends Single<Order> {

    PerformPurchaseSingle(final Context context, final PurchaseType type, final String sku, String payload) {
        super(subscriber -> BillingServiceHelper.purchaseItem(context, sku, type, payload, subscriber));
    }

}
