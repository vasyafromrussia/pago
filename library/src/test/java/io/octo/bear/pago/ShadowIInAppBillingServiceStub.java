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

import io.octo.bear.pago.model.entity.PurchaseType;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;

/**
 * Created by shc on 21.07.16.
 */
@Implements(IInAppBillingService.Stub.class)
public class ShadowIInAppBillingServiceStub {

    static final String TEST_SKU = "test.product.id";
    static final String TEST_DEVELOPER_PAYLOAD = "test developer payload";
    static final String TEST_PURCHASE_TOKEN = "someTestTokenObtainedAfterPurchase";

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

        return service;
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
    }

    private static Bundle createSkusInfoRequestBundle() {
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList("ITEM_ID_LIST", new ArrayList<>(Collections.singletonList(TEST_SKU)));
        return bundle;
    }

    private static Bundle createInventory(final PurchaseType type) {
        final Bundle result = new Bundle();
        final String detailsJson = String.format(MockResponse.SKU_DETAILS_RESPONSE, TEST_SKU, type.value);
        result.putInt("RESPONSE_CODE", 0);
        result.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(detailsJson)));
        return result;
    }

    private static Bundle createBuyIntentBundle() throws IntentSender.SendIntentException {
        final Bundle result = new Bundle();
        result.putInt("RESPONSE_CODE", 0);
        result.putParcelable(PerformPurchaseSingle.RESPONSE_BUY_INTENT, createResponseBuyIntent());
        return result;
    }

    private static PendingIntent createResponseBuyIntent() throws IntentSender.SendIntentException {
        PendingIntent intent = Mockito.mock(PendingIntent.class);
        Mockito.doReturn(Mockito.mock(IntentSender.class)).when(intent).getIntentSender();
        return intent;
    }

}