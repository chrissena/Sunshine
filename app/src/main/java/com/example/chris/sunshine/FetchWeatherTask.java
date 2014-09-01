package com.example.chris.sunshine;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.chris.sunshine.data.WeatherContract;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/**
 * AsyncTask to retrieve data from OpenWeatherMap API
 * Created by Chris on 30/08/2014.
 */
class FetchWeatherTask extends AsyncTask<String, Void, Void>
{
    private final Context mContext;


    FetchWeatherTask(Context context){
        mContext = context;

    }

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    @Override
    protected Void doInBackground(String... params) {
        WeatherDataParser parser;
        HttpURLConnection weatherHttpConnection = null;
        BufferedReader reader = null;
        String weatherJson;
        String locationQuery = params[0],
                days ="14",
                units = "metric",
                mode = "json";


        try {
            //Connect to server & create stream
            Uri.Builder uriBuilder =
                    Uri.parse(mContext.getString(R.string.weatherApiBaseUrl)).buildUpon();

            uriBuilder.appendQueryParameter("q",locationQuery)
                    .appendQueryParameter("mode",mode)
                    .appendQueryParameter("units",units)
                    .appendQueryParameter("cnt",days);

            String urlString = uriBuilder.build().toString();
            URL weatherApiUrl= new URL(urlString);
            weatherHttpConnection =(HttpURLConnection) weatherApiUrl.openConnection();
            weatherHttpConnection.setRequestMethod("GET");
            weatherHttpConnection.connect();

            //Create BufferedStream and Buffered Reader
            BufferedInputStream bufferedStream =
                    new BufferedInputStream(weatherHttpConnection.getInputStream());

            //Read stream lines to stringBuilder and make JSON String
            reader = new BufferedReader(new InputStreamReader(bufferedStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)  builder.append(line);
            weatherJson = builder.toString();

            //Instantiate a JSON parser to retrieve data
            parser  = new WeatherDataParser(weatherJson);

            // Add data in parser to database
            long locationId  = addLocation(locationQuery,parser.cityName,
                    parser.cityLatitude,parser.cityLongitude);
            parser.updateLocationId(locationId);
            //update weather forecasts in parser with locationId from db
            Vector<ContentValues> wVector = parser.weatherVector;
            if (wVector.size() > 0) {
                ContentValues[] weatherArray = new ContentValues[wVector.size()];
                wVector.toArray(weatherArray);
                addWeather(weatherArray);
            }

            Log.d(LOG_TAG,"FetchWeatherTask Complete " + wVector.size() + " Inserted");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "UrlError - fetchJson", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Connection error - fetchJson", e);
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing JSON - fetchJson", e);
            e.printStackTrace();
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage(),e);
            e.printStackTrace();
        }
        finally {
            if (weatherHttpConnection != null) weatherHttpConnection.disconnect();
            if (reader != null) try {
                reader.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error closing stream - fetchJson", e);
            }
        }
        return null;
    }

    private long addLocation (String locationSetting,
                              String cityName, double lat, double lon){

        Log.v(LOG_TAG,String.format("inserting %s, with coord: %s,%s",cityName,lat,lon));
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(WeatherContract.LocationEntry.CONTENT_URI,
                null,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);
        try {
            if (cursor.moveToFirst()) {
                Log.v(LOG_TAG, "Found it in the database!");
                return cursor.getLong(cursor.getColumnIndex(WeatherContract.LocationEntry._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,locationSetting);
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME,cityName);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT,lat);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG,lon);
        Uri uri = resolver.insert(WeatherContract.LocationEntry.CONTENT_URI, values);
        return ContentUris.parseId(uri);
    }

    @Override
    protected void onPostExecute(Void result ) {
     /*   if (result != null) {
            mForecastAdapter.clear();
            for(String dayForecastStr : result) {
                mForecastAdapter.add(dayForecastStr);
            }
            // New data is back from the server.  Hooray!
        }*/
    }

    @SuppressWarnings("UnusedReturnValue")
    private long addWeather (ContentValues[] weatherValues){
        Log.v(LOG_TAG,String.format("inserting %s forecasts", weatherValues.length));
        ContentResolver resolver = mContext.getContentResolver();

        return resolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);

    }

}