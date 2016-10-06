package io.octo.bear.pago;

import android.content.Context;

import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.exception.BillingException;
import rx.Completable;
import rx.Single;

import static io.octo.bear.pago.BillingServiceUtils.checkResponseAndThrowIfError;

/**
 * Created by shc on 18.07.16.
 */

class ConsumePurchase {

    public static Completable create(final Context context, final String purchaseToken) {
        return Completable.create(subscriber -> new BillingServiceConnection(context, service -> {
            try {
                final int codeNumber = service.consumePurchase(Pago.BILLING_API_VERSION, context.getPackageName(), purchaseToken);
                final ResponseCode code = ResponseCode.getByCode(codeNumber);

                checkResponseAndThrowIfError(code);

                subscriber.onCompleted();
            } catch (BillingException e) {
                subscriber.onError(e);
            }
        }).bindService());
    }

}
