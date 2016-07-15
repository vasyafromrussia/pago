package io.octo.bear.pago;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by shc on 11.07.16.
 * <p>
 * Activity receiving purchase flow callbacks
 */
public class BillingActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PURCHASE = 1001;

    private static final String EXTRA_PURCHASE_LISTENER = "io.octo.bear.pago:purchase.listener";
    private static final String EXTRA_INTENT_SENDER = "io.octo.bear.pago:intent.sender";

    private static final String TAG = BillingActivity.class.getSimpleName();

    private PurchaseListener purchaseListener;

    public static void start(Context context, PurchaseListener listener, IntentSender intentSender) {
        final Intent intent = new Intent(context, BillingActivity.class);
        intent.putExtra(EXTRA_PURCHASE_LISTENER, listener);
        intent.putExtra(EXTRA_INTENT_SENDER, intentSender);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle extras = getIntent().getExtras();
        this.purchaseListener = (PurchaseListener) extras.getSerializable(EXTRA_PURCHASE_LISTENER);

        try {
            final IntentSender intentSender = extras.getParcelable(EXTRA_INTENT_SENDER);
            startIntentSenderForResult(intentSender, BillingActivity.REQUEST_CODE_PURCHASE, new Intent(), 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PURCHASE) {
            if (resultCode == RESULT_OK) {
                purchaseListener.onSuccess(data);
            } else {
                purchaseListener.onError();
            }
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public interface PurchaseListener extends Serializable {

        void onSuccess(Intent result);

        void onError();

    }

}
