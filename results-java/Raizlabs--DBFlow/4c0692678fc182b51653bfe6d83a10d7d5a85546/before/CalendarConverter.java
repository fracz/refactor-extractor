package com.grosner.dbflow.converter;

import java.util.Calendar;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Defines how we store and retrieve a {@link java.util.Calendar}
 */
public class CalendarConverter implements TypeConverter<Long, Calendar> {
    @Override
    public Class<Long> getDatabaseType() {
        return Long.class;
    }

    @Override
    public Class<Calendar> getModelType() {
        return Calendar.class;
    }

    @Override
    public Long getDBValue(Calendar model) {
        return model.getTimeInMillis();
    }

    @Override
    public Calendar getModelValue(Long data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(data);
        return calendar;
    }
}