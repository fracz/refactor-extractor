package com.grosner.dbflow.test.typeconverter;

import android.location.Location;

import com.grosner.dbflow.sql.language.Delete;
import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.test.FlowTestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TypeConverterTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "typeconverter";
    }

    public void testConverters() {

        new Delete().from(TestType.class).where().query();

        TestType testType = new TestType();
        testType.name = "Name";

        long testTime = System.currentTimeMillis();

        // calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(testTime);
        testType.calendar = calendar;


        Date date = new Date(testTime);
        testType.date = date;

        java.sql.Date date1 = new java.sql.Date(testTime);
        testType.sqlDate = date1;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{ name: test, happy: true }");
            testType.json = jsonObject;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Location location = new Location("test");
        location.setLatitude(40.5);
        location.setLongitude(40.5);
        testType.location = location;

        testType.save(false);

        TestType retrieved = Select.byId(TestType.class, "Name");
        assertNotNull(retrieved);

        assertNotNull(retrieved.calendar);
        assertTrue(retrieved.calendar.equals(calendar));

        assertNotNull(retrieved.date);
        assertTrue(retrieved.date.equals(date));

        assertNotNull(retrieved.sqlDate);
        assertTrue(retrieved.sqlDate.equals(date1));

        assertNotNull(retrieved.json);
        assertTrue(retrieved.json.toString().equals(jsonObject.toString()));

        assertNotNull(retrieved.location);
        assertTrue(retrieved.location.getLongitude() == location.getLongitude());
        assertTrue(retrieved.location.getLatitude() == location.getLatitude());

    }

}