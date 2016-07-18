package io.octo.bear.pago.model.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.octo.bear.pago.Pago;
import io.octo.bear.pago.model.entity.Purchase;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.PurchasedItem;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.entity.Sku;
import io.octo.bear.pago.model.exception.BillingException;

/**
 * Created by shc on 15.07.16.
 */
final class BillingServiceHelper {

    private static final String EXTRA_ITEM_ID_LIST = "ITEM_ID_LIST";

    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String RESPONSE_DETAILS_LIST = "DETAILS_LIST";
    private static final String RESPONSE_BUY_INTENT = "BUY_INTENT";

    private static final String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    private static final String RESPONSE_INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE";

    private static final String RESPONSE_INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    private static final String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    private static final String RESPONSE_INAPP_PURCHASE_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    private static final String RESPONSE_INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    static void obtainSkuDetails(
            final Context context, final List<String> purchaseIds, final PurchaseType type, final ResultSupplier<List<Sku>> listener)
            throws RemoteException {

        new BillingServiceConnection(context, service -> {
            final Bundle querySku = new Bundle();
            querySku.putStringArrayList(EXTRA_ITEM_ID_LIST, new ArrayList<>(purchaseIds));

            final Bundle details = service.getSkuDetails(Pago.BILLING_API_VERSION, context.getPackageName(), type.value, querySku);
            final ResponseCode responseCode = ResponseCode.getByCode(details.getInt(RESPONSE_CODE));

            if (responseCode == ResponseCode.OK) {
                final ArrayList<String> skus = details.getStringArrayList(RESPONSE_DETAILS_LIST);
                if (skus == null) throw new RuntimeException("skus list is not supplied");

                final List<Sku> result = new ArrayList<>();
                for (String serializedSku : skus) {
                    result.add(Pago.gson().fromJson(serializedSku, Sku.class));
                }

                listener.onSuccess(result);
            } else {
                throw new BillingException(responseCode);
            }
        }).bindService();
    }

    static void purchaseItem(
            final Context context, final String sku, final PurchaseType type, final ResultSupplier<Purchase> listener)
            throws RemoteException {

        new BillingServiceConnection(context, service -> {
            final String payload = UUID.randomUUID().toString();
            final Bundle buyIntentBundle = service.getBuyIntent(Pago.BILLING_API_VERSION, context.getPackageName(),
                    sku, type.value, payload);

            final ResponseCode responseCode = ResponseCode.getByCode(buyIntentBundle.getInt(RESPONSE_CODE));

            if (responseCode != ResponseCode.OK) {
                throw new BillingException(responseCode);
            }

            final PendingIntent buyIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT);
            if (buyIntent == null) {
                throw new RuntimeException("unable to retrieve buy intent");
            }

            final IntentSender intentSender = buyIntent.getIntentSender();
            try {
                intentSender.sendIntent(context, 1001, new Intent(),
                        (sender, intent, i, s, result) -> {
                            final ResponseCode code = ResponseCode.getByCode(result.getInt(RESPONSE_CODE, 0));

                            if (code == ResponseCode.OK) {
                                final Purchase purchase = Pago.gson().fromJson(result.getString(RESPONSE_INAPP_PURCHASE_DATA), Purchase.class);
                                final boolean purchaseDataIsCorrect = TextUtils.equals(payload, purchase.developerPayload);

                                if (purchaseDataIsCorrect) {
                                    listener.onSuccess(purchase);
                                } else {
                                    throw new RuntimeException("purchase data doesn't match with data that was sent in request");
                                }
                            } else {
                                throw new BillingException(code);
                            }
                        }, null);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }).bindService();
    }

    // TODO: 18.07.16 use continuation token
    static void obtainPurchasedItems(
            final Context context, final PurchaseType purchaseType, final ResultSupplier<List<PurchasedItem>> listener) {

        new BillingServiceConnection(context, service -> {
            final Bundle purchases =
                    service.getPurchases(Pago.BILLING_API_VERSION, context.getPackageName(), purchaseType.value, null);

            final ResponseCode code = ResponseCode.getByCode(purchases.getInt(RESPONSE_CODE));

            if (code != ResponseCode.OK) {
                throw new BillingException(code);
            }

            final List<String> data = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST);
            final List<String> skus = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_ITEM_LIST);
            final List<String> signatures = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_SIGNATURE_LIST);

            final List<PurchasedItem> result = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                result.add(new PurchasedItem(
                        Pago.gson().fromJson(data.get(i), Purchase.class),
                        signatures.get(i),
                        skus.get(i)));
            }
            listener.onSuccess(result);

        }).bindService();
    }

    static void consumePurchase(
            final Context context, final String purchaseToken, final ResultSupplier<Void> listener) {

        new BillingServiceConnection(context, service -> {
            final int codeNumber = service.consumePurchase(Pago.BILLING_API_VERSION, context.getPackageName(), purchaseToken);
            final ResponseCode code = ResponseCode.getByCode(codeNumber);

            if (code == ResponseCode.OK) {
                listener.onSuccess(null);
            } else {
                throw new BillingException(code);
            }
        }).bindService();
    }

    interface ResultSupplier<T> {
        void onSuccess(T result);
    }

}
