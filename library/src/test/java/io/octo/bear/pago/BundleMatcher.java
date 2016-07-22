package io.octo.bear.pago;

import android.os.Bundle;

import org.mockito.ArgumentMatcher;

/**
 * Created by shc on 22.07.16.
 */

class BundleMatcher extends ArgumentMatcher<Bundle> {

    private final Bundle value;

    BundleMatcher(Bundle value) {
        this.value = value;
    }

    @Override
    public boolean matches(Object argument) {
        return argument instanceof Bundle && Utils.equalBundles((Bundle) argument, value);
    }

}
