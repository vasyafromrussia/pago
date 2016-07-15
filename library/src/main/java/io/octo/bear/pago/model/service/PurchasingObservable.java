package io.octo.bear.pago.model.service;

import android.content.Context;
import android.os.RemoteException;

import io.octo.bear.pago.model.entity.Purchase;
import io.octo.bear.pago.model.entity.PurchaseType;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by shc on 14.07.16.
 */

public class PurchasingObservable extends Single<Purchase> {

    public PurchasingObservable(final Context context, final PurchaseType type, final String sku) {
        super(new OnSubscribe<Purchase>() {
            @Override
            public void call(final SingleSubscriber<? super Purchase> subscriber) {
                try {
                    final BillingServiceHelper billingServiceHelper = new BillingServiceHelper();
                    billingServiceHelper.purchaseItem(context, sku, type, new BillingServiceHelper.PurchaseSuccessListener() {
                        @Override
                        public void onSuccess(Purchase purchase) {
                            subscriber.onSuccess(purchase);
                        }
                    });
                } catch (RemoteException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

}
