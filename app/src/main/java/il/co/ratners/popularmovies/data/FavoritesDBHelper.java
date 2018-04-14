package il.co.ratners.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import il.co.ratners.popularmovies.data.FavoritesContract.FavoritesEntry;

public class FavoritesDBHelper extends SQLiteOpenHelper {

    public static final String TAG = FavoritesDBHelper.class.getSimpleName();

    public static final String DatabaseName = "favorites.db";

    private static final int DATABASE_VERSION = 1;

    /* TODO: Change this to whatever we actually need */
    public FavoritesDBHelper(Context context,
                             String name,
                             SQLiteDatabase.CursorFactory factory,
                             int version)
    {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createQuery = "CREATE TABLE "+FavoritesEntry.TABLE_NAME+"("+
                FavoritesEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                FavoritesEntry.MOVIE_ID+" INTEGER NOT NULL, "+
                FavoritesEntry.MOVIE_JSON+" TEXT NOT NULL, "+
                "UNIQUE (" + FavoritesEntry.MOVIE_ID + ") ON CONFLICT REPLACE);";
        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrade by, Deleting the Table and re-creating it.");
        final String dropQuery = "DROP TABLE IF EXISTS "+FavoritesEntry.TABLE_NAME + ";";
        db.execSQL(dropQuery);
        onCreate(db);
    }

}
