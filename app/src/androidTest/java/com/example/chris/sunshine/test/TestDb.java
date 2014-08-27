package com.example.chris.sunshine.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.chris.sunshine.data.WeatherContract.LocationEntry;
import com.example.chris.sunshine.data.WeatherContract.WeatherEntry;
import com.example.chris.sunshine.data.WeatherDbHelper;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();
    long locationRowId;

    public void testCreateDb() throws Throwable{

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true,db.isOpen());
        db.close();
    }

    public void testInsertReadLocationDb(){
        //Test data we're going to insert into the DB to see
        String testName = "North Pole";
        String testLocationSetting = "99705";
        double testLatitude = 64.772;
        double testLongitude = -147.355;

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING,testLocationSetting);
        values.put(LocationEntry.COLUMN_COORD_LAT,testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG,testLongitude);

        //If there's an error in those massive SQL table creation Strings,
        //errors will be thrown here when you try to get a writable databaase.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        locationRowId = db.insert(LocationEntry.TABLE_NAME,null, values);

        //Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG,"New row id: " + locationRowId);

        // Specify which columns you want.
        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,  //Table to Query
                columns,
                null, //Columns for the "where" clause
                null, //Values for the "where" clause
                null, //columns to group by
                null, //columns to filter by row groups
                null  //sort order
        );
        if(cursor.moveToFirst()) {
            //Get the value in each column by finding the appropriate column index.

            int nameIndex = cursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME);
            String name = cursor.getString(nameIndex);

            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);


            int latIndex = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT);
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG);
            double longitude = cursor.getDouble(longIndex);

            //Hurray, data was returned! Assert that it's the right data, and that the database
            //creation code is working as intended.
            //Then take a break. We both know that wasn't easy

            assertEquals(testLocationSetting, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);
            assertEquals(testName, name);

            dbHelper.close();
        } else {
            //That's weird, it works on MY machine...
            fail("No values returned :(");

        }

    }
    public void testInsertReadWeatherDb() {
        // Fantastic.  Now that we have a location, add some weather!
        String testDate, testDescription;
        Double testDegrees, testWind, testHumidity, testPressure;
        int testMin, testMax, testId;
        long testKey;

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, testKey = locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, testDate = "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, testDegrees = 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, testHumidity = 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, testPressure = 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, testMax = 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, testMin = 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, testDescription = "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, testWind = 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, testId = 321);


        //errors will be thrown here when you try to get a writable databaase.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.insert(WeatherEntry.TABLE_NAME,null, weatherValues);
        // Specify which columns you want.
        String[] columns = {
                WeatherEntry.COLUMN_LOC_KEY,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_DEGREES,
                WeatherEntry.COLUMN_HUMIDITY,
                WeatherEntry.COLUMN_PRESSURE,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_WIND_SPEED,
                WeatherEntry.COLUMN_WEATHER_ID
        };


        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                WeatherEntry.TABLE_NAME,  //Table to Query
                columns,
                null, //Columns for the "where" clause
                null, //Values for the "where" clause
                null, //columns to group by
                null, //columns to filter by row groups
                null  //sort order
        );
        if (cursor.moveToFirst()) {
            //Get the value in each column by finding the appropriate column index.
            int locIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY);
            int key = cursor.getInt(locIndex);

            int dateIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT);
            String date = cursor.getString(dateIndex);

            int degIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES);
            double degrees = cursor.getDouble(degIndex);

            int humIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY);
            double humidity = cursor.getDouble(humIndex);

            int presIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE);
            double pressure = cursor.getDouble(presIndex);

            int maxIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP);
            int max = cursor.getInt(maxIndex);

            int minIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP);
            int min = cursor.getInt(minIndex);

            int descIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC);
            String description = cursor.getString(descIndex);

            int windIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED);
            double wind = cursor.getDouble(windIndex);

            int idIndex = cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID);
            long id = cursor.getLong(idIndex);


            //Hurray, data was returned! Assert that it's the right data, and that the database
            //creation code is working as intended.
            //Then take a break. We both know that wasn't easy

            assertEquals(testKey, key);
            assertEquals(testDate, date);
            assertEquals(testDegrees, degrees);
            assertEquals(testHumidity, humidity);
            assertEquals(testPressure, pressure);
            assertEquals(testMax, max);
            assertEquals(testMin, min);
            assertEquals(testDescription, description);
            assertEquals(testWind, wind);
            assertEquals(testId, id);

            dbHelper.close();
        } else {
            //That's weird, it works on MY machine...
            fail("No values returned :(");

        }
    }
}
