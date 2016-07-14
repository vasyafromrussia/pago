package io.octo.bear.pago.model.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import com.google.gson.Gson;

import io.octo.bear.pago.BillingActivity;
import io.octo.bear.pago.Pago;
import io.octo.bear.pago.PurchaseListener;
import io.octo.bear.pago.model.entity.Purchase;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.exception.BillingException;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by shc on 14.07.16.
 */

public class PurchasingObservable extends Single<Purchase> {

    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    private static final String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    private static final String RESPONSE_INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE";

    public PurchasingObservable(final BillingActivity billingActivity, final Gson gson, final PurchaseType type, final String sku) {
        super(new OnSubscribe<Purchase>() {
            @Override
            public void call(SingleSubscriber<? super Purchase> subscriber) {
                try {
                    // TODO: 14.07.16 generate unique value
                    final String payload = String.valueOf(System.currentTimeMillis());
                    final Bundle buyIntentBundle = billingActivity.getBillingService()
                            .getBuyIntent(Pago.BILLING_API_VERSION, billingActivity.getPackageName(), sku, type.value, payload);

                    final ResponseCode responseCode = ResponseCode.getByCode(buyIntentBundle.getInt(RESPONSE_CODE));
                    if (responseCode == ResponseCode.OK) {
                        final PendingIntent buyIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT);

                        billingActivity.addPurchaseListener(new Listener(billingActivity, gson, subscriber, payload));
                        billingActivity.startIntentSenderForResult(
                                buyIntent.getIntentSender(), BillingActivity.REQUEST_CODE_PURCHASE, new Intent(), 0, 0, 0);
                    } else {
                        throw new BillingException(responseCode);
                    }

                } catch (RemoteException | IntentSender.SendIntentException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static class Listener implements PurchaseListener {

        private final BillingActivity billingActivity;
        private final Gson gson;
        private final SingleSubscriber<? super Purchase> subscriber;
        private final String payload;

        Listener(BillingActivity billingActivity, Gson gson, SingleSubscriber<? super Purchase> subscriber, String payload) {
            this.billingActivity = billingActivity;
            this.gson = gson;
            this.subscriber = subscriber;
            this.payload = payload;
        }

        @Override
        public void onSuccess(Intent result) {
            billingActivity.removePurchaseListener(this);
            // TODO: 14.07.16 each observable parses this result to figure out if it belongs to this observable.
            // More handy method is needed.

            final ResponseCode code = ResponseCode.getByCode(result.getIntExtra(RESPONSE_CODE, 0));

            if (code == ResponseCode.OK) {
                final Purchase purchase = gson.fromJson(result.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA), Purchase.class);
                final boolean purchaseDataIsCorrect = TextUtils.equals(payload, purchase.developerPayload);

                if (purchaseDataIsCorrect) {
                    subscriber.onSuccess(purchase);
                } else {
                    subscriber.onError(new RuntimeException("purchase data doesn't match with data that was sent in request"));
                }
            } else {
                subscriber.onError(new BillingException(code));
            }
        }

        @Override
        public void onError() {
            billingActivity.removePurchaseListener(this);
            subscriber.onError(new RuntimeException());
        }

    }

}
