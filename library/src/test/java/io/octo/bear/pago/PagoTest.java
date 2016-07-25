package io.octo.bear.pago;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.octo.bear.pago.model.entity.Inventory;
import io.octo.bear.pago.model.entity.PurchaseType;
import rx.observers.TestSubscriber;

import static io.octo.bear.pago.BillingServiceUtils.RESPONSE_CODE;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_SKU;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

/**
 * Created by shc on 21.07.16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP,
        shadows = {ShadowIInAppBillingServiceStub.class}
)
public class PagoTest {

    static final String PACKAGE_NAME = RuntimeEnvironment.application.getPackageName();

    @Test
    public void testPurchasesAvailabilitySingle() {
        testBillingAvailabilitySingle(PurchaseType.INAPP);
    }

    @Test
    public void testSubscriptionAvailabilitySingle() {
        testBillingAvailabilitySingle(PurchaseType.SUBSCRIPTION);
    }

    @Test
    public void testObtainProductDetailsSingle() {
        testObtainDetailsSingle(PurchaseType.INAPP);
    }

    @Test
    public void testObtainSubscriptionDetailsSingle() {
        testObtainDetailsSingle(PurchaseType.SUBSCRIPTION);
    }

    private void testBillingAvailabilitySingle(final PurchaseType type) {
        final TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        new BillingAvailabilitySingle(RuntimeEnvironment.application, type).subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertValue(true);
    }

    private void testObtainDetailsSingle(final PurchaseType type) {
        final TestSubscriber<Inventory> subscriber = new TestSubscriber<>();
        final String productId = TEST_SKU;
        new ProductDetailsSingle(RuntimeEnvironment.application, type, Collections.singletonList(productId)).subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final Inventory inventory = subscriber.getOnNextEvents().get(0);
        assertNotNull(inventory.getSku(productId));
    }

    @Test
    public void testPerformPurchaseSingle() throws InterruptedException, IntentSender.SendIntentException {

        System.out.println("Test started");

        // start client activity
        final TestActivity activity = Robolectric.setupActivity(TestActivity.class);

        // initiate purchase flow
        activity.findViewById(R.id.test_button).performClick();

        // check if BillingActivity was started within X seconds
        final Intent billingActivityIntent = getBillingActivityIntent(activity);
        assertNotNull(billingActivityIntent);
        assertNotNull(billingActivityIntent.getParcelableExtra(BillingActivity.EXTRA_BUY_INTENT));

        final BillingActivity billingActivity = Robolectric
                .buildActivity(BillingActivity.class)
                .withIntent(billingActivityIntent)
                .setup()
                .get();

        final Intent intentForResult = new Intent();
        final Intent resultIntent = new Intent()
                .putExtra(RESPONSE_CODE, 0)
                .putExtra(
                        PerformPurchaseSingle.RESPONSE_INAPP_PURCHASE_DATA,
                        String.format(MockResponse.BUY_INTENT_RESPONSE,
                                RuntimeEnvironment.application.getPackageName(),
                                TEST_SKU,
                                TEST_DEVELOPER_PAYLOAD))
                .putExtra(PerformPurchaseSingle.RESPONSE_INAPP_DATA_SIGNATURE, new Random().nextInt());
        final ShadowActivity shadowBillingActivity = Shadows.shadowOf(billingActivity);

        shadowBillingActivity.startActivityForResult(intentForResult, BillingActivity.REQUEST_CODE);
        shadowBillingActivity.receiveResult(intentForResult, Activity.RESULT_OK, resultIntent);
    }

    private Intent getBillingActivityIntent(TestActivity activity) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
        do {
            final Intent intent = shadowOf(activity).getNextStartedActivity();
            if (intent != null) {
                return intent;
            }
            Thread.sleep(100);
        } while (System.currentTimeMillis() < endTime);

        return null;
    }

}