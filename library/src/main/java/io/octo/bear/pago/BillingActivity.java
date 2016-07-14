package io.octo.bear.pago;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shc on 11.07.16.
 * <p>
 * Activity receiving billing flow callbacks
 */
public class BillingActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_PURCHASE = 1001;

    private static final String TAG = BillingActivity.class.getSimpleName();

    private IInAppBillingService inAppBillingService;
    private List<PurchaseListener> purchaseListeners = new ArrayList<>();

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, billingServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (inAppBillingService != null) {
            unbindService(billingServiceConnection);
        }
    }

    public void addPurchaseListener(PurchaseListener listener) {
        purchaseListeners.add(listener);
    }

    public void removePurchaseListener(PurchaseListener listener) {
        purchaseListeners.remove(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PURCHASE) {
            for (PurchaseListener listener : purchaseListeners) {
                if (resultCode == RESULT_OK) {
                    listener.onSuccess(data);
                } else {
                    listener.onError();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public IInAppBillingService getBillingService() {
        return inAppBillingService;
    }

}
