package com.realdolmen.timeregistration.service.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import static com.realdolmen.timeregistration.service.content.DataProvider.Codes.*;

/**
 * Provider for the application's backend data. The following Uri patterns are defined:
 * <ul>
 * <li><b></b>occupations/registered/#</b> where # is a timestamp.</li>
 * </ul>
 */
public class DataProvider extends ContentProvider {

    public interface Codes {
        int ROOT = 1, REGISTERED_OCCUPATIONS = 2, AVAILABLE_OCCUPATIONS = 3;
    }

    private static final UriMatcher mUriMatcher = new UriMatcher(ROOT);

    @Override
    public boolean onCreate() {
        mUriMatcher.addURI("com.realdolmen.timeregistration.provider.occupations.registered", "occupations/registered/#", REGISTERED_OCCUPATIONS);
        mUriMatcher.addURI("com.realdolmen.timeregistration.provider.occupations.available", "occupations/available", AVAILABLE_OCCUPATIONS);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (mUriMatcher.match(uri)) {
            case REGISTERED_OCCUPATIONS:

                return null;

            case ROOT:
            default:
                //Do nothing
                return null;
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case REGISTERED_OCCUPATIONS:
            case AVAILABLE_OCCUPATIONS:
                return "application/json";
            case ROOT:
            default:
                return "text/plain";
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
