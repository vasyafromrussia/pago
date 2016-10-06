package io.octo.bear.pago;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import io.octo.bear.pago.model.entity.PurchaseType;
import rx.Scheduler;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;

/**
 * Created by defuera on 06/10/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP,
        shadows = {
                ShadowIInAppBillingServiceStub.class
        }
)
public class ThreadTest {

    private Thread testThread;

    @Test
    public void testScheduler() throws InterruptedException {

        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            testThread = new Thread(r, "thread11");
            return testThread;
        });

        Scheduler scheduler = Schedulers.from(executor);
        final Object yo = new Object();
        BillingAvailability
                .create(RuntimeEnvironment.application, PurchaseType.INAPP)
                .subscribeOn(scheduler)
                .subscribe(new SingleSubscriber<Boolean>() {
                    @Override
                    public void onSuccess(Boolean value) {
                        assertEquals(testThread, Thread.currentThread());
                        yo.notify();
                    }

                    @Override
                    public void onError(Throwable error) {
                        assertEquals(testThread, Thread.currentThread());
                        yo.notify();
                    }
                });

        synchronized (yo){
            yo.wait();
        }
    }
}
