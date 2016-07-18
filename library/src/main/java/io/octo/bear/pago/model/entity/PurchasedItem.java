package io.octo.bear.pago.model.entity;

/**
 * Created by shc on 18.07.16.
 */

public class PurchasedItem {

    public final Purchase data;
    public final String signature;
    public final String sku;

    public PurchasedItem(Purchase data, String signature, String sku) {
        this.data = data;
        this.signature = signature;
        this.sku = sku;
    }

    @Override
    public String toString() {
        return "PurchasedItem{" +
                "data='" + data + '\'' +
                ", signature='" + signature + '\'' +
                ", sku='" + sku + '\'' +
                '}';
    }

}
