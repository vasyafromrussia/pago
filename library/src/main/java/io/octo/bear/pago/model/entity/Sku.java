package io.octo.bear.pago.model.entity;

/**
 * Created by shc on 14.07.16.
 */

public class Sku {

    public final String productId;
    public final String price;

    public Sku(String productId, String price) {
        this.productId = productId;
        this.price = price;
    }
}
