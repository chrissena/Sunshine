package com.example.chris.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
    public static class PlaceholderFragment extends Fragment {

        private String mForecast;
        private static final String SHARE_HASHTAG = " #SunshineApp";
        private final String LOG_TAG = PlaceholderFragment.class.getSimpleName();
        public PlaceholderFragment() {
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
                    .putExtra(Intent.EXTRA_TEXT,mForecast + SHARE_HASHTAG)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            if(shareActionProvider != null){
                shareActionProvider.setShareIntent(shareIntent);
            }else{
                Log.d(LOG_TAG,"Action Provider is null!");
            }
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            mForecast = getActivity().getIntent().getExtras().getString(Intent.EXTRA_TEXT);
            ((TextView) rootView.findViewById(R.id.forecast_textview)).setText(mForecast);
            setHasOptionsMenu(true);
            return rootView;
        }
    }
}
