package io.octo.bear.pago.model.entity;

/**
 * Created by shc on 19.07.16.
 */
public class Order {

    public final Purchase purchase;
    public final String signature;

    public Order(Purchase purchase, String signature) {
        this.purchase = purchase;
        this.signature = signature;
    }

}
