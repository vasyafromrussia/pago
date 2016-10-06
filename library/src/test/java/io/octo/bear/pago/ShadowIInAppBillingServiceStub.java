package io.octo.bear.pago;

import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;

import static io.octo.bear.pago.BillingServiceUtils.RESPONSE_CODE;
import static io.octo.bear.pago.BillingServiceTestingUtils.PURCHASED_ITEM_COUNT;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_PURCHASE_TOKEN;
import static io.octo.bear.pago.BillingServiceTestingUtils.TEST_SKU;
import static io.octo.bear.pago.BillingServiceTestingUtils.createBuyIntentResponseBundle;
import static io.octo.bear.pago.BillingServiceTestingUtils.createProductDetailsRequestBundle;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;

/**
 * Created by shc on 21.07.16.
 *
 * Shadow {@link com.android.vending.billing.IInAppBillingService.Stub} returning
 * {@link IInAppBillingService}, that returns expected correct responses.
 */
@Implements(IInAppBillingService.Stub.class)
public class ShadowIInAppBillingServiceStub {

    private static final String SKU_DETAILS_RESPONSE = "{\"productId\":\"%s\",\"type\":\"%s\",\"price\":\"$5.00\",\"title\":\"Example Title\",\"description\":\"This is an example description\"}";

    @SuppressWarnings("unused")
    @Implementation
    public static com.android.vending.billing.IInAppBillingService asInterface(android.os.IBinder obj) throws Exception {
        final IInAppBillingService service = Mockito.mock(IInAppBillingService.class);

        setupBillingSupportedResponse(service, PurchaseType.INAPP);
        setupBillingSupportedResponse(service, PurchaseType.SUBSCRIPTION);

        setupDetailsResponse(service, PurchaseType.INAPP);
        setupDetailsResponse(service, PurchaseType.SUBSCRIPTION);

        setupBuyIntentResponse(service, PurchaseType.INAPP);
        setupBuyIntentResponse(service, PurchaseType.SUBSCRIPTION);

        setupConsumptionResponse(service);

        setupPurchasedItemsResponse(service, PurchaseType.INAPP);
        setupPurchasedItemsResponse(service, PurchaseType.SUBSCRIPTION);

        return service;
    }

    private static void setupPurchasedItemsResponse(IInAppBillingService service, PurchaseType type)
            throws RemoteException, IntentSender.SendIntentException {

        Mockito.doReturn(createPurchasedListBundle(type))
                .when(service)
                .getPurchases(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(type.value),
                        anyString());
    }

    private static void setupConsumptionResponse(IInAppBillingService service) throws RemoteException {
        Mockito.doReturn(0)
                .when(service)
                .consumePurchase(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(TEST_PURCHASE_TOKEN));

    }

    private static void setupBillingSupportedResponse(IInAppBillingService service, PurchaseType type) throws RemoteException {
        Mockito.doReturn(0)
                .when(service)
                .isBillingSupported(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(type.value));
    }

    private static void setupDetailsResponse(IInAppBillingService service, PurchaseType type)
            throws RemoteException, IntentSender.SendIntentException {

        Mockito.doReturn(createInventory(type))
                .when(service)
                .getSkuDetails(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(type.value),
                        argThat(new BundleMatcher(createProductDetailsRequestBundle(TEST_SKU))));

    }

    private static void setupBuyIntentResponse(IInAppBillingService service, PurchaseType type) throws RemoteException, IntentSender.SendIntentException {
        Mockito.doReturn(createBuyIntentResponseBundle())
                .when(service)
                .getBuyIntent(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoExpectedBehaviorTest.PACKAGE_NAME),
                        eq(TEST_SKU),
                        eq(type.value),
                        eq(TEST_DEVELOPER_PAYLOAD));

    }

    private static Bundle createPurchasedListBundle(final PurchaseType type) {
        final ArrayList<String> ids = new ArrayList<>();
        final ArrayList<String> details = new ArrayList<>();
        final ArrayList<String> signatures = new ArrayList<>();
        for (int i = 0; i < PURCHASED_ITEM_COUNT; i++) {
            final String sku = "test.sku." + i;
            ids.add(sku);
            details.add(String.format(SKU_DETAILS_RESPONSE, sku, type.value));
            signatures.add(String.valueOf(new Random(32).nextInt()));
        }

        final Bundle result = new Bundle();

        result.putInt(RESPONSE_CODE, 0);
        result.putStringArrayList(PurchasedItems.RESPONSE_INAPP_PURCHASE_ITEM_LIST, ids);
        result.putStringArrayList(PurchasedItems.RESPONSE_INAPP_PURCHASE_DATA_LIST, details);
        result.putStringArrayList(PurchasedItems.RESPONSE_INAPP_PURCHASE_SIGNATURE_LIST, signatures);

        return result;
    }

    private static Bundle createInventory(final PurchaseType type) {
        final Bundle result = new Bundle();
        final String detailsJson = String.format(SKU_DETAILS_RESPONSE, TEST_SKU, type.value);
        result.putInt(RESPONSE_CODE, ResponseCode.OK.code);
        result.putStringArrayList(ProductDetails.RESPONSE_DETAILS_LIST, new ArrayList<>(Collections.singletonList(detailsJson)));
        return result;
    }

}