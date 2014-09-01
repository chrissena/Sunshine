package com.example.chris.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chris.sunshine.data.WeatherContract;


@SuppressWarnings("WeakerAccess")
public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this,SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }else if(id == R.id.view_location){
            if (openPreferedLocationInMap()) return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean openPreferedLocationInMap() {
        String location =  PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("geo")
                .appendPath("0,0")
                .appendQueryParameter("q", location);

        Intent locationIntent = new Intent(Intent.ACTION_VIEW,builder.build());
        if (locationIntent.resolveActivity(getPackageManager())!= null){
            startActivity(locationIntent);
            return true;
        }
        Toast.makeText(this, "Please install a maps app.", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final int DETAIL_LOADER = 0;
        private String mDate;
        private static final String SHARE_HASHTAG = " #SunshineApp";
        private String mDetail;
        private double mHigh;
        private double mLow;
        private String mLocation;
        private String mForecastSummary;

        private static final String[] FORECAST_COLUMNS = {
           /*In this case the id needs to be fully qualified with a table name, since
           * the content provider joins the location & weather tables in the background
           * (both have an _id column)
           * On the one hand, that's annoying. On the other, you can search the weather table
           * using the postalcode which is only in the Location table. So the convenience
           * is worth it.*/
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
        };
        //These indices are tied to FORECAST_COLUMNS. If FORECAST_COLUMNS changes, these must change.


        public static final int COL_WEATHER_DESC = 2;
        public static final int COL_WEATHER_MAX_TEMP = 3;
        public static final int COL_WEATHER_MIN_TEMP = 4;


        private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
        public PlaceholderFragment() {
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putString(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, mLocation);
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
                Log.d(LOG_TAG,"Action Provider is null!");
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


            ((TextView) rootView.findViewById(R.id.forecast_textview)).setText(mDate);
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
                mHigh = cursor.getDouble(COL_WEATHER_MAX_TEMP);
                mLow = cursor.getDouble(COL_WEATHER_MIN_TEMP);
                mDetail = cursor.getString(COL_WEATHER_DESC);


                TextView dateView = (TextView) getActivity().findViewById(R.id.date_textview);
                TextView detailView = (TextView) getActivity().findViewById(R.id.forecast_textview);
                TextView highView = (TextView) getActivity().findViewById(R.id.high_textview);
                TextView lowView = (TextView) getActivity().findViewById(R.id.low_textview);
                dateView.setText(Utility.formatDate(mDate));
                detailView.setText(mDetail);
                highView.setText(Utility.formatTemperature(getActivity(), mHigh)+"\u00B0");
                lowView.setText(Utility.formatTemperature(getActivity(), mLow)+"\u00B0");

                mForecastSummary = String.format("Forecast for %s on %s: %s - %s/%s",
                        mLocation,
                        dateView.getText(),
                        detailView.getText(),
                        highView.getText(),lowView.getText());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }
}
