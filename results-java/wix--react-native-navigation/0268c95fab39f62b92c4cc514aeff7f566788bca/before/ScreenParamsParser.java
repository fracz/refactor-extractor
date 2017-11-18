package com.reactnativenavigation.params.parsers;

import android.os.Bundle;

import com.reactnativenavigation.params.ScreenParams;

public class ScreenParamsParser {
    private static final String KEY_TITLE = "title";
    private static final String KEY_SCREEN_ID = "screenId";
    private static final String KEY_SCREEN_INSTANCE_ID = "screenInstanceID";
    private static final String KEY_NAVIGATOR_EVENT_ID = "navigatorEventID";
    private static final String KEY_PROPS = "passProps";
    private static final String KEY_NAVIGATION_PARAMS = "navigationParams";
    private static final String KEY_BUTTONS = "titleBarButtons";
    private static final String STYLE_PARAMS = "styleParams";

    public static ScreenParams parse(Bundle params) {
        ScreenParams result = new ScreenParams();
        result.screenId = params.getString(KEY_SCREEN_ID);
        result.screenInstanceId = params.getString(KEY_SCREEN_INSTANCE_ID);
        result.passProps = params.getBundle(KEY_PROPS);
        result.navigationParams = params.getBundle(KEY_NAVIGATION_PARAMS);
        assert result.navigationParams != null;
        result.navigatorEventId = result.navigationParams.getString(KEY_NAVIGATOR_EVENT_ID);
        result.buttons = TitleBarButtonParamsParser.parse(params.getBundle(KEY_BUTTONS));
        result.title = params.getString(KEY_TITLE);
        result.styleParams = ScreenStyleParamsParser.parse(params.getBundle(STYLE_PARAMS));
        return result;
    }
}