package io.octo.bear.pago;

import android.app.Activity;
import android.content.Intent;

import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;

import java.util.concurrent.TimeUnit;

/**
 * Created by shc on 26.07.16.
 */

class BasePagoTest {

    static void receiveResultInBillingActivity(Intent billingActivityIntent, Intent result) {
        final BillingActivity billingActivity = Robolectric
                .buildActivity(BillingActivity.class)
                .withIntent(billingActivityIntent)
                .setup()
                .get();

        final ShadowActivity shadowBillingActivity = Shadows.shadowOf(billingActivity);

        shadowBillingActivity.startActivityForResult(new Intent(), BillingActivity.REQUEST_CODE);
        shadowBillingActivity.receiveResult(new Intent(), Activity.RESULT_OK, result);
    }

    static Intent getBillingActivityIntent(ShadowActivity shadowActivity, int secondsToWait)
            throws InterruptedException {

        long startTime = System.currentTimeMillis();
        long endTime = startTime + TimeUnit.MILLISECONDS.convert(secondsToWait, TimeUnit.SECONDS);
        do {
            final Intent intent = shadowActivity.getNextStartedActivity();
            if (intent != null) {
                return intent;
            }
            Thread.sleep(100);
        } while (System.currentTimeMillis() < endTime);

        return null;
    }

}
