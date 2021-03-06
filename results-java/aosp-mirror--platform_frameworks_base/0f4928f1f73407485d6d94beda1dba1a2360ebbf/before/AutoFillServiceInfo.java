/*
 * Copyright (C) 2016 The Android Open Source Project
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
package android.service.autofill;

import android.Manifest;
import android.annotation.Nullable;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.RemoteException;
import android.util.AndroidException;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.android.internal.R;

import java.io.IOException;

// TODO(b/33197203 , b/33802548): add CTS tests
/**
 * {@link ServiceInfo} and meta-data about an {@link AutoFillService}.
 *
 * <p>Upon construction, if {@link #getParseError()} is {@code null}, then the service is configured
 * correctly. Otherwise, {@link #getParseError()} indicates the parsing error.
 *
 * @hide
 */
public final class AutoFillServiceInfo {
    static final String TAG = "AutoFillServiceInfo";

    private static ServiceInfo getServiceInfoOrThrow(ComponentName comp, int userHandle)
            throws PackageManager.NameNotFoundException {
        try {
            ServiceInfo si = AppGlobals.getPackageManager().getServiceInfo(
                    comp,
                    PackageManager.GET_META_DATA,
                    userHandle);
            if (si != null) {
                return si;
            }
        } catch (RemoteException e) {
        }
        throw new PackageManager.NameNotFoundException(comp.toString());
    }

    @Nullable
    private final String mParseError;

    private final ServiceInfo mServiceInfo;
    @Nullable
    private final String mSettingsActivity;

    public AutoFillServiceInfo(PackageManager pm, ComponentName comp, int userHandle)
            throws PackageManager.NameNotFoundException {
        this(pm, getServiceInfoOrThrow(comp, userHandle));
    }

    public AutoFillServiceInfo(PackageManager pm, ServiceInfo si) {
        mServiceInfo = si;
        TypedArray metaDataArray;
        try {
            metaDataArray = getMetaDataArray(pm, si);
        } catch (AndroidException e) {
            mParseError = e.getMessage();
            mSettingsActivity = null;
            Log.w(TAG, mParseError, e);
            return;
        }

        mParseError = null;
        if (metaDataArray != null) {
            mSettingsActivity =
                    metaDataArray.getString(R.styleable.AutoFillService_settingsActivity);
            metaDataArray.recycle();
        } else {
            mSettingsActivity = null;
        }
    }

    /**
     * Gets the meta-data as a TypedArray, or null if not provided, or throws if invalid.
     */
    @Nullable
    private static TypedArray getMetaDataArray(PackageManager pm, ServiceInfo si)
            throws AndroidException {
        // Check for permissions.
        if (!Manifest.permission.BIND_AUTO_FILL.equals(si.permission)) {
            throw new AndroidException(
                "Service does not require permission " + Manifest.permission.BIND_AUTO_FILL);
        }

        // Get the AutoFill metadata, if declared.
        XmlResourceParser parser = si.loadXmlMetaData(pm, AutoFillService.SERVICE_META_DATA);
        if (parser == null) {
            return null;
        }

        // Parse service info and get the <autofill-service> tag as an AttributeSet.
        AttributeSet attrs;
        try {
            // Move the XML parser to the first tag.
            try {
                int type;
                while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                        && type != XmlPullParser.START_TAG) {
                }
            } catch (XmlPullParserException | IOException e) {
                throw new AndroidException("Error parsing auto fill service meta-data: " + e, e);
            }

            if (!"autofill-service".equals(parser.getName())) {
                throw new AndroidException("Meta-data does not start with autofill-service tag");
            }
            attrs = Xml.asAttributeSet(parser);

            // Get resources required to read the AttributeSet.
            Resources res;
            try {
                res = pm.getResourcesForApplication(si.applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {
                throw new AndroidException("Error getting application resources: " + e, e);
            }

            return res.obtainAttributes(attrs, R.styleable.AutoFillService);
        } finally {
            parser.close();
        }
    }

    @Nullable
    public String getParseError() {
        return mParseError;
    }

    public ServiceInfo getServiceInfo() {
        return mServiceInfo;
    }

    @Nullable
    public String getSettingsActivity() {
        return mSettingsActivity;
    }
}