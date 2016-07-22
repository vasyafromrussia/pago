package io.octo.bear.pago;

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

    @SuppressWarnings("unused")
    @Implementation
    public static com.android.vending.billing.IInAppBillingService asInterface(android.os.IBinder obj) throws Exception {
        final IInAppBillingService service = Mockito.mock(IInAppBillingService.class);

        setupBillingSupportedResponse(service, PurchaseType.INAPP);
        setupBillingSupportedResponse(service, PurchaseType.SUBSCRIPTION);

        setupDetailsResponse(service, PurchaseType.INAPP);
        setupDetailsResponse(service, PurchaseType.SUBSCRIPTION);

        return service;
    }

    private static void setupBillingSupportedResponse(IInAppBillingService service, PurchaseType type) throws RemoteException {
        Mockito.doReturn(0)
                .when(service)
                .isBillingSupported(Pago.BILLING_API_VERSION, PagoTest.PACKAGE_NAME, type.value);
    }

    private static void setupDetailsResponse(IInAppBillingService service, PurchaseType type) throws RemoteException {
        Mockito.doReturn(getTestInventory(PurchaseType.SUBSCRIPTION))
                .when(service)
                .getSkuDetails(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(type.value),
                        argThat(new BundleMatcher(createSkusInfoRequestBundle())));
    }

    private static Bundle createSkusInfoRequestBundle() {
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList("ITEM_ID_LIST", new ArrayList<>(Collections.singletonList(TEST_SKU)));
        return bundle;
    }

    private static Bundle getTestInventory(final PurchaseType type) {
        final Bundle result = new Bundle();
        final String detailsJson = String.format(MockResponse.SKU_DETAILS_RESPONSE, TEST_SKU, type.value);
        result.putInt("RESPONSE_CODE", 0);
        result.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(detailsJson)));
        return result;
    }

}