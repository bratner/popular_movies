package il.co.ratners.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import il.co.ratners.popularmovies.R;

/**
 * Accessor functions for Shared Preferences go here.
 */

public class PrefUtils {
    public static String TAG = PrefUtils.class.getSimpleName();
    public static final int SORT_BY_POPULARITY = 0;
    public static final int SORT_BY_RATING = 1;

    /* retrieve the sort order string needed to complete the API request URL */
    public static String getSortOrder(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSortOrder = context.getString(R.string.key_sort_by);
        String defaultSortOrder = context.getString(R.string.default_sort_order);
        sp.getString(keyForSortOrder, defaultSortOrder);
        return sp.getString(keyForSortOrder, defaultSortOrder);


    }

    public static boolean setSortOrder(Context context, int sortOrder) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSortOrder = context.getString(R.string.key_sort_by);
        String defaultSortOrder = context.getString(R.string.default_sort_order);
        String sortOrderString;
        switch (sortOrder) {
            case SORT_BY_POPULARITY:
                sortOrderString = context.getString(R.string.popular_sort_order);
                break;
            case SORT_BY_RATING:
                sortOrderString = context.getString(R.string.rating_sort_order);
                break;
            default:
                Log.e(TAG, "Defaulting Sort Order");
                sortOrderString = defaultSortOrder;
        }
        return sp.edit().putString(keyForSortOrder, sortOrderString).commit();

    }
}
