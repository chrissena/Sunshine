package com.example.chris.sunshine;

import android.content.ContentValues;
import android.util.Log;

import com.example.chris.sunshine.data.WeatherContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
class WeatherDataParser {
    private final static String LOG_TAG = WeatherDataParser.class.getSimpleName();

    //Public properties
    double cityLatitude;
    double cityLongitude;
    Vector<ContentValues> weatherVector = new Vector<ContentValues>();
    String cityName;
    final String locationSetting;
    String[] formattedArray;

    // private fields
    private final String mForecastJsonStr;
    private final int mNumDays;
    private final boolean mMetric;

    WeatherDataParser(String forecastJsonStr, int numDays,
                      String location, boolean metric) throws JSONException{

        formattedArray = null;
        mForecastJsonStr = forecastJsonStr;
        mNumDays = numDays;
        locationSetting = location;
        mMetric = metric;
        getWeatherData();
    }
    /* The date/time conversion code is going to be moved outside the AsyncTask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date);
    }
public void updateLocationId(long locationId){
    for (ContentValues forecast: weatherVector){
        forecast.put(WeatherEntry.COLUMN_LOC_KEY,locationId);
    }
}

    /**
     * Prepare the weather high/lows for presentation.
     */
    private static String formatHighLows(double high, double low, boolean metric) {

        //convert to Farenheit if not metric
        if (!metric) {
            high = (high * 1.8)+32;
            low = (low * 1.8)+32;
        }

        //round to whole numbers
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        //return formatted display string
        return roundedHigh + "/" + roundedLow;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wire frames.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public String[] getWeatherData()
            throws JSONException {

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_COORD_LAT = "lat";
        final String OWM_COORD_LONG = "lon";
        final String OWM_CITY_ID = "id";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";
        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";
        JSONArray weatherArray = null;
        try {
            JSONObject forecastJson = new JSONObject(mForecastJsonStr);
            weatherArray = forecastJson.getJSONArray(OWM_LIST);
            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);

            cityName = cityJson.getString(OWM_CITY_NAME);
            cityLatitude = cityCoord.getDouble(OWM_COORD_LAT);
            cityLongitude = cityCoord.getDouble(OWM_COORD_LONG);


            Log.v(LOG_TAG, cityName + ", with coord: " + cityLatitude
                    + " " + cityLongitude);

        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getMessage(),e);
            e.printStackTrace();
        }

        formattedArray = new String[mNumDays];
        if (weatherArray != null){
            for (int i =0; i< weatherArray.length();i++ ){
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;
                double high;
                double low;
                String description;
                int weatherId;

                JSONObject dayForecast = weatherArray.getJSONObject(i);
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                ContentValues weatherValues = new ContentValues();

                dateTime = dayForecast.getLong(OWM_DATETIME);
                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                weatherValues.put(WeatherEntry.COLUMN_DATETEXT,
                        WeatherEntry.getDbDateString(new Date(dateTime * 1000L)));
                weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                weatherVector.add(weatherValues);

                String day = getReadableDateString(dateTime);

                String highLow = formatHighLows(high,low,mMetric);
                formattedArray[i] = String.format("%s - %s - %s",day,description,highLow);
            }
        }
        return formattedArray;
    }
}
