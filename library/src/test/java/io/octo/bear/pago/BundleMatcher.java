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

class BundleMatcher implements ArgumentMatcher<Bundle> {

    private final Bundle value;

    BundleMatcher(Bundle value) {
        this.value = value;
    }

    @Override
    public boolean matches(Bundle argument) {
        return argument != null && equalBundles(argument, value);
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
