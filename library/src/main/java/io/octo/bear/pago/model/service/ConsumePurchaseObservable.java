package io.octo.bear.pago.model.service;

import android.content.Context;

import rx.Single;

/**
 * Created by shc on 18.07.16.
 */

public class ConsumePurchaseObservable extends Single<Void> {

    public ConsumePurchaseObservable(final Context context, final String purchaseToken) {
        super(subscriber -> BillingServiceHelper.consumePurchase(context, purchaseToken, subscriber));
    }

}
