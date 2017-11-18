package io.github.izzyleung.zhihudailypurify.support.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class Http {
    public static final String CHARSET = "UTF-8";

    public static String get(String urlAddr) throws IOException {
        URL url = new URL(urlAddr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        try {
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();

                return response.toString();
            } else {
                throw new IOException("Network Error - response code: " + con.getResponseCode());
            }
        } finally {
            con.disconnect();
        }
    }

    public static String get(String baseUrl, String key, String value) throws IOException {
        return get(baseUrl + "?" + concatKeyValue(key, value));
    }

    public static String get(String baseUrl, int suffix) throws IOException {
        return get(baseUrl + suffix);
    }

    public static String get(String baseUrl, String suffix) throws IOException {
        return get(baseUrl + encodeString(suffix));
    }

    private static String concatKeyValue(String key, String value) {
        return encodeString(key) + "=" + encodeString(value).replace("+", "%20");
    }

    private static String encodeString(String str) {
        try {
            return URLEncoder.encode(str, CHARSET);
        } catch (UnsupportedEncodingException ignored) {
            return "";
        }
    }
}