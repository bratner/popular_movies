package il.co.ratners.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;


public class FavoritesRequest extends AsyncTask<Void, Void, Object> {

    /* Make it possible to notify when tasks are finished */
    public interface FavoritesResponseListener {
        public abstract void onMovieRemoved(boolean really);
        public abstract void onMovieAdded(boolean really);
        public abstract void onMovieFound(boolean found);
    }

    public static final int DELETE_ACTION = 1;
    public static final int FIND_ACTION = 2;
    public static final int ADD_ACTION = 3;

    private Context mContext;
    private int mAction;
    int mMovieId;
    String mData;
    FavoritesResponseListener mCallback;


    public FavoritesRequest(Context context, int action, int movieId, String data, FavoritesResponseListener callback) {
        super();
        mContext = context;
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
                int deleted = mContext.getContentResolver().delete(
                        movieUri,null, null);
                ret = new Integer(deleted);
                break;
            case ADD_ACTION:
                ContentValues vals = new ContentValues();
                vals.put(FavoritesContract.FavoritesEntry.MOVIE_JSON, mData);
                Uri added = mContext.getContentResolver().insert(movieUri, vals);
                ret = added;
                break;
            case FIND_ACTION:
                ret = mContext.getContentResolver().query(
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
                mCallback.onMovieAdded((Uri)result != null);
                break;
            case FIND_ACTION:
                mCallback.onMovieFound(((Cursor)result).getCount() > 0);
                break;
        }
    }
}

