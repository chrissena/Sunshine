package com.example.chris.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh){
            FetchWeatherTask fetchWeather = new FetchWeatherTask();
            fetchWeather.execute("London");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);



        String[] forecastArray ={
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72,63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain - 65/56",
                "Sat - HELP TRAPPED IN WEATHER STATION - 60/51",
                "Sun - Sunny - 80/68"
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));
        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);
        ListView mForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListView.setAdapter(mForecastAdapter);
        mForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(),mForecastAdapter.getItem(i),Toast.LENGTH_LONG)
                        .show();
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

                formattedArray =
                        WeatherDataParser.getWeatherDataFromJson(
                                weatherJson);

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
