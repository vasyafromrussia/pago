package io.octo.bear.sample;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.octo.bear.pago.Pago;
import rx.android.schedulers.AndroidSchedulers;
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

        final Pago pago = new Pago(this);

        purchased.setOnClickListener(view ->
                pago.purchaseProduct(ITEM_PURCHASED, "some_payload")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                order -> showSuccessDialog("Purchased successfully: " + order.purchase.productId),
                                this::showErrorDialog
                        ));

        canceled.setOnClickListener(view ->
                pago.consumeProduct("inapp:io.octo.bear.pago:android.test.purchased")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> showSuccessDialog("Purchase consumed successfully"),
                                this::showErrorDialog
                        ));

    }

    private void showErrorDialog(Throwable error) {
        new AlertDialog
                .Builder(this)
                .setTitle("Error")
                .setMessage(error.getMessage())
                .setNegativeButton("Okay :(", (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();
    }

    private void showSuccessDialog(String message) {
        new AlertDialog
                .Builder(this)
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();
    }

}
