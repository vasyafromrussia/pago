package io.octo.bear.pago;

import android.content.Intent;

/**
 * Created by shc on 14.07.16.
 */

public interface PurchaseListener {

    void onSuccess(Intent result);

    void onError();

}
