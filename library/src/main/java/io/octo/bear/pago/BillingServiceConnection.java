package io.octo.bear.pago;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by shc on 15.07.16.
 */

class BillingServiceConnection implements ServiceConnection {

    private static final String TAG = BillingServiceConnection.class.getSimpleName();

    private IInAppBillingService inAppBillingService;
    private ServiceConnectionListener listener;
    private Context context;

    BillingServiceConnection(Context context, ServiceConnectionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    void bindService() {
        final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        context.bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }

    private void unbindService(Context context) {
        if (inAppBillingService != null) {
            context.unbindService(this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: billing service connected");
        inAppBillingService = IInAppBillingService.Stub.asInterface(iBinder);
        try {
            listener.onServiceConnected(inAppBillingService);
        } catch (RemoteException e) {
            Log.e(TAG, "onServiceConnected: ", e);
        } finally {
            unbindService(context);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceDisconnected: billing service disconnected");
        inAppBillingService = null;
    }

    interface ServiceConnectionListener {
        void onServiceConnected(IInAppBillingService service) throws RemoteException;
    }

}
