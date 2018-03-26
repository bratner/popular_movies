package il.co.ratners.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import il.co.ratners.popularmovies.R;

/**
 * Accessor functions for Shared Preferences go here.
 */

public class PreferenceUtils {
    private static String TAG = PreferenceUtils.class.getSimpleName();


    public static final int SORT_BY_POPULARITY = 0;
    public static final int SORT_BY_RATING = 1;

    public enum SORT_ORDER {
        POPULARITY, RATING
    }

    private static final String SORT_ORDER_KEY = "sort_order";



    /* retrieve the sort order stri ng needed to complete the API request URL */
    public static String getSortOrder(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSortOrder = PreferenceUtils.SORT_ORDER_KEY;
        String defaultSortOrder = TheMovieDB.DEFAULT_SORT_ORDER;
        sp.getString(keyForSortOrder, defaultSortOrder);
        return sp.getString(keyForSortOrder, defaultSortOrder);

    }

    public static boolean setSortOrder(Context context, SORT_ORDER sortOrder) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSortOrder = PreferenceUtils.SORT_ORDER_KEY;
        String defaultSortOrder = TheMovieDB.DEFAULT_SORT_ORDER;
        String sortOrderString;
        switch (sortOrder) {
            case POPULARITY:
                sortOrderString = TheMovieDB.SORT_BY_POPULARITY;
                break;
            case RATING:
                sortOrderString = TheMovieDB.SORT_BY_RATING;
                break;
            default:
                Log.e(TAG, "Defaulting Sort Order");
                sortOrderString = defaultSortOrder;
        }
        return sp.edit().putString(keyForSortOrder, sortOrderString).commit();
    }
}
