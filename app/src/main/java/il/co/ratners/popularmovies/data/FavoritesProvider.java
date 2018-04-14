package il.co.ratners.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class FavoritesProvider extends ContentProvider {

    public static final String TAG = FavoritesProvider.class.getSimpleName();
    public static final int CODE_MOVIE = 100;

    private final UriMatcher mUriMatcher = buildUriMatcher();
    private final FavoritesDBHelper mDB = new FavoritesDBHelper(getContext());

    private UriMatcher buildUriMatcher()
    {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(FavoritesContract.CONTENT_AUTHORITY,
                FavoritesContract.PATH_FAVORITES + "/#", CODE_MOVIE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int matchResult = mUriMatcher.match(uri);

        switch (matchResult) {
            case CODE_MOVIE:
                break;
            default:
                Log.e(TAG, "Unsupported URI: "+uri);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
