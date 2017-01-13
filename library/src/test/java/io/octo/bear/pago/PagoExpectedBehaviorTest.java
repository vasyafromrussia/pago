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

import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.octo.bear.pago.model.entity.Inventory;
import io.octo.bear.pago.model.entity.Order;
import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;
import rx.observers.TestSubscriber;

import static io.octo.bear.pago.BillingServiceUtils.RESPONSE_CODE;
import static io.octo.bear.pago.BillingServiceTestingUtils.PURCHASED_ITEM_COUNT;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_PURCHASE_TOKEN;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_SKU;
import static io.octo.bear.pago.PerformPurchaseSingle.RESPONSE_INAPP_DATA_SIGNATURE;
import static io.octo.bear.pago.PerformPurchaseSingle.RESPONSE_INAPP_PURCHASE_DATA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shc on 21.07.16.
 *
 * Test set for {@link com.android.vending.billing.IInAppBillingService} expected responses.
 */
@RunWith(RobolectricTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP,
        shadows = {
                ShadowIInAppBillingServiceStub.class
        }
)
public class PagoExpectedBehaviorTest extends BasePagoTest {

    static final String PACKAGE_NAME = RuntimeEnvironment.application.getPackageName();

    private static final String BUY_INTENT_RESPONSE = "{\"orderId\":\"12999763169054705758.1371079406387615\",\"packageName\":\"%s\",\"productId\":\"%s\",\"purchaseTime\":1345678900000,\"purchaseToken\":\"122333444455555\",\"developerPayload\":\"%s\"}";

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
        final Intent billingActivityIntent = getBillingActivityIntent(shadowActivity, 10);
        assertNotNull(billingActivityIntent);
        assertNotNull(billingActivityIntent.getParcelableExtra(BillingActivity.EXTRA_BUY_INTENT));

        receiveResultInBillingActivity(billingActivityIntent, createPurchaseResultBundle());

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
    public void testObtainPurchasedProductsListSingle() {
        final TestSubscriber<List<Order>> subscriber = new TestSubscriber<>();
        new PurchasedItemsSingle(RuntimeEnvironment.application, PurchaseType.INAPP).subscribe(subscriber);
        subscriber.assertNoErrors();
        final List<Order> orders = subscriber.getOnNextEvents().get(0);
        assertNotNull(orders);
        assertEquals(PURCHASED_ITEM_COUNT, orders.size());
    }

    private static Intent createPurchaseResultBundle() {
        return new Intent()
                .putExtra(RESPONSE_CODE, ResponseCode.OK.code)
                .putExtra(RESPONSE_INAPP_PURCHASE_DATA, String.format(BUY_INTENT_RESPONSE,
                        RuntimeEnvironment.application.getPackageName(),
                        TEST_SKU,
                        TEST_DEVELOPER_PAYLOAD))
                .putExtra(RESPONSE_INAPP_DATA_SIGNATURE, new Random().nextInt());
    }

}