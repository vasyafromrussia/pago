package io.octo.bear.pago.model.exception;

import io.octo.bear.pago.model.entity.ResponseCode;

/**
 * Created by shc on 14.07.16.
 */

public class BillingException extends RuntimeException {

    public BillingException(int code) {
        this(ResponseCode.getByCode(code));
    }

    public BillingException(ResponseCode code) {
        super(getErrorMessage(code));
    }

    private static String getErrorMessage(ResponseCode code) {
        switch (code) {
            case USER_CANCELED:
                return "User pressed back or canceled a dialog";
            case SERVICE_UNAVAILABLE:
                return "Network connection is down";
            case BILLING_UNAVAILABLE:
                return "Billing API version is not supported for the type requested";
            case ITEM_UNAVAILABLE:
                return "Requested product is not available for purchase";
            case DEVELOPER_ERROR:
                return "Invalid arguments provided to the API. This error can also indicate " +
                        "that the application was not correctly signed or properly set up for In-app Billing in Google Play, " +
                        "or does not have the necessary permissions in its manifest";
            case ERROR:
                return "Fatal error during the API action";
            case ITEM_ALREADY_OWNED:
                return "Failure to purchase since item is already owned";
            case ITEM_NOT_OWNED:
                return "Failure to consume since item is not owned";
            default:
                return "Unknown error";
        }
    }

}
