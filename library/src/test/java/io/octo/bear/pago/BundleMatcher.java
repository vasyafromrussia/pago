package io.octo.bear.pago;

import android.os.Bundle;

import org.mockito.ArgumentMatcher;

import java.util.Set;

/**
 * Created by shc on 22.07.16.
 *
 * {@link Bundle} doesn't override {@link Object#equals(Object)},
 * while {@link org.mockito.Matchers#eq(Object)} requires it.
 * So this matcher is needed to use Bundles with Mockito mocks.
 */

class BundleMatcher extends ArgumentMatcher<Bundle> {

    private final Bundle value;

    BundleMatcher(Bundle value) {
        this.value = value;
    }

    @Override
    public boolean matches(Object argument) {
        return argument instanceof Bundle && equalBundles((Bundle) argument, value);
    }

    private static boolean equalBundles(Bundle one, Bundle two) {
        if (one.size() != two.size())
            return false;

        Set<String> setOne = one.keySet();
        Object valueOne;
        Object valueTwo;

        for (String key : setOne) {
            valueOne = one.get(key);
            valueTwo = two.get(key);
            if (valueOne instanceof Bundle && valueTwo instanceof Bundle &&
                    !equalBundles((Bundle) valueOne, (Bundle) valueTwo)) {
                return false;
            } else if (valueOne == null) {
                if (valueTwo != null || !two.containsKey(key))
                    return false;
            } else if (!valueOne.equals(valueTwo))
                return false;
        }

        return true;
    }

}
