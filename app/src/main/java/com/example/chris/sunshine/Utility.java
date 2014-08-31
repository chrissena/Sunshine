package com.example.chris.sunshine;

import android.content.Context;
import android.preference.PreferenceManager;

import com.example.chris.sunshine.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Chris on 30/08/2014.
 */
public class Utility {

    static  String formatTemperature(Context context, double temperature){
                double temp;
        if (!isMetric(context)){
            temp = temperature * (9/5) +32;
        }else {
            temp = temperature;
                    }
        return String.format("%.0f", temp);
    }
    public static String getPreferredLocation(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_location_key),
                        context.getString(R.string.pref_location_default));
    }
    public static boolean isMetric(Context context){
       return (PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                        context.getString(R.string.pref_units_key),
                        "Metric"))
                .equalsIgnoreCase("Metric");
    }
    static String formatDate(String dateString) {
        Date date = WeatherContract.WeatherEntry.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }

}