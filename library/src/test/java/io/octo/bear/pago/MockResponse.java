package io.octo.bear.pago;

/**
 * Created by shc on 22.07.16.
 */
interface MockResponse {
    String SKU_DETAILS_RESPONSE = "{\"productId\":\"%s\",\"type\":\"%s\",\"price\":\"$5.00\",\"title\":\"Example Title\",\"description\":\"This is an example description\"}";
    String BUY_INTENT_RESPONSE = "{\"orderId\":\"12999763169054705758.1371079406387615\",\"packageName\":\"%s\",\"productId\":\"%s\",\"purchaseTime\":1345678900000,\"purchaseToken\":\"122333444455555\",\"developerPayload\":\"%s\"}";
}