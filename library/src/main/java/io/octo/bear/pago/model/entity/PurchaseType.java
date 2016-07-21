package io.octo.bear.pago.model.entity;

/**
 * Created by shc on 14.07.16.
 */

public enum PurchaseType {

    INAPP("inapp"),
    SUBSCRIPTION("subs");

    public String value;

    PurchaseType(String value) {
        this.value = value;
    }
}
