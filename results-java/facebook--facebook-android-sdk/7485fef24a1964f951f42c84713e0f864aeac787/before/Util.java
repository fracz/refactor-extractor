/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import android.os.Bundle;
import android.util.Log;


/**
 * @author ssoneff@facebook.com
 *
 */
public final class Util {

    public static interface Callback {
        public void call(String result);
    }

    public static String encodeUrl(Bundle parameters) {
        if (parameters == null) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : parameters.keySet()) {
            if (first) first = false; else sb.append("&");
            sb.append(key + "=" + parameters.getString(key));
        }
        return sb.toString();
    }

    public static Bundle decodeUrl(String s) {
        Bundle params = new Bundle();
        if (s != null) {
            String array[] = s.split("&");
            for (String parameter : array) {
                String v[] = parameter.split("=");
                params.putString(v[0], v[1]);
            }
        }
        return params;
    }

    public static Bundle parseUrl(String url) {
        // hack to prevent MalformedURLException
        url = url.replace("fbconnect", "http");
        try {
            URL u = new URL(url);
            Bundle b = decodeUrl(u.getQuery());
            b.putAll(decodeUrl(u.getRef()));
            return b;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }

    public static String openUrl(String url, String method, Bundle params) {
        try {
            url = (method.equals("GET") || method.equals("DELETE")) ?
            		(url + "?" + encodeUrl(params)) : url;
            Log.d("Facebook-Util", method + " URL: " + url);
            HttpURLConnection conn =
                (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            if (method.equals("POST") || method.equals("PUT")) {
                conn.setDoOutput(true);
                conn.getOutputStream().write(
                        encodeUrl(params).getBytes("UTF-8"));
            }
            return read(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }

    public static String join(String[] strings, String delimiter) {
        return join(Arrays.asList(strings), delimiter);
    }

    public static String join(Iterable<String> strings, String delimiter) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : strings) {
            if (first) first = false; else sb.append(delimiter);
            sb.append(s);
        }
        return sb.toString();
    }

    public static void asyncOpenUrl(final String url,
                                    final String httpMethod,
                                    final Bundle parameters,
                                    final Callback callback) {
        new Thread() {
            @Override public void run() {
                callback.call(openUrl(url, httpMethod, parameters));
            }
        }.run();
    }

}