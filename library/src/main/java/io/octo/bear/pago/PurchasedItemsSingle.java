package io.octo.bear.pago;

import android.content.Context;

import java.util.List;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.PurchasedItem;
import rx.Single;

/**
 * Created by shc on 18.07.16.
 */

class PurchasedItemsSingle extends Single<List<PurchasedItem>> {

    PurchasedItemsSingle(final Context context, final PurchaseType type) {
        super(subscriber -> BillingServiceHelper.obtainPurchasedItems(context, type, subscriber));
    }

}
