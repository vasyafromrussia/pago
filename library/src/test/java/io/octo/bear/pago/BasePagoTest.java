/*
 * Copyright (C) 2017 Vasily Styagov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
