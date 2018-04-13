package il.co.ratners.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritesDBHelper extends SQLiteOpenHelper {

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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
