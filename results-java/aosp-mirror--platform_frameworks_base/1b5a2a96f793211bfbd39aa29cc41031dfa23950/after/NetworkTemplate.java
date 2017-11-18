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

import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_WIMAX;
import static android.net.ConnectivityManager.isNetworkTypeMobile;
import static android.telephony.TelephonyManager.NETWORK_CLASS_2_G;
import static android.telephony.TelephonyManager.NETWORK_CLASS_3_G;
import static android.telephony.TelephonyManager.NETWORK_CLASS_4_G;
import static android.telephony.TelephonyManager.NETWORK_CLASS_UNKNOWN;
import static android.telephony.TelephonyManager.getNetworkClass;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.internal.util.Objects;

/**
 * Template definition used to generically match {@link NetworkIdentity},
 * usually when collecting statistics.
 *
 * @hide
 */
public class NetworkTemplate implements Parcelable {

    /**
     * Template to combine all {@link ConnectivityManager#TYPE_MOBILE} style
     * networks together. Only uses statistics for requested IMSI.
     */
    public static final int MATCH_MOBILE_ALL = 1;

    /**
     * Template to combine all {@link ConnectivityManager#TYPE_MOBILE} style
     * networks together that roughly meet a "3G" definition, or lower. Only
     * uses statistics for requested IMSI.
     */
    public static final int MATCH_MOBILE_3G_LOWER = 2;

    /**
     * Template to combine all {@link ConnectivityManager#TYPE_MOBILE} style
     * networks together that meet a "4G" definition. Only uses statistics for
     * requested IMSI.
     */
    public static final int MATCH_MOBILE_4G = 3;

    /**
     * Template to combine all {@link ConnectivityManager#TYPE_WIFI} style
     * networks together.
     */
    public static final int MATCH_WIFI = 4;

    final int mMatchRule;
    final String mSubscriberId;

    public NetworkTemplate(int matchRule, String subscriberId) {
        this.mMatchRule = matchRule;
        this.mSubscriberId = subscriberId;
    }

    public NetworkTemplate(Parcel in) {
        mMatchRule = in.readInt();
        mSubscriberId = in.readString();
    }

    /** {@inheritDoc} */
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMatchRule);
        dest.writeString(mSubscriberId);
    }

    /** {@inheritDoc} */
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        final String scrubSubscriberId = mSubscriberId != null ? "valid" : "null";
        return "NetworkTemplate: matchRule=" + getMatchRuleName(mMatchRule) + ", subscriberId="
                + scrubSubscriberId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mMatchRule, mSubscriberId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NetworkTemplate) {
            final NetworkTemplate other = (NetworkTemplate) obj;
            return mMatchRule == other.mMatchRule
                    && Objects.equal(mSubscriberId, other.mSubscriberId);
        }
        return false;
    }

    public int getMatchRule() {
        return mMatchRule;
    }

    public String getSubscriberId() {
        return mSubscriberId;
    }

    /**
     * Test if this network matches the given template and IMEI.
     */
    public boolean matches(NetworkIdentity ident) {
        switch (mMatchRule) {
            case MATCH_MOBILE_ALL:
                return matchesMobile(ident);
            case MATCH_MOBILE_3G_LOWER:
                return matchesMobile3gLower(ident);
            case MATCH_MOBILE_4G:
                return matchesMobile4g(ident);
            case MATCH_WIFI:
                return matchesWifi(ident);
            default:
                throw new IllegalArgumentException("unknown network template");
        }
    }

    /**
     * Check if mobile network with matching IMEI. Also matches
     * {@link #TYPE_WIMAX}.
     */
    private boolean matchesMobile(NetworkIdentity ident) {
        if (isNetworkTypeMobile(ident.mType) && Objects.equal(mSubscriberId, ident.mSubscriberId)) {
            return true;
        } else if (ident.mType == TYPE_WIMAX) {
            return true;
        }
        return false;
    }

    /**
     * Check if mobile network classified 3G or lower with matching IMEI.
     */
    private boolean matchesMobile3gLower(NetworkIdentity ident) {
        if (isNetworkTypeMobile(ident.mType) && Objects.equal(mSubscriberId, ident.mSubscriberId)) {
            switch (getNetworkClass(ident.mSubType)) {
                case NETWORK_CLASS_UNKNOWN:
                case NETWORK_CLASS_2_G:
                case NETWORK_CLASS_3_G:
                    return true;
            }
        }
        return false;
    }

    /**
     * Check if mobile network classified 4G with matching IMEI. Also matches
     * {@link #TYPE_WIMAX}.
     */
    private boolean matchesMobile4g(NetworkIdentity ident) {
        if (isNetworkTypeMobile(ident.mType) && Objects.equal(mSubscriberId, ident.mSubscriberId)) {
            switch (getNetworkClass(ident.mSubType)) {
                case NETWORK_CLASS_4_G:
                    return true;
            }
        } else if (ident.mType == TYPE_WIMAX) {
            return true;
        }
        return false;
    }

    /**
     * Check if matches Wi-Fi network template.
     */
    private boolean matchesWifi(NetworkIdentity ident) {
        if (ident.mType == TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public static String getMatchRuleName(int matchRule) {
        switch (matchRule) {
            case MATCH_MOBILE_3G_LOWER:
                return "MOBILE_3G_LOWER";
            case MATCH_MOBILE_4G:
                return "MOBILE_4G";
            case MATCH_MOBILE_ALL:
                return "MOBILE_ALL";
            case MATCH_WIFI:
                return "WIFI";
            default:
                return "UNKNOWN";
        }
    }

    public static final Creator<NetworkTemplate> CREATOR = new Creator<NetworkTemplate>() {
        public NetworkTemplate createFromParcel(Parcel in) {
            return new NetworkTemplate(in);
        }

        public NetworkTemplate[] newArray(int size) {
            return new NetworkTemplate[size];
        }
    };
}