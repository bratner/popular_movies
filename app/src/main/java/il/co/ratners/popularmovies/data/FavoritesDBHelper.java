package il.co.ratners.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FavoritesDBHelper extends SQLiteOpenHelper {

    public static String TAG = FavoritesDBHelper.class.getSimpleName();
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
        final String createQuery = "";
        db.execSQL(createQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Deleting the Database.");
        final String dropQuery = "DROP TABLE "+FavoritesContract.FavoritesEntry.TABLE_NAME + ";";
    }

}
