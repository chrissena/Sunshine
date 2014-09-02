package com.example.chris.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chris.sunshine.data.WeatherContract;
import com.example.chris.sunshine.data.WeatherContract.LocationEntry;
import com.example.chris.sunshine.data.WeatherContract.WeatherEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    private String mDate;
    private static final String SHARE_HASHTAG = " #SunshineApp";
    private String mLocation;
    private String mForecastSummary;
    private TextView mDateView;
    private TextView mDetailView;
    private TextView mHighView;
    private TextView mLowView;
    private TextView mDayVew;
    private TextView mHumidityVew;
    private TextView mWindVew;
    private TextView mPressureVew;
    private ImageView mIconView;


    private static final String[] FORECAST_COLUMNS = {
           /*In this case the id needs to be fully qualified with a table name, since
           * the content provider joins the location & weather tables in the background
           * (both have an _id column)
           * On the one hand, that's annoying. On the other, you can search the weather table
           * using the postalcode which is only in the Location table. So the convenience
           * is worth it.*/
            WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_PRESSURE
    };
    //These indices are tied to FORECAST_COLUMNS. If FORECAST_COLUMNS changes, these must change.

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_PRESSURE = 8;



    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    public DetailFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LocationEntry.COLUMN_LOCATION_SETTING, mLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_fragment,menu);
        MenuItem item = menu.findItem(R.id.action_share);

        ShareActionProvider shareActionProvider
                = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, mForecastSummary + SHARE_HASHTAG)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        if(shareActionProvider != null){
            shareActionProvider.setShareIntent(shareIntent);
        }else{
            Log.d(LOG_TAG, "Action Provider is null!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(mLocation);
        }
        mDate = getActivity().getIntent().getExtras().getString(Intent.EXTRA_TEXT);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mDateView = (TextView) rootView.findViewById(R.id.date_textview);
        mDetailView = (TextView) rootView.findViewById(R.id.forecast_textview);
        mHighView = (TextView) rootView.findViewById(R.id.high_textview);
        mLowView = (TextView) rootView.findViewById(R.id.low_textview);
        mDayVew = (TextView) rootView.findViewById(R.id.day_textview);
        mHumidityVew = (TextView) rootView.findViewById(R.id.humidity_textview);
        mWindVew = (TextView) rootView.findViewById(R.id.wind_textview);
        mPressureVew = (TextView) rootView.findViewById(R.id.pressure_textview);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LocationEntry.COLUMN_LOCATION_SETTING);
        }
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mLocation = Utility.getPreferredLocation(getActivity());
        return new CursorLoader(getActivity(),
                WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation,mDate),
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            int weatherId = cursor.getInt(COL_WEATHER_ID);
            Double high = cursor.getDouble(COL_WEATHER_MAX_TEMP);
            Double low = cursor.getDouble(COL_WEATHER_MIN_TEMP);
            String detail = cursor.getString(COL_WEATHER_DESC);
            String day = Utility.getDayName(getActivity(),mDate);
            String humidity = cursor.getString(COL_WEATHER_HUMIDITY);
            String  wind = cursor.getString(COL_WEATHER_WIND_SPEED);
            String pressure = cursor.getString(COL_WEATHER_PRESSURE);
                        
            mDateView.setText(Utility.formatDate(mDate));
            mHighView.setText(Utility.formatTemperature(getActivity(), high));
            mLowView.setText(Utility.formatTemperature(getActivity(), low));
            mDayVew.setText(day);
            mHumidityVew.setText(String.format("HUMIDITY: %s%%", humidity));
            mWindVew.setText(String.format("WIND: %s km/H", wind));
            mPressureVew.setText(String.format("PRESSURE: %s kPa", pressure));
            mDetailView.setText(detail);
            mIconView.setImageResource(R.drawable.ic_launcher);

            mForecastSummary = String.format("Forecast for %s on %s: %s - %s/%s",
                    mLocation,
                    mDateView.getText(),
                    mDetailView.getText(),
                    mHighView.getText(), mLowView.getText());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}
