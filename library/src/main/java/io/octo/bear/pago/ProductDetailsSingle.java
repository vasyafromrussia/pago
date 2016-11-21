package io.octo.bear.pago;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import io.octo.bear.pago.model.entity.Inventory;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.entity.Sku;
import io.octo.bear.pago.model.exception.BillingException;
import rx.Single;

import static io.octo.bear.pago.BillingServiceUtils.GSON;
import static io.octo.bear.pago.BillingServiceUtils.checkResponseAndThrowIfError;
import static io.octo.bear.pago.BillingServiceUtils.retrieveResponseCode;

/**
 * Created by shc on 14.07.16.
 */

class ProductDetailsSingle extends Single<Inventory> {

    static final String RESPONSE_DETAILS_LIST = "DETAILS_LIST";
    static final String EXTRA_ITEM_ID_LIST = "ITEM_ID_LIST";

    ProductDetailsSingle(final Activity activity, final PurchaseType type, final List<String> purchaseIds) {
        super(subscriber -> new BillingServiceConnection(activity, service -> {
                    try {
                        final Bundle querySku = new Bundle();
                        querySku.putStringArrayList(EXTRA_ITEM_ID_LIST, new ArrayList<>(purchaseIds));

                        final Bundle details = service.getSkuDetails(Pago.BILLING_API_VERSION, activity.getPackageName(), type.value, querySku);
                        final ResponseCode responseCode = retrieveResponseCode(details);

                        checkResponseAndThrowIfError(responseCode);

                        final ArrayList<String> skus = details.getStringArrayList(RESPONSE_DETAILS_LIST);
                        if (skus == null) throw new RuntimeException("skus list is not supplied");

                        final Inventory inventory = new Inventory();
                        for (String serializedSku : skus) {
                            inventory.addItem(GSON.fromJson(serializedSku, Sku.class));
                        }

                        subscriber.onSuccess(inventory);
                    } catch (RemoteException | BillingException e) {
                        subscriber.onError(e);
                    }
                }).bindService()
        );
    }

}
