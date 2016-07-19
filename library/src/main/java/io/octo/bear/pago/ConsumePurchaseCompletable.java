package io.octo.bear.pago;

import android.content.Context;

import rx.Completable;

/**
 * Created by shc on 18.07.16.
 */

class ConsumePurchaseCompletable extends Completable {

    ConsumePurchaseCompletable(final Context context, final String purchaseToken) {
        super(subscriber -> BillingServiceHelper.consumePurchase(context, purchaseToken, subscriber));
    }

}
