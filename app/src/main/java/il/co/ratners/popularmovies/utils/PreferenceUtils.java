package il.co.ratners.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Accessor functions for Shared Preferences go here.
 */

public class PreferenceUtils {
    private static String TAG = PreferenceUtils.class.getSimpleName();

    public static final String POPULAR = "popular";
    public static final String TOP_RATED = "top_rated";
    public static final String FAVORITES = "top_rated";
    public static final String DEFAULT_GRID = POPULAR;

    public enum GRID_CONTENT {
        POPULARITY, RATING, FAVORITES
    }

    private static final String GRID_TYPE_KEY = "grid_type";



    /* retrieve the sort order stri ng needed to complete the API request URL */
    public static String getSortOrder(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSortOrder = PreferenceUtils.GRID_TYPE_KEY;
        String defaultSortOrder = DEFAULT_GRID;
        sp.getString(keyForSortOrder, defaultSortOrder);
        return sp.getString(keyForSortOrder, defaultSortOrder);

    }

    public static boolean setSortOrder(Context context, GRID_CONTENT sortOrder) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSortOrder = PreferenceUtils.GRID_TYPE_KEY;
        String defaultSortOrder = DEFAULT_GRID;
        String sortOrderString;
        switch (sortOrder) {
            case POPULARITY:
                sortOrderString = POPULAR;
                break;
            case RATING:
                sortOrderString = TOP_RATED;
                break;
            case FAVORITES:
                sortOrderString = FAVORITES;
            default:
                Log.e(TAG, "Defaulting Sort Order");
                sortOrderString = defaultSortOrder;
        }
        return sp.edit().putString(keyForSortOrder, sortOrderString).commit();
    }
}
