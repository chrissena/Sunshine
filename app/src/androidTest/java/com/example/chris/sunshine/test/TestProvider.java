package com.example.chris.sunshine.test;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.chris.sunshine.data.WeatherContract.LocationEntry;
import com.example.chris.sunshine.data.WeatherContract.WeatherEntry;

import java.util.Map;
import java.util.Set;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    static long locationRowId;
    static long weatherRowId;
    static public String TEST_CITY_NAME = "North Pole";
    static public String TEST_LOCATION = "99705";
    static public String TEST_DATE = "20141205";
    static public ContentValues weatherContentValues;

    // The target api annotation is needed for the call to keySet -- we wouldn't want
    // to use this in our app, but in a test it's fine to assume a higher target.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }

    public void setUp() {
        deleteAllRecords();
    }

    ContentValues getLocationContentValues(){
        //Test data we're going to insert into the DB to see
        double testLatitude = 64.772;
        double testLongitude = -147.355;

        //Create a new map of locationValues, where column names are the keys
        ContentValues locationValues = new ContentValues();
        locationValues.put(LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING,TEST_LOCATION);
        locationValues.put(LocationEntry.COLUMN_COORD_LAT,testLatitude);
        locationValues.put(LocationEntry.COLUMN_COORD_LONG,testLongitude);

        return locationValues;
    }
    ContentValues getWeatherContentValues(long locationRowId){
        String testDate, testDescription;
        Double testDegrees, testWind, testHumidity, testPressure;
        int testMin, testMax, testId;
        long testKey;

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, testKey = locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
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

    public void testInsertReadProviderLocation(){

        mContext.getContentResolver().delete(LocationEntry.CONTENT_URI,null,null);
        mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,null,null);
       ContentValues locationValues = getLocationContentValues();
        Uri addedContentUri = mContext.getContentResolver()
                .insert(LocationEntry.CONTENT_URI, locationValues);
        locationRowId = ContentUris.parseId(addedContentUri);
        Cursor locationCursor;


        //Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG,"New row id: " + locationRowId);


        // A cursor is your primary interface to the query results.
        locationCursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),  //Uri to query
                null, //Columns for the "where" clause
                null, //Values for the "where" clause
                null, //columns to group by
                null  //sort order
        );
        if(locationCursor.moveToFirst()){
            validateCursor(locationValues,locationCursor);


        } else {
            fail("No values returned :(");
        }
        locationCursor.close();

    }
    public void testInsertReadProviderBoth() {
        //bring Db to empty state


        //reenter the location data
        testInsertReadProviderLocation();

        //now enter correspoonding weather data
        weatherContentValues = getWeatherContentValues(locationRowId);

        Uri addedContentUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, weatherContentValues);
        weatherRowId = ContentUris.parseId(addedContentUri);
        Cursor weatherCursor  = null;

        try {
            // A weatherCursor is your primary interface to the query results.
            weatherCursor = mContext.getContentResolver().query(
                    WeatherEntry.CONTENT_URI,  //Uri to query
                    null, //Projection
                    null, //Columns for the "where" clause
                    null, //Values for the "where" clause
                    null  //columns to group by
            );
            if(weatherCursor.moveToFirst()){
                validateCursor(weatherContentValues,weatherCursor);
            } else {
                //That's weird, it works on MY machine...
                fail("No values returned :(");

            }
            weatherCursor.close();

            // A weatherCursor is your primary interface to the query results.
            weatherCursor = mContext.getContentResolver().query(
                    WeatherEntry.buildWeatherLocation(TEST_LOCATION),  //Uri to query
                    null, //Projection
                    null, //Columns for the "where" clause
                    null, //Values for the "where" clause
                    null  //columns to group by
            );
            if(weatherCursor.moveToFirst()){
                validateCursor(weatherContentValues,weatherCursor);


            } else {
                //That's weird, it works on MY machine...
                fail("No values returned :(");

            }
              // A weatherCursor is your primary interface to the query results.
            weatherCursor = mContext.getContentResolver().query(
                    WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION
                            ,TEST_DATE),  //Uri to query
                    null, //Projection
                    null, //Columns for the "where" clause
                    null, //Values for the "where" clause
                    null  //columns to group by
            );
            if(weatherCursor.moveToFirst()){
                validateCursor(weatherContentValues, weatherCursor);
            } else {
                //That's weird, it works on MY machine...
                fail("No values returned :(");

            }
            // A weatherCursor is your primary interface to the query results.
            weatherCursor = mContext.getContentResolver().query(
                    WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION
                            , TEST_DATE),  //Uri to query
                    null, //Projection
                    null, //Columns for the "where" clause
                    null, //Values for the "where" clause
                    null  //columns to group by
            );

            if(weatherCursor.moveToFirst()){
                validateCursor(weatherContentValues, weatherCursor);
            } else {
                //That's weird, it works on MY machine...
                fail("No values returned :(");

            }

        } finally {
            weatherCursor.close();
        }


    }
    public void testUpdateBoth(){
        testInsertReadProviderBoth();
        addAllContentValues(weatherContentValues, getLocationContentValues());

        mContext.getContentResolver().update(LocationEntry.CONTENT_URI,getLocationContentValues(),null,null);
    }
    public void testGetType(){
        //content://com.example.chris.sunshine/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        //vnd.android.cursor.dir/com.example.chris.cunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE,type);

        String testLocation = "94074";
        //content://com.example.chris.sunshine/weather/94074
        type = mContext.getContentResolver()
                .getType(WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.chris.sunshine/weather
        WeatherEntry.buildWeatherLocation(testLocation);
        assertEquals(WeatherEntry.CONTENT_TYPE,type);

        String testDate = "20146012";
        // content://com.example.chris.sunshine/weather/94074/20140506
        type = mContext.getContentResolver()
                .getType(WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.chris.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE,type);

        //content://com.example.chris.sunshine/location/1
        type = mContext.getContentResolver()
                .getType(LocationEntry.buildLocationUri(1L));
        //vnd.android.cursor.iitem/com.example.chris.sunshine/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE,type);
    }

    // brings our database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
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
