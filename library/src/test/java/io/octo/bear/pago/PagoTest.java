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
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.octo.bear.pago.model.entity.Inventory;
import io.octo.bear.pago.model.entity.Order;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.exception.BillingException;
import rx.observers.TestSubscriber;

import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.OWNED_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.OWNED_SKU;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.PURCHASED_ITEM_COUNT;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_PURCHASE_TOKEN;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_SKU;
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
                ShadowIInAppBillingServiceStub.class
        }
)
public class PagoTest {

    static final String PACKAGE_NAME = RuntimeEnvironment.application.getPackageName();

    @Test
    public void testPurchasesAvailabilitySingle() {
        testBillingAvailabilitySingle(PurchaseType.INAPP);
    }

    @Test
    public void testObtainProductDetailsSingle() {
        testObtainDetailsSingle(PurchaseType.INAPP);
    }

    @Test
    public void testPurchaseProductSingle() throws IntentSender.SendIntentException, InterruptedException {
        testPerformSuccessfulPurchase(PurchaseType.INAPP);
    }

    @Test
    public void testPurchaseOwnedProduct() throws InterruptedException, IntentSender.SendIntentException {
        testPerformOwnedPurchase(PurchaseType.INAPP);
    }

    @Test
    public void testConsumptionSingle() {
        final TestSubscriber<Void> subscriber = new TestSubscriber<>();
        new ConsumePurchaseCompletable(RuntimeEnvironment.application, TEST_PURCHASE_TOKEN).subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    @Test
    public void testErrorOnConsumption() {
        final TestSubscriber<Void> subscriber = new TestSubscriber<>();
        new ConsumePurchaseCompletable(RuntimeEnvironment.application, null).subscribe(subscriber);
        subscriber.assertError(BillingException.class);
    }

    @Test
    public void testPurchasedProductsListSingle() {
        testObtainPurchasedItemsSingle(PurchaseType.INAPP);
    }

    private void testObtainPurchasedItemsSingle(final PurchaseType type) {
        final TestSubscriber<List<Order>> subscriber = new TestSubscriber<>();
        new PurchasedItemsSingle(RuntimeEnvironment.application, type).subscribe(subscriber);
        subscriber.assertNoErrors();
        final List<Order> orders = subscriber.getOnNextEvents().get(0);
        assertNotNull(orders);
        assertEquals(PURCHASED_ITEM_COUNT, orders.size());
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

    private void testPerformSuccessfulPurchase(final PurchaseType type) throws IntentSender.SendIntentException, InterruptedException {
        //start purchase flow
        final TestSubscriber<Order> subscriber = new TestSubscriber<>();
        final PerformPurchaseSingle performPurchaseSingle = new PerformPurchaseSingle(
                RuntimeEnvironment.application,
                type,
                TEST_SKU,
                TEST_DEVELOPER_PAYLOAD
        );
        performPurchaseSingle.subscribe(subscriber);

        // check if BillingActivity was started within X seconds
        final ShadowActivity shadowActivity = new ShadowActivity();
        final Intent billingActivityIntent = getBillingActivityIntent(shadowActivity);
        assertNotNull(billingActivityIntent);
        assertNotNull(billingActivityIntent.getParcelableExtra(BillingActivity.EXTRA_BUY_INTENT));

        receiveResultInBillingActivity(billingActivityIntent, MockResponse.PURCHASE_RESULT);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final Order order = subscriber.getOnNextEvents().get(0);
        assertEquals(order.purchase.productId, TEST_SKU);
        assertEquals(order.purchase.developerPayload, TEST_DEVELOPER_PAYLOAD);
    }

    private void testPerformOwnedPurchase(final PurchaseType type) throws IntentSender.SendIntentException, InterruptedException {
        //start purchase flow
        final TestSubscriber<Order> subscriber = new TestSubscriber<>();
        final PerformPurchaseSingle performPurchaseSingle = new PerformPurchaseSingle(
                RuntimeEnvironment.application,
                type,
                OWNED_SKU,
                OWNED_DEVELOPER_PAYLOAD
        );
        performPurchaseSingle.subscribe(subscriber);

        subscriber.assertError(BillingException.class);
        final BillingException exception = (BillingException) subscriber.getOnErrorEvents().get(0);
        assertEquals(exception.getCode(), ResponseCode.ITEM_ALREADY_OWNED);
    }

    private void receiveResultInBillingActivity(Intent billingActivityIntent, Intent result) {
        final BillingActivity billingActivity = Robolectric
                .buildActivity(BillingActivity.class)
                .withIntent(billingActivityIntent)
                .setup()
                .get();

        final ShadowActivity shadowBillingActivity = Shadows.shadowOf(billingActivity);

        // TODO: 25.07.16 is there better way to check startIntentSenderForResult?
        shadowBillingActivity.startActivityForResult(new Intent(), BillingActivity.REQUEST_CODE);
        shadowBillingActivity.receiveResult(new Intent(), Activity.RESULT_OK, result);
    }

    private Intent getBillingActivityIntent(ShadowActivity shadowActivity) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
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