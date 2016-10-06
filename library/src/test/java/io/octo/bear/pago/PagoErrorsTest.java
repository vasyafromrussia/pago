package io.octo.bear.pago;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
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

import static io.octo.bear.pago.BillingServiceUtils.RESPONSE_CODE;
import static io.octo.bear.pago.BillingServiceTestingUtils.OWNED_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.BillingServiceTestingUtils.OWNED_SKU;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_SKU;
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
        final ShadowActivity shadowActivity = new ShadowActivity();

        //start purchase flow
        final TestSubscriber<Order> subscriber = new TestSubscriber<>();
        PerformPurchase.create(
                RuntimeEnvironment.application,
                PurchaseType.INAPP,
                TEST_SKU,
                TEST_DEVELOPER_PAYLOAD
        ).subscribe(subscriber);

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
        //start purchase flow
        final TestSubscriber<Order> subscriber = new TestSubscriber<>();
        PerformPurchase.create(
                RuntimeEnvironment.application,
                PurchaseType.INAPP,
                OWNED_SKU,
                OWNED_DEVELOPER_PAYLOAD
        ).subscribe(subscriber);

        subscriber.assertError(BillingException.class);
        final BillingException exception = (BillingException) subscriber.getOnErrorEvents().get(0);
        assertEquals(exception.getCode(), ResponseCode.ITEM_ALREADY_OWNED);
    }

    @Test
    public void testErrorOnConsumption() {
        final TestSubscriber<Void> subscriber = new TestSubscriber<>();
        ConsumePurchase.create(RuntimeEnvironment.application, null).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

    @Test
    public void testErrorOnRequestPurchasedItemsList() {
        final TestSubscriber<List<Order>> subscriber = new TestSubscriber<>();
        PurchasedItems.create(RuntimeEnvironment.application, PurchaseType.INAPP).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

    @Test
    public void testPurchasesAreNotAvailable() {
        final TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        BillingAvailability.create(RuntimeEnvironment.application, PurchaseType.INAPP).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
        final BillingException exception = (BillingException) subscriber.getOnErrorEvents().get(0);
        assertEquals(ResponseCode.BILLING_UNAVAILABLE, exception.getCode());
    }

    @Test
    public void testErrorWhileRequestProductDetails() {
        final TestSubscriber<Inventory> subscriber = new TestSubscriber<>();
        ProductDetails.create(RuntimeEnvironment.application, PurchaseType.INAPP, Collections.singletonList(TEST_SKU))
                .subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

    private static Intent getErrorIntent() {
        return new Intent().putExtra(RESPONSE_CODE, ResponseCode.ERROR.code);
    }

}