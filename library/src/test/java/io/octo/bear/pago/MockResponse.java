package io.octo.bear.pago;

import android.content.Intent;

import org.robolectric.RuntimeEnvironment;

import java.util.Random;

import io.octo.bear.pago.model.entity.ResponseCode;

import static io.octo.bear.pago.BillingServiceUtils.RESPONSE_CODE;
import static io.octo.bear.pago.PerformPurchaseSingle.RESPONSE_INAPP_DATA_SIGNATURE;
import static io.octo.bear.pago.PerformPurchaseSingle.RESPONSE_INAPP_PURCHASE_DATA;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_SKU;

/**
 * Created by shc on 22.07.16.
 */
interface MockResponse {
    String SKU_DETAILS_RESPONSE = "{\"productId\":\"%s\",\"type\":\"%s\",\"price\":\"$5.00\",\"title\":\"Example Title\",\"description\":\"This is an example description\"}";
    String BUY_INTENT_RESPONSE = "{\"orderId\":\"12999763169054705758.1371079406387615\",\"packageName\":\"%s\",\"productId\":\"%s\",\"purchaseTime\":1345678900000,\"purchaseToken\":\"122333444455555\",\"developerPayload\":\"%s\"}";
    Intent PURCHASE_RESULT = new Intent()
            .putExtra(RESPONSE_CODE, 0)
            .putExtra(RESPONSE_INAPP_PURCHASE_DATA, String.format(BUY_INTENT_RESPONSE,
                    RuntimeEnvironment.application.getPackageName(),
                    TEST_SKU,
                    TEST_DEVELOPER_PAYLOAD))
            .putExtra(RESPONSE_INAPP_DATA_SIGNATURE, new Random().nextInt());
}