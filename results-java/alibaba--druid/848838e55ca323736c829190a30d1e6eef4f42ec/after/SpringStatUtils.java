package com.alibaba.druid.support.spring.stat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;

public class SpringStatUtils {

    private final static Log LOG = LogFactory.getLog(SpringStatUtils.class);

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getMethodStatDataList(Object methodStat) {
        if (methodStat.getClass() == SpringStat.class) {
            return ((SpringStat) methodStat).getMethodStatDataList();
        }

        try {
            Method method = methodStat.getClass().getMethod("getMethodStatDataList");
            Object obj = method.invoke(methodStat);
            return (List<Map<String, Object>>) obj;
        } catch (Exception e) {
            LOG.error("getMethodStatDataList error", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMethodStatData(Object methodStat, String clazz, String methodSignature) {
        if (methodStat.getClass() == SpringStat.class) {
            return ((SpringStat) methodStat).getMethodStatData(clazz, methodSignature);
        }

        try {
            Method method = methodStat.getClass().getMethod("getMethodStatData", String.class, String.class);
            Object obj = method.invoke(methodStat, clazz, methodSignature);
            return (Map<String, Object>) obj;
        } catch (Exception e) {
            LOG.error("getMethodStatDataList error", e);
            return null;
        }
    }

    public static void reset(Object webStat) {
        if (webStat.getClass() == SpringStat.class) {
            ((SpringStat) webStat).reset();
            return;
        }

        try {
            Method method = webStat.getClass().getMethod("reset");
            method.invoke(webStat);
        } catch (Exception e) {
            LOG.error("reset error", e);
        }
    }
}