package io.octo.bear.pago.model.service;

import android.content.Context;

import io.octo.bear.pago.model.entity.PurchaseType;
import rx.Single;

/**
 * Created by shc on 19.07.16.
 */

public class BillingAvailabiliyObservable extends Single<Boolean> {

    public BillingAvailabiliyObservable(final Context context, final PurchaseType type) {
        super(subscriber -> BillingServiceHelper.isBillingSupported(context, type, subscriber));
    }

}
