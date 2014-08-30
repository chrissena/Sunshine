package com.example.chris.sunshine;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Chris on 30/08/2014.
 */
public class Utility {
    public static String getPreferredLocation(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_location_key),
                        context.getString(R.string.pref_location_default));
    }
}