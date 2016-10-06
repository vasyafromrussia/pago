package io.octo.bear.pago;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.exception.BillingException;
import rx.Single;

/**
 * Created by shc on 19.07.16.
 */
class BillingAvailability {

    public static Single<Boolean> create(final Context context, final PurchaseType type) {
        return Single.create(subscriber -> {
                    Log.i("DensTest", "out thread " + Thread.currentThread());
                    new BillingServiceConnection(
                            context,
                            service -> {
                                Log.i("DensTest", "int thread " + Thread.currentThread());
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

                            }
                    ).bindService();
                }
        );
    }

}
