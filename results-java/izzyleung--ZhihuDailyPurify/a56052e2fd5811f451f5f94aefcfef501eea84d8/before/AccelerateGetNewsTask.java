package io.github.izzyleung.zhihudailypurify.task;

import android.text.TextUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.support.Logger;

public class AccelerateGetNewsTask extends BaseGetNewsTask {
    private String serverCode;

    public AccelerateGetNewsTask(String serverCode, String date, UpdateUIListener callback) {
        super(date, callback);
        this.serverCode = serverCode;
    }

    @Override
    protected List<DailyNews> doInBackground(Void... params) {
        List<DailyNews> resultNewsList = new ArrayList<>();

        Type listType = new TypeToken<List<DailyNews>>() {

        }.getType();

        String baseUrl = serverCode.equals(Constants.ServerCode.SAE) ?
                Constants.Url.ZHIHU_DAILY_PURIFY_SAE_BEFORE :
                Constants.Url.ZHIHU_DAILY_PURIFY_HEROKU_BEFORE;

        String jsonFromWeb;
        try {
            jsonFromWeb = downloadStringFromUrl(baseUrl + date);
        } catch (IOException e) {
            isRefreshSuccess = false;
            Logger.e(e);
            return null;
        }

        String newsListJSON = decodeHtml(jsonFromWeb);

        if (!TextUtils.isEmpty(newsListJSON)) {
            try {
                resultNewsList = new GsonBuilder().create().fromJson(newsListJSON, listType);
            } catch (JsonSyntaxException e) {
                Logger.e(e);
            }
        } else {
            isRefreshSuccess = false;
        }

        isContentSame = checkIsContentSame(resultNewsList);
        return resultNewsList;
    }
}