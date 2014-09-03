package com.example.chris.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Chris on 02/09/2014.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;
    boolean mUseTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;

    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId;
        layoutId = ((viewType == VIEW_TYPE_TODAY) && mUseTodayLayout) ?
                R.layout.list_item_forecast_today
                : R.layout.list_item_forecast;
        View listItemView = LayoutInflater.from(context)
                .inflate(layoutId, parent, false);
        listItemView.setTag(new ViewHolder(listItemView));
        return listItemView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        int resourceId = (cursor.getPosition() == 0 && mUseTodayLayout) ?
                Utility.getArtResourceForWeatherCondition(weatherId) :
                Utility.getIconResourceForWeatherCondition(weatherId);
        // Use placeholder image for now
        holder.iconView.setImageResource(resourceId);
        // Read date from cursor
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        holder.dateView.setText(Utility.getFriendlyDayString(context, dateString));
        // Read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        holder.descriptionView.setText(description);
        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        //Find TextView and set formatted high temperature on it
        holder.highTextView.setText(Utility.formatTemperature(context, high));
        // Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        //Find TextView and set formatted low temperature on it
        holder.lowTextView.setText(Utility.formatTemperature(context, low));

    }

    static class ViewHolder {
        View view;

        ImageView iconView;
        TextView dateView;
        TextView descriptionView;
        TextView highTextView;
        TextView lowTextView;

        ViewHolder(View listItemView) {
            view = listItemView;

            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}
