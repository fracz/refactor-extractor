package com.github.scribejava.core.model;

import com.ning.http.client.AsyncHttpClientConfig;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Parameter object that groups OAuth config values
 */
public class OAuthConfig {

    private final String apiKey;
    private final String apiSecret;
    private final String callback;
    private final SignatureType signatureType;
    private final String scope;
    private final OutputStream debugStream;
    private final String state;
    private final String responseType;
    private final String userAgent;

    //sync only version
    private final Integer connectTimeout;
    private final Integer readTimeout;

    //async only version
    private final AsyncHttpClientConfig asyncHttpClientConfig;
    private final String asyncHttpProviderClassName;

    public OAuthConfig(String key, String secret) {
        this(key, secret, null, null, null, null, null, null, null, null, null, null, null);
    }

    public OAuthConfig(String apiKey, String apiSecret, String callback, SignatureType signatureType, String scope,
            OutputStream debugStream, String state, String responseType, String userAgent, Integer connectTimeout,
            Integer readTimeout, AsyncHttpClientConfig asyncHttpClientConfig, String asyncHttpProviderClassName) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.callback = callback;
        this.signatureType = signatureType;
        this.scope = scope;
        this.debugStream = debugStream;
        this.state = state;
        this.responseType = responseType;
        this.userAgent = userAgent;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.asyncHttpClientConfig = asyncHttpClientConfig;
        this.asyncHttpProviderClassName = asyncHttpProviderClassName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getCallback() {
        return callback;
    }

    public SignatureType getSignatureType() {
        return signatureType;
    }

    public String getScope() {
        return scope;
    }

    public boolean hasScope() {
        return scope != null;
    }

    public String getState() {
        return state;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void log(String message) {
        if (debugStream != null) {
            message += '\n';
            try {
                debugStream.write(message.getBytes("UTF8"));
            } catch (IOException | RuntimeException e) {
                throw new RuntimeException("there were problems while writting to the debug stream", e);
            }
        }
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public AsyncHttpClientConfig getAsyncHttpClientConfig() {
        return asyncHttpClientConfig;
    }

    public String getAsyncHttpProviderClassName() {
        return asyncHttpProviderClassName;
    }
}