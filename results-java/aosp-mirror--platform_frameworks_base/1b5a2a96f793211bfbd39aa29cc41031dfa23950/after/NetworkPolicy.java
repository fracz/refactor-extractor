/*
 * Copyright (C) 2011 The Android Open Source Project
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

package android.net;

import static com.android.internal.util.Preconditions.checkNotNull;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Policy for networks matching a {@link NetworkTemplate}, including usage cycle
 * and limits to be enforced.
 *
 * @hide
 */
public class NetworkPolicy implements Parcelable, Comparable<NetworkPolicy> {
    public static final long WARNING_DISABLED = -1;
    public static final long LIMIT_DISABLED = -1;

    public final NetworkTemplate template;
    public int cycleDay;
    public long warningBytes;
    public long limitBytes;

    // TODO: teach how to snooze limit for current cycle

    public NetworkPolicy(
            NetworkTemplate template, int cycleDay, long warningBytes, long limitBytes) {
        this.template = checkNotNull(template, "missing NetworkTemplate");
        this.cycleDay = cycleDay;
        this.warningBytes = warningBytes;
        this.limitBytes = limitBytes;
    }

    public NetworkPolicy(Parcel in) {
        template = in.readParcelable(null);
        cycleDay = in.readInt();
        warningBytes = in.readLong();
        limitBytes = in.readLong();
    }

    /** {@inheritDoc} */
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(template, flags);
        dest.writeInt(cycleDay);
        dest.writeLong(warningBytes);
        dest.writeLong(limitBytes);
    }

    /** {@inheritDoc} */
    public int describeContents() {
        return 0;
    }

    /** {@inheritDoc} */
    public int compareTo(NetworkPolicy another) {
        if (another == null || another.limitBytes == LIMIT_DISABLED) {
            // other value is missing or disabled; we win
            return -1;
        }
        if (limitBytes == LIMIT_DISABLED || another.limitBytes < limitBytes) {
            // we're disabled or other limit is smaller; they win
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "NetworkPolicy[" + template + "]: cycleDay=" + cycleDay + ", warningBytes="
                + warningBytes + ", limitBytes=" + limitBytes;
    }

    public static final Creator<NetworkPolicy> CREATOR = new Creator<NetworkPolicy>() {
        public NetworkPolicy createFromParcel(Parcel in) {
            return new NetworkPolicy(in);
        }

        public NetworkPolicy[] newArray(int size) {
            return new NetworkPolicy[size];
        }
    };
}