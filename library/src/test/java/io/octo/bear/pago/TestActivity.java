package io.octo.bear.pago;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.robolectric.RuntimeEnvironment;

import io.octo.bear.pago.model.entity.Order;
import io.octo.bear.pago.model.entity.PurchaseType;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_DEVELOPER_PAYLOAD;
import static io.octo.bear.pago.ShadowIInAppBillingServiceStub.TEST_SKU;

/**
 * Created by shc on 22.07.16.
 */
public class TestActivity extends Activity implements View.OnClickListener, Action1<Order> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Button button = new Button(this);
        button.setId(R.id.test_button);
        setContentView(button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        new PerformPurchaseSingle(
                RuntimeEnvironment.application,
                PurchaseType.INAPP,
                TEST_SKU,
                TEST_DEVELOPER_PAYLOAD
        ).subscribeOn(Schedulers.immediate()).subscribe(this);
    }

    @Override
    public void call(Order order) {

    }

}
