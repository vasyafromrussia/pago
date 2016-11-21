package io.octo.bear.pago;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.Collections;
import java.util.List;

import io.octo.bear.pago.model.entity.Inventory;
import io.octo.bear.pago.model.entity.Order;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.exception.BillingException;
import rx.observers.TestSubscriber;

import static io.octo.bear.pago.BillingServiceTestingUtils.OWNED_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.BillingServiceTestingUtils.OWNED_SKU;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_SKU;
import static io.octo.bear.pago.BillingServiceUtils.RESPONSE_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shc on 21.07.16.
 *
 * Test set for {@link com.android.vending.billing.IInAppBillingService} error responses.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP,
        shadows = {
                ShadowFaultyIInAppBillingServiceStub.class
        }
)
public class PagoErrorsTest extends BasePagoTest {

    @Test
    public void testErrorDuringPurchaseFlow() throws IntentSender.SendIntentException, InterruptedException {
        TestActivity testActivity = Robolectric.buildActivity(TestActivity.class).create().get();

        final ShadowActivity shadowActivity = new ShadowActivity();

        //start purchase flow
        final TestSubscriber<Order> subscriber = new TestSubscriber<>();
        final PerformPurchaseSingle performPurchaseSingle = new PerformPurchaseSingle(
                testActivity,
                PurchaseType.INAPP,
                TEST_SKU,
                TEST_DEVELOPER_PAYLOAD
        );
        performPurchaseSingle.subscribe(subscriber);

        // check if BillingActivity was started within X seconds
        final Intent billingActivityIntent = getBillingActivityIntent(shadowActivity, 10);
        assertNotNull(billingActivityIntent);
        assertNotNull(billingActivityIntent.getParcelableExtra(BillingActivity.EXTRA_BUY_INTENT));

        receiveResultInBillingActivity(billingActivityIntent, getErrorIntent());

        subscriber.assertError(BillingException.class);
        final BillingException exception = (BillingException) subscriber.getOnErrorEvents().get(0);
        assertEquals(ResponseCode.ERROR, exception.getCode());
    }

    @Test
    public void testPurchaseOwnedProduct() throws InterruptedException, IntentSender.SendIntentException {
        TestActivity testActivity = Robolectric.buildActivity(TestActivity.class).create().get();

        //start purchase flow
        final TestSubscriber<Order> subscriber = new TestSubscriber<>();
        final PerformPurchaseSingle performPurchaseSingle = new PerformPurchaseSingle(
                testActivity,
                PurchaseType.INAPP,
                OWNED_SKU,
                OWNED_DEVELOPER_PAYLOAD
        );
        performPurchaseSingle.subscribe(subscriber);

        subscriber.assertError(BillingException.class);
        final BillingException exception = (BillingException) subscriber.getOnErrorEvents().get(0);
        assertEquals(exception.getCode(), ResponseCode.ITEM_ALREADY_OWNED);
    }

    @Test
    public void testErrorOnConsumption() {
        TestActivity testActivity = Robolectric.buildActivity(TestActivity.class).create().get();
        final TestSubscriber<Void> subscriber = new TestSubscriber<>();
        new ConsumePurchaseCompletable(testActivity, null).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

    @Test
    public void testErrorOnRequestPurchasedItemsList() {
        TestActivity testActivity = Robolectric.buildActivity(TestActivity.class).create().get();
        final TestSubscriber<List<Order>> subscriber = new TestSubscriber<>();
        new PurchasedItemsSingle(testActivity, PurchaseType.INAPP).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

    @Test
    public void testPurchasesAreNotAvailable() {
        TestActivity testActivity = Robolectric.buildActivity(TestActivity.class).create().get();
        final TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        new BillingAvailabilitySingle(testActivity, PurchaseType.INAPP).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
        final BillingException exception = (BillingException) subscriber.getOnErrorEvents().get(0);
        assertEquals(ResponseCode.BILLING_UNAVAILABLE, exception.getCode());
    }

    @Test
    public void testErrorWhileRequestProductDetails() {
        TestActivity testActivity = Robolectric.buildActivity(TestActivity.class).create().get();
        final TestSubscriber<Inventory> subscriber = new TestSubscriber<>();
        new ProductDetailsSingle(testActivity, PurchaseType.INAPP, Collections.singletonList(TEST_SKU)).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

    private static Intent getErrorIntent() {
        return new Intent().putExtra(RESPONSE_CODE, ResponseCode.ERROR.code);
    }

}