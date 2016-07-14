package io.octo.bear.pago.model.entity;

/**
 * Created by shc on 14.07.16.
 */

public class Purchase {

    public final String orderId;
    public final String packageName;
    public final String productId;
    public final long purchaseTime;
    public final int purchaseState;
    public final String developerPayload;
    public final String purchaseToken;

    public Purchase(
            String orderId,
            String packageName,
            String productId,
            long purchaseTime,
            int purchaseState,
            String developerPayload,
            String purchaseToken) {
        this.orderId = orderId;
        this.packageName = packageName;
        this.productId = productId;
        this.purchaseTime = purchaseTime;
        this.purchaseState = purchaseState;
        this.developerPayload = developerPayload;
        this.purchaseToken = purchaseToken;
    }
}
