package io.octo.bear.pago;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;

import io.octo.bear.pago.model.entity.Inventory;
import io.octo.bear.pago.model.entity.PurchaseType;
import rx.observers.TestSubscriber;

import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_SKU;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shc on 21.07.16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP,
        shadows = ShadowIInAppBillingServiceStub.class
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

}