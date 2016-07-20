package io.octo.bear.pago;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import io.octo.bear.pago.model.entity.Purchase;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.PurchasedItem;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.exception.BillingException;
import rx.Single;

import static io.octo.bear.pago.BillingServiceUtils.GSON;
import static io.octo.bear.pago.BillingServiceUtils.checkResponseAndThrowIfError;
import static io.octo.bear.pago.BillingServiceUtils.retrieveResponseCode;

/**
 * Created by shc on 18.07.16.
 */

class PurchasedItemsSingle extends Single<List<PurchasedItem>> {

    private static final String RESPONSE_INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    private static final String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static final String RESPONSE_INAPP_PURCHASE_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    private static final String RESPONSE_INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    PurchasedItemsSingle(final Context context, final PurchaseType type) {
        super(subscriber ->
                new BillingServiceConnection(context, service -> {
                    try {
                        final Bundle purchases =
                                service.getPurchases(Pago.BILLING_API_VERSION, context.getPackageName(), type.value, null);

                        final ResponseCode code = retrieveResponseCode(purchases);

                        checkResponseAndThrowIfError(code);

                        final List<PurchasedItem> result = new ArrayList<>();

                        final List<String> data = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST);
                        final List<String> skus = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_ITEM_LIST);
                        final List<String> signatures = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_SIGNATURE_LIST);

                        for (int i = 0; i < data.size(); i++) {
                            result.add(new PurchasedItem(
                                    GSON.fromJson(data.get(i), Purchase.class),
                                    signatures.get(i),
                                    skus.get(i)));
                        }
                        subscriber.onSuccess(result);
                    } catch (BillingException e) {
                        subscriber.onError(e);
                    }
                }).bindService()
        );
    }

}
