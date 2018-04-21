package il.co.ratners.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;


public class FavoritesRequest extends AsyncTask<Void, Void, Object> {

    /* Make it possible to notify when tasks are finished */
    public interface FavoritesResponseListener {
        void onMovieRemoved(boolean really);
        void onMovieAdded(boolean really);
        void onMovieFound(boolean found);
    }

    public static final int DELETE_ACTION = 1;
    public static final int FIND_ACTION = 2;
    public static final int ADD_ACTION = 3;

    //private Context mContext;
    ContentResolver mResolver;
    private int mAction;
    int mMovieId;
    String mData;
    FavoritesResponseListener mCallback;

    public FavoritesRequest(Context context, int action, int movieId, FavoritesResponseListener callback) {
        super();
        mResolver = context.getContentResolver();
       // mContext = context;
        mAction = action;
        mMovieId = movieId;
        mCallback = callback;
    }
    public FavoritesRequest(Context context, int action, int movieId, FavoritesResponseListener callback, String data) {
        super();
        mResolver = context.getContentResolver();
        //mContext = context;
        mAction = action;
        mMovieId = movieId;
        mData = data;
        mCallback = callback;
    }

    @Override
    protected Object doInBackground(Void... voids) {

        Object ret = null;

        Uri movieUri = FavoritesContract.FavoritesEntry.CONTENT_URI.buildUpon().appendPath(""+mMovieId).build();
        switch (mAction) {
            case DELETE_ACTION:
                int deleted = mResolver.delete(
                        movieUri,null, null);
                ret = new Integer(deleted);
                break;
            case ADD_ACTION:
                ContentValues vals = new ContentValues();
                vals.put(FavoritesContract.FavoritesEntry.MOVIE_ID, ""+mMovieId);
                vals.put(FavoritesContract.FavoritesEntry.MOVIE_JSON, mData);
                Uri added = mResolver.insert(movieUri, vals);
                ret = added;
                break;
            case FIND_ACTION:
                ret = mResolver.query(
                        movieUri,
                        null,
                        null,
                        null,
                        null);
                break;
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        if (mCallback == null)
            return;
        switch (mAction) {
            case DELETE_ACTION:
                mCallback.onMovieRemoved((Integer)result > 0);
                break;
            case ADD_ACTION:
                mCallback.onMovieAdded(result != null);
                break;
            case FIND_ACTION:
                mCallback.onMovieFound(((Cursor)result).getCount() > 0);
                break;
        }
    }
}

