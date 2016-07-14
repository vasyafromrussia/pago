package io.octo.bear.pago.model.service;

import android.os.Bundle;
import android.os.RemoteException;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.octo.bear.pago.BillingActivity;
import io.octo.bear.pago.Pago;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.entity.Sku;
import io.octo.bear.pago.model.exception.BillingException;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by shc on 14.07.16.
 */

public class SkuDetailsObservable extends Single<List<Sku>> {

    private static final String EXTRA_ITEM_ID_LIST = "ITEM_ID_LIST";

    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String RESPONSE_DETAILS_LIST = "DETAILS_LIST";

    public SkuDetailsObservable(
            final BillingActivity billingActivity, final Gson gson, final PurchaseType type, final List<String> purchaseIds) {

        super(new OnSubscribe<List<Sku>>() {
            @Override
            public void call(SingleSubscriber<? super List<Sku>> subscriber) {
                final Bundle querySku = new Bundle();
                querySku.putStringArrayList(EXTRA_ITEM_ID_LIST, new ArrayList<>(purchaseIds));

                try {
                    final Bundle details = billingActivity.getBillingService().getSkuDetails(
                            Pago.BILLING_API_VERSION, billingActivity.getPackageName(), type.value, querySku);
                    final ResponseCode responseCode = ResponseCode.getByCode(details.getInt(RESPONSE_CODE));

                    if (responseCode == ResponseCode.OK) {
                        final ArrayList<String> skus = details.getStringArrayList(RESPONSE_DETAILS_LIST);
                        final List<Sku> result = new ArrayList<>();

                        for (String serializedSku : skus) {
                            result.add(gson.fromJson(serializedSku, Sku.class));
                        }

                        subscriber.onSuccess(result);
                    } else {
                        throw new BillingException(responseCode);
                    }

                } catch (RemoteException e) {
                    subscriber.onError(e);
                }
            }
        });

    }

}
