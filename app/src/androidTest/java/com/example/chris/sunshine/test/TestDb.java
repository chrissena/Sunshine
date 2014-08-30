package com.example.chris.sunshine.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.chris.sunshine.data.WeatherContract.LocationEntry;
import com.example.chris.sunshine.data.WeatherContract.WeatherEntry;
import com.example.chris.sunshine.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();
    long locationRowId;
    public String testName = "North Pole";


    ContentValues getLocationContentValues(){
        //Test data we're going to insert into the DB to see

        String testLocationSetting = "99705";
        double testLatitude = 64.772;
        double testLongitude = -147.355;

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING,testLocationSetting);
        values.put(LocationEntry.COLUMN_COORD_LAT,testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG,testLongitude);

        return values;
    }
    ContentValues getWeatherContentValues(long locationRowId){
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
        return weatherValues;
    }
    public void testCreateDb() throws Throwable{

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true,db.isOpen());
        db.close();
    }

    public void testInsertReadLocationDb(){

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = getLocationContentValues();
        locationRowId = db.insert(LocationEntry.TABLE_NAME,null, values);

        //Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG,"New row id: " + locationRowId);


        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,  //Table to Query
                null,
                null, //Columns for the "where" clause
                null, //Values for the "where" clause
                null, //columns to group by
                null, //columns to filter by row groups
                null  //sort order
        );
        if(cursor.moveToFirst()){
            validateCursor(values,cursor);

            dbHelper.close();
        } else {
            fail("No values returned :(");
        }

    }
    public void testInsertReadWeatherDb() {

        ContentValues weatherValues = getWeatherContentValues(locationRowId);
        //errors will be thrown here when you try to get a writable databaase.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.insert(WeatherEntry.TABLE_NAME,null, weatherValues);

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                WeatherEntry.TABLE_NAME,  //Table to Query
                null,
                null, //Columns for the "where" clause
                null, //Values for the "where" clause
                null, //columns to group by
                null, //columns to filter by row groups
                null  //sort order
        );
        if(cursor.moveToFirst()){
            validateCursor(weatherValues,cursor);

            dbHelper.close();
        } else {
            //That's weird, it works on MY machine...
            fail("No values returned :(");

        }
    }

    static public void validateCursor(ContentValues expectedValues, Cursor valueCursor){
        Set<Map.Entry<String,Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String,Object> entry : valueSet){
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(-1 == idx);
            String expectedValue = entry.getValue().toString();

            assertEquals(expectedValue, valueCursor.getString(idx));
        }
    }


}
