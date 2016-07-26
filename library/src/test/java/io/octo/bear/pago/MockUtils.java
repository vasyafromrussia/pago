package io.octo.bear.pago;

import android.app.PendingIntent;
import android.content.IntentSender;
import android.os.Bundle;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import io.octo.bear.pago.model.entity.PurchaseType;
import io.octo.bear.pago.model.entity.ResponseCode;

import static io.octo.bear.pago.BillingServiceUtils.RESPONSE_CODE;

/**
 * Created by shc on 26.07.16.
 */

public class MockUtils {

    static final String OWNED_SKU = "owned.product.id";
    static final String ERROR_SKU = "error.product.id";
    static final String OWNED_DEVELOPER_PAYLOAD = "owned developer payload";
    static final String TEST_SKU = "test.product.id";
    static final String TEST_DEVELOPER_PAYLOAD = "test developer payload";
    static final String TEST_PURCHASE_TOKEN = "someTestTokenObtainedAfterPurchase";
    static final int PURCHASED_ITEM_COUNT = 3;

    static Bundle createSkusInfoRequestBundle(final String sku) {
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList(ProductDetailsSingle.EXTRA_ITEM_ID_LIST, new ArrayList<>(Collections.singletonList(sku)));
        return bundle;
    }

    static Bundle createErrorBundle(final ResponseCode code) throws IntentSender.SendIntentException {
        final Bundle result = new Bundle();
        result.putInt(RESPONSE_CODE, code.code);
        return result;
    }

     static Bundle createInventory(final PurchaseType type) {
        final Bundle result = new Bundle();
        final String detailsJson = String.format(MockResponse.SKU_DETAILS_RESPONSE, TEST_SKU, type.value);
        result.putInt(RESPONSE_CODE, ResponseCode.OK.code);
        result.putStringArrayList(ProductDetailsSingle.RESPONSE_DETAILS_LIST, new ArrayList<>(Collections.singletonList(detailsJson)));
        return result;
    }

     static Bundle createBuyIntentBundle() throws IntentSender.SendIntentException {
        final Bundle result = new Bundle();
        result.putInt(RESPONSE_CODE, 0);
        result.putParcelable(PerformPurchaseSingle.RESPONSE_BUY_INTENT, createResponseBuyIntent());
        return result;
    }

     static PendingIntent createResponseBuyIntent() throws IntentSender.SendIntentException {
        PendingIntent intent = Mockito.mock(PendingIntent.class);
        Mockito.doReturn(Mockito.mock(IntentSender.class)).when(intent).getIntentSender();
        return intent;
    }

     static Bundle createPurchasedListBundle(final PurchaseType type) {
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

}
