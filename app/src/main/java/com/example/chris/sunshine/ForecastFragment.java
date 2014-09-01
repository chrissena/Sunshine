package com.example.chris.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chris.sunshine.data.WeatherContract.LocationEntry;
import com.example.chris.sunshine.data.WeatherContract.WeatherEntry;

import java.util.Date;

/**
 Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 * Created by Chris on 26/08/2014.
 */
@SuppressWarnings("WeakerAccess")
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private String mLocation;
    private static final int FORECAST_LOADER = 0;
    //For the forecast view we're showing only a small subset of the stored data.
    //Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
           /*In this case the id needs to be fully qualified with a table name, since
           * the content provider joins the location & weather tables in the background
           * (both have an _id column)
           * On the one hand, that's annoying. On the other, you can search the weather table
           * using the postalcode which is only in the Location table. So the convenience
           * is worth it.*/
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING
    };
    //These indices are tied to FORECAST_COLUMNS. If FORECAST_COLUMNS changes, these must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_LOCATION_SETTING = 5;

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private SimpleCursorAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh){
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        FetchWeatherTask fetchWeather = new FetchWeatherTask(getActivity());

        fetchWeather.execute(Utility.getPreferredLocation(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_forecast,
                null,
                new String[]{
                        WeatherEntry.COLUMN_DATETEXT,
                        WeatherEntry.COLUMN_SHORT_DESC,
                        WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherEntry.COLUMN_MIN_TEMP
                },
                new int[]{
                        R.id.list_item_date_textview,
                        R.id.list_item_forecast_textview,
                        R.id.list_item_high_textview,
                        R.id.list_item_low_textview
                },
                0
        );
        mForecastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (columnIndex){
                    case COL_WEATHER_MAX_TEMP:
                    case COL_WEATHER_MIN_TEMP:{
                        //we have to do some formatting and possibly a conversion.
                        ((TextView) view).setText(Utility.formatTemperature
                                (getActivity(),cursor.getDouble(columnIndex))+"\u00B0");
                        return true;
                    }
                    case COL_WEATHER_DATE:{
                        String dateString = cursor.getString(columnIndex);
                        TextView dateView = (TextView) view;
                        dateView.setText(Utility.formatDate(dateString));
                        return true;
                    }
                }
                return false;
            }
        });

        ListView mForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mForecastAdapter);
        mForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String date;

                Intent detailIntent = new Intent(getActivity(),DetailActivity.class);
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) adapterView.getAdapter();
                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);
                date = cursor.getString(COL_WEATHER_DATE);
                detailIntent.putExtra(Intent.EXTRA_TEXT,date);
                startActivity(detailIntent);

            }
        });
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !Utility.getPreferredLocation(getActivity()).equals(mLocation)){
            getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherEntry.getDbDateString(new Date());

        //Sort order: Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(mLocation,
                startDate);

        //Now create and return a CursorLoader that will take care of
        //creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }
    @Override
    public void onLoaderReset(Loader loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER,null,this);
    }
}
