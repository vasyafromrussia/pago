package io.octo.bear.pago.model.service;

import android.content.Context;

import java.util.List;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.Sku;
import rx.Single;

/**
 * Created by shc on 14.07.16.
 */

public class SkuDetailsObservable extends Single<List<Sku>> {

    public SkuDetailsObservable(final Context context, final PurchaseType type, final List<String> purchaseIds) {
        super(subscriber -> BillingServiceHelper.obtainSkuDetails(context, purchaseIds, type, subscriber));
    }

}
