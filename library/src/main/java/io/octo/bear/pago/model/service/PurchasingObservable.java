package io.octo.bear.pago.model.service;

import android.content.Context;
import android.os.RemoteException;

import io.octo.bear.pago.model.entity.Order;
import io.octo.bear.pago.model.entity.Purchase;
import io.octo.bear.pago.model.entity.PurchaseType;
import rx.Single;

/**
 * Created by shc on 14.07.16.
 */

public class PurchasingObservable extends Single<Order> {

    public PurchasingObservable(final Context context, final PurchaseType type, final String sku, String payload) {
        super(subscriber -> BillingServiceHelper.purchaseItem(context, sku, type, payload, subscriber));
    }

}
