package com.example.chris.sunshine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

class WeatherDataParser {

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

    /**
     * Prepare the weather high/lows for presentation.
     */
    private static String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return roundedHigh + "/" + roundedLow;

    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wire frames.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public static String[] getWeatherDataFromJson(String forecastJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";

        JSONArray weatherArray = new JSONObject(forecastJsonStr)
                .getJSONArray(OWM_LIST);
        String[] formattedArray = new String[weatherArray.length()];
        for (int i =0; i< weatherArray.length();i++ ){

            String date = getReadableDateString(weatherArray.getJSONObject(i)
                    .getInt(OWM_DATETIME));

            String description = weatherArray.getJSONObject(i)
                    .getJSONArray(OWM_WEATHER).getJSONObject(0)
                    .getString(OWM_DESCRIPTION);

            JSONObject tempJson = weatherArray.getJSONObject(i)
                    .getJSONObject(OWM_TEMPERATURE);
            String highLow = formatHighLows((tempJson.getDouble(OWM_MAX)),
                    (tempJson.getDouble(OWM_MIN)));
            formattedArray[i] = String.format("%s - %s - %s",date,description,highLow);
        }
        return formattedArray;
    }
}
