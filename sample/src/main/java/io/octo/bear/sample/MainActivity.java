package io.octo.bear.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.octo.bear.pago.Pago;
import io.octo.bear.pago.model.entity.PurchasedItem;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ITEM_PURCHASED = "android.test.purchased";
    private static final String ITEM_CANCELED = "android.test.canceled";
    private static final String ITEM_REFUNDED = "android.test.refunded";
    private static final String ITEM_UNAVAILABLE = "android.test.item_unavailable";

    @BindView(R.id.purchased)
    Button purchased;

    @BindView(R.id.canceled)
    Button canceled;

    @BindView(R.id.refunded)
    Button refunded;

    @BindView(R.id.unavailable)
    Button unavailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        final Pago pago = new Pago(getApplicationContext());

        purchased.setOnClickListener(view ->
                pago.purchaseProduct(ITEM_PURCHASED)
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                purchase -> Log.d(TAG, "purchase success: " + purchase),
                                throwable -> Log.e(TAG, "error: ", throwable)
                        ));

        canceled.setOnClickListener(view ->
                pago.consumeProduct("inapp:io.octo.bear.pago:android.test.purchased")
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                result -> Log.d(TAG, "purchase consumed"),
                                e -> Log.e(TAG, "error", e)
                        ));

    }

}
