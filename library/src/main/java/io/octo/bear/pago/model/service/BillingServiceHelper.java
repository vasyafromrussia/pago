package io.octo.bear.pago.model.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.octo.bear.pago.BillingActivity;
import io.octo.bear.pago.Pago;
import io.octo.bear.pago.model.entity.Purchase;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.PurchasedItem;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.entity.Sku;
import io.octo.bear.pago.model.exception.BillingException;
import rx.SingleSubscriber;

/**
 * Created by shc on 15.07.16.
 */
final class BillingServiceHelper {

    private static final String TAG = BillingServiceHelper.class.getSimpleName();

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
            final Context context, final List<String> purchaseIds, final PurchaseType type, final SingleSubscriber<? super List<Sku>> subscriber) {

        new BillingServiceConnection(context, service -> {
            try {
                final Bundle querySku = new Bundle();
                querySku.putStringArrayList(EXTRA_ITEM_ID_LIST, new ArrayList<>(purchaseIds));

                final Bundle details = service.getSkuDetails(Pago.BILLING_API_VERSION, context.getPackageName(), type.value, querySku);
                final ResponseCode responseCode = retrieveResponseCode(details);

                checkResponseAndThrowIfError(responseCode);

                final ArrayList<String> skus = details.getStringArrayList(RESPONSE_DETAILS_LIST);
                if (skus == null) throw new RuntimeException("skus list is not supplied");

                final List<Sku> result = new ArrayList<>();
                for (String serializedSku : skus) {
                    result.add(Pago.gson().fromJson(serializedSku, Sku.class));
                }

                subscriber.onSuccess(result);
            } catch (RemoteException | BillingException e) {
                subscriber.onError(e);
            }
        }).bindService();
    }

    static void purchaseItem(
            final Context context, final String sku, final PurchaseType type, final SingleSubscriber<? super Purchase> subscriber) {

        new BillingServiceConnection(context, service -> {
            try {
                final String payload = UUID.randomUUID().toString();
                final Bundle buyIntentBundle = service.getBuyIntent(Pago.BILLING_API_VERSION, context.getPackageName(),
                        sku, type.value, payload);

                final ResponseCode responseCode = retrieveResponseCode(buyIntentBundle);

                checkResponseAndThrowIfError(responseCode);

                final PendingIntent buyIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT);
                if (buyIntent == null) {
                    throw new RuntimeException("unable to retrieve buy intent");
                }

                LocalBroadcastManager
                        .getInstance(context)
                        .registerReceiver(
                                createPurchaseBroadcastReceiver(payload, subscriber),
                                new IntentFilter(BillingActivity.ACTION_PURCHASE_SUCCESS));

                BillingActivity.start(context, buyIntent);
            } catch (BillingException e) {
                subscriber.onError(e);
            }

        }).bindService();
    }

    // TODO: 18.07.16 use continuation token
    static void obtainPurchasedItems(
            final Context context, final PurchaseType purchaseType, final SingleSubscriber<? super List<PurchasedItem>> subscriber) {

        new BillingServiceConnection(context, service -> {
            try {
                final Bundle purchases =
                        service.getPurchases(Pago.BILLING_API_VERSION, context.getPackageName(), purchaseType.value, null);

                final ResponseCode code = retrieveResponseCode(purchases);

                checkResponseAndThrowIfError(code);

                final List<PurchasedItem> result = new ArrayList<>();

                final List<String> data = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST);
                final List<String> skus = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_ITEM_LIST);
                final List<String> signatures = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_SIGNATURE_LIST);

                for (int i = 0; i < data.size(); i++) {
                    result.add(new PurchasedItem(
                            Pago.gson().fromJson(data.get(i), Purchase.class),
                            signatures.get(i),
                            skus.get(i)));
                }
                subscriber.onSuccess(result);
            } catch (BillingException e) {
                subscriber.onError(e);
            }
        }).bindService();
    }

    static void consumePurchase(
            final Context context, final String purchaseToken, final SingleSubscriber<? super Void> subscriber) {

        new BillingServiceConnection(context, service -> {
            try {
                final int codeNumber = service.consumePurchase(Pago.BILLING_API_VERSION, context.getPackageName(), purchaseToken);
                final ResponseCode code = ResponseCode.getByCode(codeNumber);

                checkResponseAndThrowIfError(code);

                subscriber.onSuccess(null);
            } catch (BillingException e) {
                subscriber.onError(e);
            }
        }).bindService();
    }

    private static ResponseCode retrieveResponseCode(final Bundle result) {
        return ResponseCode.getByCode(result.getInt(RESPONSE_CODE));
    }

    private static void checkResponseAndThrowIfError(ResponseCode code) throws BillingException {
        if (code != ResponseCode.OK) throw new BillingException(code);
    }

    private static BroadcastReceiver createPurchaseBroadcastReceiver
            (final String payload, final SingleSubscriber<? super Purchase> subscriber) {

        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent data) {
                try {
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(this);

                    final Bundle result = data.getExtras();
                    final ResponseCode code = retrieveResponseCode(result);

                    checkResponseAndThrowIfError(code);

                    final Purchase purchase = Pago.gson().fromJson(result.getString(RESPONSE_INAPP_PURCHASE_DATA), Purchase.class);
                    final boolean purchaseDataIsCorrect = TextUtils.equals(payload, purchase.developerPayload);

                    if (purchaseDataIsCorrect) {
                        subscriber.onSuccess(purchase);
                    } else {
                        throw new RuntimeException("purchase data doesn't match with data that was sent in request");
                    }
                } catch (BillingException e) {
                    subscriber.onError(e);
                }
            }
        };
    }

}
