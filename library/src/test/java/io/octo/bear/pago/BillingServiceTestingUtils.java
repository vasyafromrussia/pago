/*
 * Copyright (C) 2017 Vasily Styagov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.octo.bear.pago;

import android.app.PendingIntent;
import android.content.IntentSender;
import android.os.Bundle;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;

import static io.octo.bear.pago.BillingServiceUtils.RESPONSE_CODE;

/**
 * Created by shc on 26.07.16.
 *
 * Some common methods and data for interaction with mocked IInAppBillingService
 */

class BillingServiceTestingUtils {

    /* package */ static final int PURCHASED_ITEM_COUNT = 3;

    /* package */ static final String TEST_SKU = "test.product.id";
    /* package */ static final String OWNED_SKU = "owned.product.id";

    /* package */ static final String TEST_DEVELOPER_PAYLOAD = "test developer payload";
    /* package */ static final String OWNED_DEVELOPER_PAYLOAD = "owned developer payload";

    /* package */ static final String TEST_PURCHASE_TOKEN = "someTestTokenObtainedAfterPurchase";


    /**
     * @param sku product id
     * @return bundle to send in product details request
     */
    static Bundle createProductDetailsRequestBundle(final String sku) {
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList(ProductDetailsSingle.EXTRA_ITEM_ID_LIST, new ArrayList<>(Collections.singletonList(sku)));
        return bundle;
    }

    /**
     * @return bundle that IInAppBillingService returns to receiving activity after successful purchase
     * @throws IntentSender.SendIntentException
     */
    static Bundle createBuyIntentResponseBundle() throws IntentSender.SendIntentException {
        final Bundle result = new Bundle();
        result.putInt(RESPONSE_CODE, 0);
        result.putParcelable(PerformPurchaseSingle.RESPONSE_BUY_INTENT, createResponseBuyIntent());
        return result;
    }

    private static PendingIntent createResponseBuyIntent() throws IntentSender.SendIntentException {
        PendingIntent intent = Mockito.mock(PendingIntent.class);
        Mockito.doReturn(Mockito.mock(IntentSender.class)).when(intent).getIntentSender();
        return intent;
    }

}
