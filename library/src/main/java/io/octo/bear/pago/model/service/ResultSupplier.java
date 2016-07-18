package io.octo.bear.pago.model.service;

import java.io.Serializable;

/**
 * Created by shc on 18.07.16.
 */
public interface ResultSupplier<T> extends Serializable {
    void onSuccess(T result);
}
