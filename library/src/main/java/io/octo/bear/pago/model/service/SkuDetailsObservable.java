package io.octo.bear.pago.model.service;

import android.content.Context;
import android.os.RemoteException;

import java.util.List;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.Sku;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by shc on 14.07.16.
 */

public class SkuDetailsObservable extends Single<List<Sku>> {

    public SkuDetailsObservable(final Context context, final PurchaseType type, final List<String> purchaseIds) {

        super(new OnSubscribe<List<Sku>>() {
            @Override
            public void call(SingleSubscriber<? super List<Sku>> subscriber) {
                try {
                    final BillingServiceHelper billingServiceHelper = new BillingServiceHelper();
                    final List<Sku> skuDetails = billingServiceHelper.getSkuDetails(context, purchaseIds, type);

                    subscriber.onSuccess(skuDetails);
                } catch (RemoteException e) {
                    subscriber.onError(e);
                }
            }
        });

    }

}
