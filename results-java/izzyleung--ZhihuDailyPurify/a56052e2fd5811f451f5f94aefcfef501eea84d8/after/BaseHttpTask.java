package io.github.izzyleung.zhihudailypurify.task;

import android.text.Html;

import io.github.izzyleung.zhihudailypurify.support.lib.MyAsyncTask;

public abstract class BaseHttpTask<Params, Progress, Result> extends MyAsyncTask<Params, Progress, Result> {
    protected String decodeHtml(String in) {
        return Html.fromHtml(Html.fromHtml(in).toString()).toString();
    }
}