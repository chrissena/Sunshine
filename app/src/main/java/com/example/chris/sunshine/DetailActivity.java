package com.example.chris.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chris.sunshine.debug.ViewServer;


@SuppressWarnings("WeakerAccess")
public class DetailActivity extends ActionBarActivity {
final static String DATE_KEY = "date";

    public void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }

    public void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view, etc.

        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, new DetailFragment())
                    .commit();
            ViewServer.get(this).addWindow(this);
        }
        Fragment fragB = new DetailFragment();
        int containerId = R.id.weather_detail_container;
        String tag = null;
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(tag);
        ft.replace(containerId,fragB);
ft.commit();
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

}
