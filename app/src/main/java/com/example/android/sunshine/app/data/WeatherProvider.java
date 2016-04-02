/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import static com.example.android.sunshine.app.data.WeatherContract.*;


public class WeatherProvider extends ContentProvider {



    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper mOpenHelper;

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        
        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        //.setTables fills out from part of SQL query
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
    }

    //location.location_setting = ?
    private static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";


   // private static final String sLocation = WeatherContract.LocationEntry.TABLE_NAME;


    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }


        Cursor ccc =  sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        int ccc_i = ccc.getCount();

         ccc =  sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder);
        ccc_i = ccc.getCount();


        debugCursor(ccc);


        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }



    private Cursor getWeather(Uri uri, String[] projection, String sortOrder) {

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        //projection = null;
        String selection = null;
        String[] selectionArgs = null;
        sqLiteQueryBuilder.setTables(WeatherContract.WeatherEntry.TABLE_NAME);
        return (sqLiteQueryBuilder.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder));
    }



    private Cursor getLocation(Uri uri, String[] projection, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        //projection = null;
        String selection = null;
        String[] selectionArgs = null;
        sqLiteQueryBuilder.setTables(WeatherContract.LocationEntry.TABLE_NAME);
        return (sqLiteQueryBuilder.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder));
    }



    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.

         UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.

        matcher.addURI(CONTENT_AUTHORITY, PATH_WEATHER, WEATHER);
        matcher.addURI(CONTENT_AUTHORITY, PATH_LOCATION, LOCATION);
        matcher.addURI(CONTENT_AUTHORITY, PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(CONTENT_AUTHORITY, PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);



        // 3) Return the new matcher!
        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = getWeather(uri,projection,sortOrder);
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = getLocation(uri,projection,sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }




    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                normalizeDate(values);
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        int delete_count;

        switch (match) {
            case WEATHER: {
                delete_count = db.delete(WeatherContract.WeatherEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case LOCATION: {
                delete_count = db.delete(WeatherContract.LocationEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //Cursor cc = db.query(LocationEntry.TABLE_NAME,null,null,null,null,null,null);
        //if(cc.moveToFirst()){
         //   int z = cc.getCount();
          //  for (int y = 0;y < z;y++) {
          //      int col = cc.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
          //      String ss = cc.getString(col);
          //      cc.moveToPosition(y);
          //  }

//        }


        if(delete_count != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.

        // Student: return the actual rows deleted
        return delete_count;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
            values.put(WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.

           final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
           final int match = sUriMatcher.match(uri);
           int update_count;

        switch (match) {
            case WEATHER: {
                update_count = db.update(WeatherContract.WeatherEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            }
            case LOCATION: {
                update_count = db.update(WeatherContract.LocationEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(update_count != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return update_count;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
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

                Cursor cc = db.query(WeatherContract.WeatherEntry.TABLE_NAME,null,null,null,null,null,null);
                Log.d("BULK_INSERT","Bulk Insert Cursor");
                WeatherProvider.debugCursor(cc);

                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }


//************************************************************************************************/
//    Cursor Debugger  - Mike Brown 25th March 2016
//************************************************************************************************/
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static public int debugCursor(Cursor cursor){
        String CURSOR_DEBUG = "CURSOR DBG";
        Log.d(CURSOR_DEBUG, "******************** CURSOR DEBUG ************************");
        Log.d(CURSOR_DEBUG,"Rows " + cursor.getCount() + "   Cols " + cursor.getColumnCount() );

        // Print Column Names
         if(cursor.getColumnCount() > 0) {
            String[] col_names_array = cursor.getColumnNames();
            String col_names = "";
            for (String s : col_names_array)
                col_names += s + ", ";
            Log.d(CURSOR_DEBUG, col_names);
        }
        int row_count = cursor.getCount();
        int col_count = cursor.getColumnCount();
        String row_data;
        cursor.moveToFirst();
        if(row_count > 0) {
            for (int rowIdx = 0; rowIdx < row_count ; rowIdx++) {

                row_data = "";
                for (int col_idx = 0; col_idx < col_count ; col_idx++) {

                    switch (cursor.getType(col_idx)) {

                        case Cursor.FIELD_TYPE_STRING:
                            row_data +=  " ," + cursor.getString(col_idx);
                            break;

                        case Cursor.FIELD_TYPE_INTEGER:
                            row_data +=  " ," + cursor.getInt(col_idx);
                            break;

                        case Cursor.FIELD_TYPE_BLOB:
                            row_data += " ,BLOB";
                            break;

                        case Cursor.FIELD_TYPE_FLOAT:
                            row_data += " ," + cursor.getFloat(col_idx);
                            break;

                        case Cursor.FIELD_TYPE_NULL:
                            row_data += " ,NULL";
                            break;

                        default:
                            row_data += " ,?????";
                            break;
                    }
                }
                Log.d(CURSOR_DEBUG,row_data);
                cursor.moveToNext();
            }
        }
        return cursor.getCount();
    }
}