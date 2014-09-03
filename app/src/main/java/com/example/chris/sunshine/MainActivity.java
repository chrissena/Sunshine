package com.example.chris.sunshine;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


@SuppressWarnings("WeakerAccess")
public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {
    boolean mTwoPane;

    @Override
    public void onItemSelected(String date) {

        Bundle detailBundle = new Bundle();
        detailBundle.putString(DetailActivity.DATE_KEY, date);

        if (!mTwoPane) {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtras(detailBundle);
            startActivity(detailIntent);
        } else {
            DetailFragment details = new DetailFragment();
            details.setArguments(detailBundle);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.weather_detail_container, details);
            ft.commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

            FragmentManager fragmentManager = getSupportFragmentManager();
            ForecastFragment forecastFragment =
                    (ForecastFragment) fragmentManager.findFragmentById(R.id.fragment_forecast);

            forecastFragment.setTwoPaneMode(mTwoPane);

            if (savedInstanceState == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (id == R.id.view_location) {
            if (openPreferedLocationInMap()) return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean openPreferedLocationInMap() {
        String location = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("geo")
                .appendPath("0,0")
                .appendQueryParameter("q", location);

        Intent locationIntent = new Intent(Intent.ACTION_VIEW, builder.build());
        if (locationIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(locationIntent);
            return true;
        }
        Toast.makeText(this, "Please install a maps app.", Toast.LENGTH_SHORT).show();
        return false;
    }

}
