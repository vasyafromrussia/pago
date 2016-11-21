package io.octo.bear.pago;

import android.app.Activity;

import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.exception.BillingException;
import rx.Completable;

import static io.octo.bear.pago.BillingServiceUtils.checkResponseAndThrowIfError;

/**
 * Created by shc on 18.07.16.
 */

class ConsumePurchaseCompletable extends Completable {

    ConsumePurchaseCompletable(final Activity activity, final String purchaseToken) {
        super(subscriber -> new BillingServiceConnection(activity, service -> {
            try {
                final int codeNumber = service.consumePurchase(Pago.BILLING_API_VERSION, activity.getPackageName(), purchaseToken);
                final ResponseCode code = ResponseCode.getByCode(codeNumber);

                checkResponseAndThrowIfError(code);

                subscriber.onCompleted();
            } catch (BillingException e) {
                subscriber.onError(e);
            }
        }).bindService());
    }

}
