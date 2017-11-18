/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.security;

import android.annotation.IntDef;
import android.security.keymaster.KeymasterDefs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Characteristics of {@code AndroidKeyStore} keys.
 *
 * @hide
 */
public abstract class KeyStoreKeyCharacteristics {
    private KeyStoreKeyCharacteristics() {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Origin.GENERATED, Origin.IMPORTED})
    public @interface OriginEnum {}

    /**
     * Origin of the key.
     */
    public static abstract class Origin {
        private Origin() {}

        /** Key was generated inside AndroidKeyStore. */
        public static final int GENERATED = 1 << 0;

        /** Key was imported into AndroidKeyStore. */
        public static final int IMPORTED = 1 << 1;

        /**
         * @hide
         */
        public static @OriginEnum int fromKeymaster(int origin) {
            switch (origin) {
                case KeymasterDefs.KM_ORIGIN_HARDWARE:
                    return GENERATED;
                case KeymasterDefs.KM_ORIGIN_IMPORTED:
                    return IMPORTED;
                default:
                    throw new IllegalArgumentException("Unknown origin: " + origin);
            }
        }
    }
}