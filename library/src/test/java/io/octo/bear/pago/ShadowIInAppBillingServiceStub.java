package io.octo.bear.pago;

import android.os.Bundle;

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

    private static final String TEST_SKU_DETAILS_RESPONSE = "{\"productId\":\"%s\",\"type\":\"%s\",\"price\":\"$5.00\",\"title\":\"Example Title\",\"description\":\"This is an example description\"}";

    @SuppressWarnings("unused")
    @Implementation
    public static com.android.vending.billing.IInAppBillingService asInterface(android.os.IBinder obj) throws Exception {
        final IInAppBillingService service = Mockito.mock(IInAppBillingService.class);

        Mockito.doReturn(0)
                .when(service)
                .isBillingSupported(Pago.BILLING_API_VERSION, PagoTest.PACKAGE_NAME, PurchaseType.INAPP.value);

        Mockito.doReturn(0)
                .when(service)
                .isBillingSupported(Pago.BILLING_API_VERSION, PagoTest.PACKAGE_NAME, PurchaseType.SUBSCRIPTION.value);

        Mockito.doReturn(getTestInventory(PurchaseType.INAPP))
                .when(service)
                .getSkuDetails(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(PurchaseType.INAPP.value),
                        argThat(new BundleMatcher(createSkusInfoRequestBundle())));

        Mockito.doReturn(getTestInventory(PurchaseType.SUBSCRIPTION))
                .when(service)
                .getSkuDetails(
                        eq(Pago.BILLING_API_VERSION),
                        eq(PagoTest.PACKAGE_NAME),
                        eq(PurchaseType.SUBSCRIPTION.value),
                        argThat(new BundleMatcher(createSkusInfoRequestBundle())));

        return service;
    }

    private static Bundle createSkusInfoRequestBundle() {
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList("ITEM_ID_LIST", new ArrayList<>(Collections.singletonList(TEST_SKU)));
        return bundle;
    }

    private static Bundle getTestInventory(final PurchaseType type) {
        final Bundle result = new Bundle();
        final String detailsJson = String.format(TEST_SKU_DETAILS_RESPONSE, TEST_SKU, type.value);
        result.putInt("RESPONSE_CODE", 0);
        result.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(detailsJson)));
        return result;
    }

}