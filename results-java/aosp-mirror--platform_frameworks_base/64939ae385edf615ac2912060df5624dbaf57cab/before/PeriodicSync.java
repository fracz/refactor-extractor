/*
 * Copyright (C) 2010 The Android Open Source Project
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

package android.content;

import android.os.Parcelable;
import android.os.Bundle;
import android.os.Parcel;
import android.accounts.Account;

/**
 * Value type that contains information about a periodic sync.
 */
public class PeriodicSync implements Parcelable {
    /** The account to be synced. Can be null. */
    public final Account account;
    /** The authority of the sync. Can be null. */
    public final String authority;
    /** The service for syncing, if this is an anonymous sync. Can be null.*/
    public final ComponentName service;
    /** Any extras that parameters that are to be passed to the sync adapter. */
    public final Bundle extras;
    /** How frequently the sync should be scheduled, in seconds. Kept around for API purposes. */
    public final long period;
    /** Whether this periodic sync runs on a {@link SyncService}. */
    public final boolean isService;
    /**
     * How much flexibility can be taken in scheduling the sync, in seconds.
     * {@hide}
     */
    public final long flexTime;

      /**
       * Creates a new PeriodicSync, copying the Bundle. SM no longer uses this ctor - kept around
       * becuse it is part of the API.
       * Note - even calls to the old API will not use this ctor, as
       * they are given a default flex time.
       */
    public PeriodicSync(Account account, String authority, Bundle extras, long periodInSeconds) {
        this.account = account;
        this.authority = authority;
        this.service = null;
        this.isService = false;
        if (extras == null) {
            this.extras = new Bundle();
        } else {
            this.extras = new Bundle(extras);
        }
        this.period = periodInSeconds;
        // Old API uses default flex time. No-one should be using this ctor anyway.
        this.flexTime = 0L;
    }

    /**
     * Create a copy of a periodic sync.
     * {@hide}
     */
    public PeriodicSync(PeriodicSync other) {
        this.account = other.account;
        this.authority = other.authority;
        this.service = other.service;
        this.isService = other.isService;
        this.extras = new Bundle(other.extras);
        this.period = other.period;
        this.flexTime = other.flexTime;
    }

    /**
     * A PeriodicSync for a sync with a specified provider.
     * {@hide}
     */
    public PeriodicSync(Account account, String authority, Bundle extras,
            long period, long flexTime) {
        this.account = account;
        this.authority = authority;
        this.service = null;
        this.isService = false;
        this.extras = new Bundle(extras);
        this.period = period;
        this.flexTime = flexTime;
    }

    /**
     * A PeriodicSync for a sync with a specified SyncService.
     * {@hide}
     */
    public PeriodicSync(ComponentName service, Bundle extras,
            long period,
            long flexTime) {
        this.account = null;
        this.authority = null;
        this.service = service;
        this.isService = true;
        this.extras = new Bundle(extras);
        this.period = period;
        this.flexTime = flexTime;
    }

    private PeriodicSync(Parcel in) {
        this.isService = (in.readInt() != 0);
        if (this.isService) {
            this.service = in.readParcelable(null);
            this.account = null;
            this.authority = null;
        } else {
            this.account = in.readParcelable(null);
            this.authority = in.readString();
            this.service = null;
        }
        this.extras = in.readBundle();
        this.period = in.readLong();
        this.flexTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(isService ? 1 : 0);
        if (account == null && authority == null) {
            dest.writeParcelable(service, flags);
        } else {
            dest.writeParcelable(account, flags);
            dest.writeString(authority);
        }
        dest.writeBundle(extras);
        dest.writeLong(period);
        dest.writeLong(flexTime);
    }

    public static final Creator<PeriodicSync> CREATOR = new Creator<PeriodicSync>() {
        @Override
        public PeriodicSync createFromParcel(Parcel source) {
            return new PeriodicSync(source);
        }

        @Override
        public PeriodicSync[] newArray(int size) {
            return new PeriodicSync[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PeriodicSync)) {
            return false;
        }
        final PeriodicSync other = (PeriodicSync) o;
        if (this.isService != other.isService) {
            return false;
        }
        boolean equal = false;
        if (this.isService) {
            equal = service.equals(other.service);
        } else {
            equal = account.equals(other.account)
                    && authority.equals(other.authority);
        }
        return equal
            && period == other.period
            && syncExtrasEquals(extras, other.extras);
    }

    /**
     * Periodic sync extra comparison function. Duplicated from
     * {@link com.android.server.content.SyncManager#syncExtrasEquals(Bundle b1, Bundle b2)}
     * {@hide}
     */
    public static boolean syncExtrasEquals(Bundle b1, Bundle b2) {
        if (b1.size() != b2.size()) {
            return false;
        }
        if (b1.isEmpty()) {
            return true;
        }
        for (String key : b1.keySet()) {
            if (!b2.containsKey(key)) {
                return false;
            }
            if (!b1.get(key).equals(b2.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "account: " + account +
               ", authority: " + authority +
               ", service: " + service +
               ". period: " + period + "s " +
               ", flex: " + flexTime;
    }
}