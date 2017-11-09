package com.anantya.watchsensor.db;

/**
 * Created by bill on 10/6/17.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.anantya.watchsensor.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.regex.Pattern;


/**
 * Created by bill on 10/2/17.
 */

public class EventDataCacheHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "event_data_cache.db";
    public static final int DATABASE_VERSION = 1;
    public static final long CACHE_TIMOUT_SECONDS = 60 * 60 * 1000;

    private String mCreateScript;
    private String mDeleteScript;

    public EventDataCacheHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mCreateScript = context.getResources().getString(R.string.event_data_cache_db_create);
        mDeleteScript = context.getResources().getString(R.string.event_data_cache_db_delete);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        processScript(db, mCreateScript);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        processScript(db, mDeleteScript);
        onCreate(db);
    }


    protected String processScript(SQLiteDatabase db, String script) {
        String errorMessage = "";
        BufferedReader reader = new BufferedReader(new StringReader(script));
        String line;
        try {
            String sql = "";
            while ( (line = reader.readLine()) != null ) {
                if (Pattern.matches("^\\s*#.*", line) || line.length() == 0) {
                    continue;
                }
                sql = sql + line;
                if ( Pattern.matches(".*;\\s*$", line)) {
                    db.execSQL(sql);
                    sql = "";
                }
            }
        } catch (IOException e) {
            errorMessage = "unable to read script";
        }
        return errorMessage;
    }

// static helper methods

    public static void updateCacheTableTime(SQLiteDatabase db, String name) {
        CacheStateModel cacheStateModel = new CacheStateModel(db);
        Date now = new Date();
        cacheStateModel.update(name, now);
    }

    public static boolean isCacheTableDirty(SQLiteDatabase db, String name) {
        boolean result = true;
        CacheStateModel cacheStateModel = new CacheStateModel(db);
        Date date = cacheStateModel.getUpdateTime(name);
        if ( date != null) {
            Date now = new Date();
            long fromTime = now.getTime(), toTime = date.getTime();
            if ( (now.getTime() - date.getTime()) < CACHE_TIMOUT_SECONDS ) {
                result = false;
            }
        }
        return result;
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}