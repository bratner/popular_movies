package il.co.ratners.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class FavoritesProvider extends ContentProvider {

    public static final String TAG = FavoritesProvider.class.getSimpleName();
    public static final int CODE_MOVIE = 100;
    public static final int CODE_MOVIE_LIST = 101;

    private final UriMatcher mUriMatcher = buildUriMatcher();
    private FavoritesDBHelper mDB;

    private UriMatcher buildUriMatcher()
    {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(FavoritesContract.CONTENT_AUTHORITY,
                FavoritesContract.PATH_FAVORITES + "/#", CODE_MOVIE);
        matcher.addURI(FavoritesContract.CONTENT_AUTHORITY,
                FavoritesContract.PATH_FAVORITES, CODE_MOVIE_LIST);
        return matcher;
    }

    @Override
    public boolean onCreate() {

        mDB = new FavoritesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        int matchResult = mUriMatcher.match(uri);
        final SQLiteDatabase  db = mDB.getReadableDatabase();
        Cursor ret = null;

        switch (matchResult) {
            case CODE_MOVIE:
                ret = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        null,
                        FavoritesContract.FavoritesEntry.MOVIE_ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case CODE_MOVIE_LIST:
                ret = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                        );
                break;

        }
        return ret;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        int uriMatch = mUriMatcher.match(uri);
        if (uriMatch != CODE_MOVIE)
            return null;

        final SQLiteDatabase  db = mDB.getWritableDatabase();

        long ret = db.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, values);
        if (ret == -1)
            return null;

        getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(FavoritesContract.FavoritesEntry.CONTENT_URI, null);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriMatch = mUriMatcher.match(uri);
        if (uriMatch != CODE_MOVIE)
            return 0;

        final SQLiteDatabase  db = mDB.getWritableDatabase();
        int ret = db.delete(FavoritesContract.FavoritesEntry.TABLE_NAME,
                FavoritesContract.FavoritesEntry.MOVIE_ID + "=?",
                new String[]{uri.getLastPathSegment()});
        getContext().getContentResolver().notifyChange(uri, null);
        getContext().getContentResolver().notifyChange(FavoritesContract.FavoritesEntry.CONTENT_URI, null);
        return ret;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
