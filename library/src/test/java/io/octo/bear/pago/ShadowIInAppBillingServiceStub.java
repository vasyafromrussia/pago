package io.octo.bear.pago;

import android.app.PendingIntent;
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
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;

/**
 * Created by shc on 21.07.16.
 */
@Implements(IInAppBillingService.Stub.class)
public class ShadowIInAppBillingServiceStub {

    static final String TEST_SKU = "test.product.id";
    static final String OWNED_SKU = "owned.product.id";
    static final String TEST_DEVELOPER_PAYLOAD = "test developer payload";
    static final String OWNED_DEVELOPER_PAYLOAD = "owned developer payload";
    static final String TEST_PURCHASE_TOKEN = "someTestTokenObtainedAfterPurchase";
    static final int PURCHASED_ITEM_COUNT = 3;

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

    private static void setupPurchasedItemsResponse(IInAppBillingService service, PurchaseType inapp) throws RemoteException {
        Mockito.doReturn(createPurchasedListBundle(inapp))
                .when(service)
                .getPurchases(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(inapp.value),
                        eq(null));
    }

    private static void setupConsumptionResponse(IInAppBillingService service) throws RemoteException {
        Mockito.doReturn(0)
                .when(service)
                .consumePurchase(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(TEST_PURCHASE_TOKEN));

        Mockito.doReturn(1)
                .when(service)
                .consumePurchase(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(null));
    }

    private static void setupBillingSupportedResponse(IInAppBillingService service, PurchaseType type) throws RemoteException {
        Mockito.doReturn(0)
                .when(service)
                .isBillingSupported(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(type.value));
    }

    private static void setupDetailsResponse(IInAppBillingService service, PurchaseType type) throws RemoteException {
        Mockito.doReturn(createInventory(PurchaseType.SUBSCRIPTION))
                .when(service)
                .getSkuDetails(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(type.value),
                        argThat(new BundleMatcher(createSkusInfoRequestBundle())));
    }

    private static void setupBuyIntentResponse(IInAppBillingService service, PurchaseType type) throws RemoteException, IntentSender.SendIntentException {
        Mockito.doReturn(createBuyIntentBundle())
                .when(service)
                .getBuyIntent(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(TEST_SKU),
                        eq(type.value),
                        eq(TEST_DEVELOPER_PAYLOAD));

        Mockito.doReturn(createErrorBundle(ResponseCode.ITEM_ALREADY_OWNED))
                .when(service)
                .getBuyIntent(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(OWNED_SKU),
                        eq(type.value),
                        eq(OWNED_DEVELOPER_PAYLOAD));
    }

    private static Bundle createSkusInfoRequestBundle() {
        final Bundle bundle = new Bundle();
        bundle.putInt(RESPONSE_CODE, 0);
        bundle.putStringArrayList("ITEM_ID_LIST", new ArrayList<>(Collections.singletonList(TEST_SKU)));
        return bundle;
    }

    private static Bundle createInventory(final PurchaseType type) {
        final Bundle result = new Bundle();
        final String detailsJson = String.format(MockResponse.SKU_DETAILS_RESPONSE, TEST_SKU, type.value);
        result.putInt(RESPONSE_CODE, 0);
        result.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(detailsJson)));
        return result;
    }

    private static Bundle createBuyIntentBundle() throws IntentSender.SendIntentException {
        final Bundle result = new Bundle();
        result.putInt(RESPONSE_CODE, 0);
        result.putParcelable(PerformPurchaseSingle.RESPONSE_BUY_INTENT, createResponseBuyIntent());
        return result;
    }

    private static PendingIntent createResponseBuyIntent() throws IntentSender.SendIntentException {
        PendingIntent intent = Mockito.mock(PendingIntent.class);
        Mockito.doReturn(Mockito.mock(IntentSender.class)).when(intent).getIntentSender();
        return intent;
    }

    private static Bundle createPurchasedListBundle(final PurchaseType type) {
        final ArrayList<String> ids = new ArrayList<>();
        final ArrayList<String> details = new ArrayList<>();
        final ArrayList<String> signatures = new ArrayList<>();
        for (int i = 0; i < PURCHASED_ITEM_COUNT; i++) {
            final String sku = "test.sku." + i;
            ids.add(sku);
            details.add(String.format(MockResponse.SKU_DETAILS_RESPONSE, sku, type.value));
            signatures.add(String.valueOf(new Random(32).nextInt()));
        }

        final Bundle result = new Bundle();

        result.putInt(RESPONSE_CODE, 0);
        result.putStringArrayList(PurchasedItemsSingle.RESPONSE_INAPP_PURCHASE_ITEM_LIST, ids);
        result.putStringArrayList(PurchasedItemsSingle.RESPONSE_INAPP_PURCHASE_DATA_LIST, details);
        result.putStringArrayList(PurchasedItemsSingle.RESPONSE_INAPP_PURCHASE_SIGNATURE_LIST, signatures);

        return result;
    }

    private static Bundle createErrorBundle(final ResponseCode code) throws IntentSender.SendIntentException {
        final Bundle result = new Bundle();
        result.putInt(RESPONSE_CODE, code.code);
        return result;
    }

}