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
import rx.observers.TestSubscriber;

import static io.octo.bear.pago.MockUtils.PURCHASED_ITEM_COUNT;
import static io.octo.bear.pago.MockUtils.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.MockUtils.TEST_PURCHASE_TOKEN;
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
                ShadowIInAppBillingServiceStub.class
        }
)
public class PagoTest {

    static final String PACKAGE_NAME = RuntimeEnvironment.application.getPackageName();

    @Test
    public void testPurchasesAvailabilitySingle() {
        final TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        new BillingAvailabilitySingle(RuntimeEnvironment.application, PurchaseType.INAPP).subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertValue(true);
    }

    @Test
    public void testObtainProductDetailsSingle() {
        final TestSubscriber<Inventory> subscriber = new TestSubscriber<>();
        final String productId = TEST_SKU;
        new ProductDetailsSingle(RuntimeEnvironment.application, PurchaseType.INAPP, Collections.singletonList(productId))
                .subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final Inventory inventory = subscriber.getOnNextEvents().get(0);
        assertNotNull(inventory.getSku(productId));
    }

    @Test
    public void testPurchaseProductSingle() throws IntentSender.SendIntentException, InterruptedException {
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

        receiveResultInBillingActivity(billingActivityIntent, MockResponse.PURCHASE_RESULT);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        final Order order = subscriber.getOnNextEvents().get(0);
        assertEquals(order.purchase.productId, TEST_SKU);
        assertEquals(order.purchase.developerPayload, TEST_DEVELOPER_PAYLOAD);
    }

    @Test
    public void testConsumptionSingle() {
        final TestSubscriber<Void> subscriber = new TestSubscriber<>();
        new ConsumePurchaseCompletable(RuntimeEnvironment.application, TEST_PURCHASE_TOKEN).subscribe(subscriber);
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    @Test
    public void testPurchasedProductsListSingle() {
        final TestSubscriber<List<Order>> subscriber = new TestSubscriber<>();
        new PurchasedItemsSingle(RuntimeEnvironment.application, PurchaseType.INAPP).subscribe(subscriber);
        subscriber.assertNoErrors();
        final List<Order> orders = subscriber.getOnNextEvents().get(0);
        assertNotNull(orders);
        assertEquals(PURCHASED_ITEM_COUNT, orders.size());
    }

}