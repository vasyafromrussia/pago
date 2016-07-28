package io.octo.bear.pago;

import android.os.Bundle;

import com.google.gson.Gson;

import io.octo.bear.pago.model.entity.ResponseCode;
import io.octo.bear.pago.model.exception.BillingException;

/**
 * Created by shc on 15.07.16.
 */
final class BillingServiceUtils {

    static final String RESPONSE_CODE = "RESPONSE_CODE";

    static final Gson GSON = new Gson();

    static ResponseCode retrieveResponseCode(final Bundle result) {
        return ResponseCode.getByCode(result.getInt(RESPONSE_CODE));
    }

    static void checkResponseAndThrowIfError(ResponseCode code) throws BillingException {
        if (code != ResponseCode.OK) throw new BillingException(code);
    }

}
