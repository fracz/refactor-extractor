package io.github.izzyleung.zhihudailypurify.task;

import android.text.Html;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;

public abstract class BaseDownloadTask<Params, Progress, Result> extends MyAsyncTask<Params, Progress, Result> {
    protected String downloadStringFromUrl(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();

        try {
            HttpResponse httpResponse = client.execute(new HttpGet(url));
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }
        } finally {
            client.getConnectionManager().shutdown();
        }

        return "";
    }

    protected String decodeHtml(String in) {
        return Html.fromHtml(Html.fromHtml(in).toString()).toString();
    }
}