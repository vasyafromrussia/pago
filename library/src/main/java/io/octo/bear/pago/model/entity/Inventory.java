package io.octo.bear.pago.model.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shc on 20.07.16.
 */

public class Inventory {

    private final Map<String, Sku> items;

    public Inventory() {
        items = new HashMap<>();
    }

    public void addItem(final Sku sku) {
        items.put(sku.productId, sku);
    }

    public Sku getSku(final String productId) {
        return items.get(productId);
    }

}
