/*
 * Copyright (C) 2008 Google Inc.
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

package com.android.barcodes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.net.URI;

public class QRCodeEncoder {

    private static final String TAG = "QRCodeEncoder";

    private Activity mActivity;
    private String mContents;
    private String mDisplayContents;
    private String mTitle;
    private NetworkThread mNetworkThread;

    public QRCodeEncoder(Activity activity, Intent intent) {
        mActivity = activity;
        if (!encodeContents(intent)) {
            throw new IllegalArgumentException("No valid data to encode.");
        }
    }

    // Once the core ZXing library supports encoding, we'll be able to generate the bitmap
    // synchronously. For now, it's a network request, so it's handled on a thread.
    public void requestBarcode(Handler handler, int pixelResolution) {
        mNetworkThread = new NetworkThread(mContents, handler, pixelResolution);
        mNetworkThread.start();
    }

    public String getContents() {
        return mContents;
    }

    public String getDisplayContents() {
        return mDisplayContents;
    }

    public String getTitle() {
        return mTitle;
    }

    // Perhaps the string encoding should live in the core ZXing library too.
    private boolean encodeContents(Intent intent) {
        if (intent == null) return false;
        String type = intent.getStringExtra(Intents.Encode.TYPE);
        if (type == null || type.length() == 0) return false;

        if (type.equals(Contents.Type.TEXT)) {
            String string = intent.getStringExtra(Intents.Encode.DATA);
            if (string != null && string.length() > 0) {
                mContents = string;
                mDisplayContents = string;
                mTitle = mActivity.getString(R.string.contents_text);
            }
        } else if (type.equals(Contents.Type.EMAIL)) {
            String string = intent.getStringExtra(Intents.Encode.DATA);
            if (string != null && string.length() > 0) {
                mContents = "mailto:" + string;
                mDisplayContents = string;
                mTitle = mActivity.getString(R.string.contents_email);
            }
        } else if (type.equals(Contents.Type.PHONE)) {
            String string = intent.getStringExtra(Intents.Encode.DATA);
            if (string != null && string.length() > 0) {
                mContents = "tel:" + string;
                mDisplayContents = string;
                mTitle = mActivity.getString(R.string.contents_phone);
            }
        } else if (type.equals(Contents.Type.SMS)) {
            String string = intent.getStringExtra(Intents.Encode.DATA);
            if (string != null && string.length() > 0) {
                mContents = "sms:" + string;
                mDisplayContents = string;
                mTitle = mActivity.getString(R.string.contents_sms);
            }
        } else if (type.equals(Contents.Type.CONTACT)) {
            Bundle bundle = intent.getBundleExtra(Intents.Encode.DATA);
            if (bundle != null) {
                String name = bundle.getString(Contacts.Intents.Insert.NAME);
                if (name != null && !name.equals("")) {
                    mContents = "MECARD:N:" + name + ";";
                    mDisplayContents = name;
                    String phone = bundle.getString(Contacts.Intents.Insert.PHONE);
                    if (phone != null && !phone.equals("")) {
                        mContents += "TEL:" + phone + ";";
                        mDisplayContents += "\n" + phone;
                    }
                    String email = bundle.getString(Contacts.Intents.Insert.EMAIL);
                    if (email != null && !email.equals("")) {
                        mContents += "EMAIL:" + email + ";";
                        mDisplayContents += "\n" + email;
                    }
                    mContents += ";";
                    mTitle = mActivity.getString(R.string.contents_contact);
                }
            }
        } else if (type.equals(Contents.Type.LOCATION)) {
            Bundle bundle = intent.getBundleExtra(Intents.Encode.DATA);
            if (bundle != null) {
                float latitude = bundle.getFloat("LAT", Float.MAX_VALUE);
                float longitude = bundle.getFloat("LONG", Float.MAX_VALUE);
                if (latitude != Float.MAX_VALUE && longitude != Float.MAX_VALUE) {
                    mContents = "geo:" + latitude + "," + longitude;
                    mDisplayContents = latitude + "," + longitude;
                    mTitle = mActivity.getString(R.string.contents_location);
                }
            }
        }
        return mContents != null && mContents.length() > 0;
    }

    private class NetworkThread extends Thread {

        private String mContents;
        private Handler mHandler;
        private int mPixelResolution;

        public NetworkThread(String contents, Handler handler, int pixelResolution) {
            mContents = contents;
            mHandler = handler;
            mPixelResolution = pixelResolution;
        }

        public void run() {
            String url = "//chartserver.apis.google.com/chart?cht=qr&chs=";
            url += mPixelResolution + "x" + mPixelResolution + "&chl=" + mContents;
            try {
                URI uri = new URI("http", url, null);
                HttpGet get = new HttpGet(uri);
                AndroidHttpClient client = AndroidHttpClient.newInstance("Android-BarcodeScanner/0.1");
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();
                Bitmap image = BitmapFactory.decodeStream(entity.getContent());
                if (image != null) {
                    Message message = Message.obtain(mHandler, R.id.encode_succeeded);
                    message.obj = image;
                    message.sendToTarget();
                } else {
                    Log.e(TAG, "Could not decode png from the network");
                    Message message = Message.obtain(mHandler, R.id.encode_failed);
                    message.sendToTarget();
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Message message = Message.obtain(mHandler, R.id.encode_failed);
                message.sendToTarget();
            }
        }
    }

}