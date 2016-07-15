package io.octo.bear.pago.model.service;

import android.content.Context;
import android.os.RemoteException;

import java.util.List;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.Sku;
import rx.Single;

/**
 * Created by shc on 14.07.16.
 */

public class SkuDetailsObservable extends Single<List<Sku>> {

    public SkuDetailsObservable(final Context context, final PurchaseType type, final List<String> purchaseIds) {
        super(subscriber -> {
            try {
                final BillingServiceHelper billingServiceHelper = new BillingServiceHelper();
                billingServiceHelper.obtainSkuDetails(context, purchaseIds, type, subscriber::onSuccess);
            } catch (RemoteException e) {
                subscriber.onError(e);
            }
        });
    }

}
