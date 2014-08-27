package com.example.chris.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying main weather forecast
 * Created by Chris on 26/08/2014.
 */
@SuppressWarnings("WeakerAccess")
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> mForecastAdapter;

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

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private void updateWeather() {
        FetchWeatherTask fetchWeather = new FetchWeatherTask();
        fetchWeather.execute(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_location_key),
                        getString(R.string.pref_location_default)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> weekForecast = new ArrayList<String>(new ArrayList<String>());
        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);
        ListView mForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mForecastAdapter);
        mForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Intent detailIntent = new Intent(getActivity(),DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(position));
                startActivity(detailIntent);

            }
        });
        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]>
    {
        @Override
        protected void onPostExecute(String[] forecast) {

            if (forecast != null) {
                mForecastAdapter.clear();
                for (String dayForecast : forecast) {
                    mForecastAdapter.add(dayForecast);
                }
            }
        }

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection weatherHttpConnection = null;
            BufferedReader reader = null;
            String weatherJson;
            String location = params[0],
                    days ="7",
                    units = "metric",
                    mode = "json";
            String[] formattedArray = null;

            try {
                //Connect to server & create stream
                Uri.Builder uriBuilder =
                        Uri.parse(getString(R.string.weatherApiBaseUrl)).buildUpon();

                uriBuilder.appendQueryParameter("q",location)
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

                //Read stream lines to stringBuilder

                reader = new BufferedReader(new InputStreamReader(bufferedStream));
                StringBuilder builder = new StringBuilder();


                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                weatherJson = builder.toString();
                boolean metric = (PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(
                                getString(R.string.pref_units_key),
                                "Metric"))
                        .equalsIgnoreCase("Metric");

                formattedArray =
                        WeatherDataParser.getWeatherDataFromJson(
                                weatherJson,metric);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "UrlError - fetchJson", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Connection error - fetchJson", e);

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing JSON - fetchJson", e);
            }
            finally {
                if (weatherHttpConnection != null) weatherHttpConnection.disconnect();
                if (reader != null) try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream reader - fetchJson", e);
                }
            }
            //Return formatted Json String[]
            return formattedArray;
        }
    }
}
