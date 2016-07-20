package io.octo.bear.pago;

import android.content.Context;

import java.util.List;

import io.octo.bear.pago.model.entity.Inventory;
import io.octo.bear.pago.model.entity.PurchaseType;
import rx.Single;

/**
 * Created by shc on 14.07.16.
 */

class ProductDetailsSingle extends Single<Inventory> {

    ProductDetailsSingle(final Context context, final PurchaseType type, final List<String> purchaseIds) {
        super(subscriber -> BillingServiceHelper.obtainSkuDetails(context, purchaseIds, type, subscriber));
    }

}
