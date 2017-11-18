package io.github.izzyleung.zhihudailypurify.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.izzyleung.zhihudailypurify.bean.DailyNews;
import io.github.izzyleung.zhihudailypurify.support.Constants;
import io.github.izzyleung.zhihudailypurify.support.Logger;
import io.github.izzyleung.zhihudailypurify.support.lib.Http;

public class OriginalGetNewsTask extends BaseGetNewsTask {

    public OriginalGetNewsTask(String date, UpdateUIListener callback) {
        super(date, callback);
    }

    @Override
    protected List<DailyNews> doInBackground(Void... params) {
        List<DailyNews> resultNewsList = new ArrayList<>();

        try {
            JSONObject contents = new JSONObject(Http.get(Constants.Url.ZHIHU_DAILY_BEFORE, date));

            JSONArray newsArray = contents.getJSONArray("stories");
            for (int i = 0; i < newsArray.length(); i++) {
                JSONObject singleNews = newsArray.getJSONObject(i);

                DailyNews dailyNews = new DailyNews();
                dailyNews.setThumbnailUrl(singleNews.has("images")
                        ? (String) singleNews.getJSONArray("images").get(0)
                        : null);
                dailyNews.setDailyTitle(singleNews.getString("title"));
                String newsInfoJson = Http.get(Constants.Url.ZHIHU_DAILY_OFFLINE_NEWS,
                        String.valueOf(singleNews.getInt("id")));
                JSONObject newsDetail = new JSONObject(newsInfoJson);
                if (newsDetail.has("body")) {
                    if (updateDailyNews(Jsoup.parse(newsDetail.getString("body")), dailyNews)) {
                        resultNewsList.add(dailyNews);
                    }
                }
            }
        } catch (JSONException | IOException e) {
            isRefreshSuccess = false;
            Logger.e(e);
        }

        isContentSame = checkIsContentSame(resultNewsList);
        return resultNewsList;
    }

    private boolean updateDailyNews(Document doc, DailyNews dailyNews)
            throws JSONException {
        Elements viewMoreElements = doc.getElementsByClass("view-more");

        if (viewMoreElements.size() > 1) {
            return processMulti(doc, viewMoreElements, dailyNews);
        } else {
            return viewMoreElements.size() == 1 && processSingle(doc, viewMoreElements, dailyNews);
        }
    }

    private boolean processMulti(Document doc, Elements viewMoreElements, DailyNews dailyNews) {
        dailyNews.setMulti(true);
        Elements questionTitleElements = doc.getElementsByClass("question-title");

        for (int j = 0; j < questionTitleElements.size(); j++) {
            if (j == 0 && questionTitleElements.get(j).text().length() == 0) {
                dailyNews.addQuestionTitle(dailyNews.getDailyTitle());
            }

            if (questionTitleElements.get(j).text().equals("原题描述：")) {
                continue;
            }

            dailyNews.addQuestionTitle(questionTitleElements.get(j).text());
        }

        for (Element viewMoreElement : viewMoreElements) {
            Elements viewQuestionElement = viewMoreElement.select("a");

            // Unless the url is a link to zhihu, do not add it to the result NewsList
            if (viewQuestionElement.text().equals("查看知乎讨论")) {
                dailyNews.addQuestionUrl(viewQuestionElement.attr("href"));
            } else {
                return false;
            }
        }

        return true;
    }

    private boolean processSingle(Document doc, Elements viewMoreElements, DailyNews dailyNews) {
        dailyNews.setMulti(false);

        Elements viewQuestionElement = viewMoreElements.select("a");
        if (viewQuestionElement.text().equals("查看知乎讨论")) {
            dailyNews.setQuestionUrl(viewQuestionElement.attr("href"));
        } else {
            return false;
        }

        // Question title is the same with daily title
        if (doc.getElementsByClass("question-title").text().length() == 0) {
            dailyNews.setQuestionTitle(dailyNews.getDailyTitle());
        } else {
            if (doc.getElementsByClass("question-title").size() == 1) {
                dailyNews.setQuestionTitle(doc.getElementsByClass("question-title").text());
            } else {
                for (Element questionTitle : doc.getElementsByClass("question-title")) {
                    if (!(questionTitle.text().equals("原题描述：") || questionTitle.text().equals("原题描述"))) {
                        dailyNews.setQuestionTitle(questionTitle.text());
                        break;
                    }
                }
            }
        }

        return true;
    }
}