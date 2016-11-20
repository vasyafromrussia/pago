package io.octo.bear.pago;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by shc on 18.07.16.
 */

public class BillingActivity extends Activity {

    private static final String TAG = BillingActivity.class.getSimpleName();

    static final String ACTION_PURCHASE = "io.octo.bear.pago.broadcast:purchase_success";
    static final String EXTRA_SUCCESS = "io.octo.bear.pago:extra.success";

    static final int REQUEST_CODE = 1001;

    static final String EXTRA_BUY_INTENT = "extra.buy_intent";

    static void start(@NonNull final Context context, @NonNull final PendingIntent buyIntent) {
        final Intent intent = new Intent(context, BillingActivity.class);
        intent.putExtra(EXTRA_BUY_INTENT, buyIntent);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle data = getIntent().getExtras();
        final PendingIntent buyIntent = data.getParcelable(EXTRA_BUY_INTENT);

        startPurchaseFlow(buyIntent);
    }

    private void startPurchaseFlow(PendingIntent buyIntent) {
        try {
            startIntentSenderForResult(buyIntent.getIntentSender(), REQUEST_CODE, new Intent(), 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            data.setAction(ACTION_PURCHASE);
            data.putExtra(EXTRA_SUCCESS, resultCode == RESULT_OK);
            LocalBroadcastManager.getInstance(this).sendBroadcast(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
