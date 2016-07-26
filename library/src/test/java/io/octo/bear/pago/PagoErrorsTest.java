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

import static io.octo.bear.pago.MockUtils.ERROR_SKU;
import static io.octo.bear.pago.MockUtils.OWNED_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.MockUtils.OWNED_SKU;
import static io.octo.bear.pago.MockUtils.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.MockUtils.TEST_SKU;
import static io.octo.bear.pago.MockUtils.getBillingActivityIntent;
import static io.octo.bear.pago.MockUtils.receiveResultInBillingActivity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shc on 21.07.16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP,
        shadows = {
                ShadowFaultyIInAppBillingServiceStub.class
        }
)
public class PagoErrorsTest {

    @Test
    public void testErrorDuringPurchaseFlow() throws IntentSender.SendIntentException, InterruptedException {
        final ShadowActivity shadowActivity = new ShadowActivity();

        //start purchase flow
        final TestSubscriber<Order> subscriber = new TestSubscriber<>();
        final PerformPurchaseSingle performPurchaseSingle = new PerformPurchaseSingle(
                RuntimeEnvironment.application,
                PurchaseType.INAPP,
                TEST_SKU,
                TEST_DEVELOPER_PAYLOAD
        );
        performPurchaseSingle.subscribe(subscriber);

        // check if BillingActivity was started within X seconds
        final Intent billingActivityIntent = getBillingActivityIntent(shadowActivity);
        assertNotNull(billingActivityIntent);
        assertNotNull(billingActivityIntent.getParcelableExtra(BillingActivity.EXTRA_BUY_INTENT));

        receiveResultInBillingActivity(billingActivityIntent, MockResponse.PURCHASE_ERROR);

        subscriber.assertError(BillingException.class);
        final BillingException exception = (BillingException) subscriber.getOnErrorEvents().get(0);
        assertEquals(ResponseCode.ERROR, exception.getCode());
    }

    @Test
    public void testPurchaseOwnedProduct() throws InterruptedException, IntentSender.SendIntentException {
        //start purchase flow
        final TestSubscriber<Order> subscriber = new TestSubscriber<>();
        final PerformPurchaseSingle performPurchaseSingle = new PerformPurchaseSingle(
                RuntimeEnvironment.application,
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
        final TestSubscriber<Void> subscriber = new TestSubscriber<>();
        new ConsumePurchaseCompletable(RuntimeEnvironment.application, null).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

    @Test
    public void testErrorOnObtainedPurchasedItemsList() {
        final TestSubscriber<List<Order>> subscriber = new TestSubscriber<>();
        new PurchasedItemsSingle(RuntimeEnvironment.application, PurchaseType.INAPP).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

    @Test
    public void testPurchasesAreNotAvailable() {
        final TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        new BillingAvailabilitySingle(RuntimeEnvironment.application, PurchaseType.INAPP).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
        final BillingException exception = (BillingException) subscriber.getOnErrorEvents().get(0);
        assertEquals(ResponseCode.BILLING_UNAVAILABLE, exception.getCode());
    }

    @Test
    public void testOnObtainProductDetails() {
        final TestSubscriber<Inventory> subscriber = new TestSubscriber<>();
        new ProductDetailsSingle(RuntimeEnvironment.application, PurchaseType.INAPP, Collections.singletonList(ERROR_SKU))
                .subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

}