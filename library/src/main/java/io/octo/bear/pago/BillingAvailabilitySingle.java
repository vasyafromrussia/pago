package io.octo.bear.pago;

import android.content.Context;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.exception.BillingException;
import rx.Single;

/**
 * Created by shc on 19.07.16.
 */

class BillingAvailabilitySingle extends Single<Boolean> {

    BillingAvailabilitySingle(final Context context, final PurchaseType type) {
        super((OnSubscribe<Boolean>) subscriber -> new BillingServiceConnection(context, service -> {
                    try {
                        final int codeNumber = service.isBillingSupported(Pago.BILLING_API_VERSION, context.getPackageName(), type.value);
                        final ResponseCode code = ResponseCode.getByCode(codeNumber);

                        if (code == ResponseCode.OK) {
                            subscriber.onSuccess(true);
                        } else {
                            throw new BillingException(ResponseCode.BILLING_UNAVAILABLE);
                        }
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }).bindService()
        );
    }

}
