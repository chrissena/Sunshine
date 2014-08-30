package com.example.chris.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.chris.sunshine.data.WeatherContract.LocationEntry;
import com.example.chris.sunshine.data.WeatherContract.WeatherEntry;


/**
 * Content provider for weather & location data
 * Created by Chris on 29/08/2014.
 */
@SuppressWarnings("WeakerAccess")
public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;
    private WeatherDbHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;
    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        LocationEntry.TABLE_NAME +
                        " ON " + WeatherEntry.TABLE_NAME +
                        "." + WeatherEntry.COLUMN_LOC_KEY +
                        " = " + LocationEntry.TABLE_NAME +
                        "." + LocationEntry._ID);
    }
    private static final String sLocationSettingSelection =
            LocationEntry.TABLE_NAME +
                    "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
    private static final String sLocationSettingWIthStartDateSelection =
            LocationEntry.TABLE_NAME +
                    "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherEntry.COLUMN_DATETEXT + " >= ?";
    private static final String sLocationSettingWIthDateSelection =
            LocationEntry.TABLE_NAME +
                    "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherEntry.COLUMN_DATETEXT + " = ?";

    private  Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOder){
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        }else{
            selectionArgs = new String[]{locationSetting, startDate};
            selection = sLocationSettingWIthStartDateSelection;
        }


        return  sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,selection,selectionArgs,null,null,sortOder);
    }
    private  Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOder){
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        String date = WeatherEntry.getDateFromUri(uri);

        String[] selectionArgs;
        String selection;
        selectionArgs = new String[]{locationSetting, date};
        selection = sLocationSettingWIthDateSelection;



        return  sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,selection,selectionArgs,null,null,sortOder);
    }


    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        matcher.addURI(authority,WeatherContract.PATH_WEATHER,WEATHER);
        matcher.addURI(authority,WeatherContract.PATH_WEATHER + "/*",WEATHER_WITH_LOCATION);
        matcher.addURI(authority,WeatherContract.PATH_WEATHER + "/*/*",
                WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(authority,WeatherContract.PATH_LOCATION,LOCATION);
        matcher.addURI(authority,WeatherContract.PATH_LOCATION + "/#",LOCATION_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        //Here's the switch statement that, given a URI, will determine what kind of request
        //and query the database accord
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            //"weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE: {
                retCursor = getWeatherByLocationSettingAndDate(uri,projection,sortOrder);
                break;
            }
            //"weather/*"
            case WEATHER_WITH_LOCATION:{
                retCursor = getWeatherByLocationSetting(uri,projection,sortOrder);
                break;
            }
            //"weather"
            case WEATHER:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            //"location/"
            case LOCATION:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            //"location/#"
            case LOCATION_ID:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        LocationEntry._ID + "=?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match){
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return  WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match){
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values){
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db =  mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long id;
        switch (match){
            case WEATHER:
                id = db.insert(WeatherEntry.TABLE_NAME,null,contentValues);
                if (id > 0)
                    returnUri = WeatherEntry.buildWeatherUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case LOCATION:
                id = db.insert(LocationEntry.TABLE_NAME,null,contentValues);
                if (id > 0)
                    returnUri = LocationEntry.buildLocationUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db =  mOpenHelper.getWritableDatabase();
        int number;
        switch (match){
            case WEATHER:
                number = db.delete(WeatherEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case LOCATION:
                number = db.delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (number >= 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return number;
        }else
            throw new android.database.SQLException("Failed to delete: " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db =  mOpenHelper.getWritableDatabase();
        int number;
        switch (match){
            case WEATHER:
                number = db.update(WeatherEntry.TABLE_NAME,contentValues,
                        selection,selectionArgs);
                break;
            case LOCATION:
                number = db.update(LocationEntry.TABLE_NAME,contentValues,
                        selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (number > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return number;
        }else
            throw new android.database.SQLException("Failed to delete: " + uri);
    }
}
