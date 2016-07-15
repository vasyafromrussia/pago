package io.octo.bear.pago;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import java.io.Serializable;

/**
 * Created by shc on 11.07.16.
 * <p>
 * Activity receiving billing flow callbacks
 */
public class BillingActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_PURCHASE = 1001;

    private static final String EXTRA_PURCHASE_LISTENER = "io.octo.bear.pago:purchase.listener";
    private static final String EXTRA_INTENT_SENDER = "io.octo.bear.pago:intent.sender";

    private static final String TAG = BillingActivity.class.getSimpleName();

    private IInAppBillingService inAppBillingService;
    private PurchaseListener purchaseListener;

    private final ServiceConnection billingServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: billing service connected");
            inAppBillingService = IInAppBillingService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: billing service disconnected");
            inAppBillingService = null;
        }

    };

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

        final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, billingServiceConnection, BIND_AUTO_CREATE);

        try {
            final IntentSender intentSender = extras.getParcelable(EXTRA_INTENT_SENDER);
            startIntentSenderForResult(intentSender, BillingActivity.REQUEST_CODE_PURCHASE, new Intent(), 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (inAppBillingService != null) {
            unbindService(billingServiceConnection);
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
