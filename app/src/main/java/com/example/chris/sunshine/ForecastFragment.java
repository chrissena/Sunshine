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
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.chris.sunshine.data.WeatherContract.LocationEntry;
import com.example.chris.sunshine.data.WeatherContract.WeatherEntry;
import com.example.chris.sunshine.service.SunshineService;

import java.util.Date;

/**
 *Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 * Created by Chris on 26/08/2014.
 */
@SuppressWarnings("WeakerAccess")
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int COL_WEATHER_ID = 6;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
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
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID
    };
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final String POSITION_KEY = "position";
    private int mPosition;
    private String mLocation;
    private boolean mTwoPaneMode = false;
    private ForecastAdapter mForecastAdapter;
    private ListView mForecastListView;
    public ForecastFragment() {
    }

    public void setTwoPaneMode(boolean twoPaneMode) {

        if (mForecastAdapter != null) {
            mTwoPaneMode = twoPaneMode;
            mForecastAdapter.setUseTodayLayout(!twoPaneMode);
        }
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


        Intent serivceIntent = new Intent(getActivity(), SunshineService.class);
        serivceIntent.putExtra(SunshineService.LOCATION_QUERY_KEY,
                Utility.getPreferredLocation(getActivity()));
        getActivity().startService(serivceIntent);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(POSITION_KEY, mPosition);

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ForecastAdapter(
                getActivity(),
                null,
                0
        );
        mForecastAdapter.setUseTodayLayout(!mTwoPaneMode);
        mForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mForecastAdapter);
        mForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mPosition = position;
                String date;
                CursorAdapter adapter = (CursorAdapter) adapterView.getAdapter();
                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);
                date = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
                ((Callback) getActivity()).onItemSelected(date);
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        } else {
            mPosition = -1;
        }
        return rootView;


    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !Utility.getPreferredLocation(getActivity()).equals(mLocation)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
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

        if (mTwoPaneMode) {
            if (mPosition != ListView.INVALID_POSITION) {
                clickListItem(mPosition);
            } else {
                clickListItem(0);
            }
        } else if (mPosition != ListView.INVALID_POSITION) {
            mForecastListView.setSelection(mPosition);
        }
    }

    private void clickListItem(final int position) {
        mForecastListView.post(new Runnable() {
            @Override
            public void run() {
                mForecastListView.performItemClick(mForecastListView.getChildAt(position),
                        position, mForecastListView.getItemIdAtPosition(position));
            }
        });
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String date);
    }

}
