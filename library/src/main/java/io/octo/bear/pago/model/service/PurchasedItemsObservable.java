package io.octo.bear.pago.model.service;

import android.content.Context;

import java.util.List;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.PurchasedItem;
import rx.Single;

/**
 * Created by shc on 18.07.16.
 */

public class PurchasedItemsObservable extends Single<List<PurchasedItem>> {

    public PurchasedItemsObservable(final Context context, final PurchaseType type) {
        super(subscriber -> {
            try {
                BillingServiceHelper.obtainPurchasedItems(context, type, subscriber::onSuccess);
            } catch (Throwable e) {
                subscriber.onError(e);
            }
        });
    }

}
