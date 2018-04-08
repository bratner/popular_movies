package il.co.ratners.popularmovies.data;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import il.co.ratners.popularmovies.network.MovieDBApi;
import il.co.ratners.popularmovies.utils.PreferenceUtils;
import il.co.ratners.popularmovies.utils.TheMovieDB;
import retrofit2.Call;
import retrofit2.Retrofit;


/**
 * Holds a cache of Movie objects to simulate infinite scrolling.
 */

public class SmartMovieList {

    private static String TAG = SmartMovieList.class.getSimpleName();
    /* Themoviedb returns 20 results per page. Assuming this as true for now */
    private final int INITAL_CACHE_PAGES = 3;
    private final int ITEMS_PER_PAGE = 20;

    private Vector<Movie> mMovies;

    private Context mContext;
    private UpdateListener mUpdateListener;
    private AsyncTask<Integer, Void, ArrayList<Movie>> mPageGetter;
    private Retrofit mRetrofit;


    private int lastLoadedPage = -1;
    private boolean loading = false;

    public SmartMovieList(Context context) {
        mContext = context;
        mMovies = new Vector<>(INITAL_CACHE_PAGES*ITEMS_PER_PAGE, ITEMS_PER_PAGE);
    }

    public void setUpdateListener(UpdateListener listener) {
        mUpdateListener = listener;
    }

    public int size() {
        //Log.d(TAG, "size() returning "+mMovies.size());
        return mMovies.size();
    }

    public void reset() {
        lastLoadedPage = -1;
        if(loading)
        {
            mPageGetter.cancel(true);
            loading = false;
        }
        mMovies.clear();
        mMovies.ensureCapacity(INITAL_CACHE_PAGES*ITEMS_PER_PAGE);
    }

    /* Callback mechanism to update the recyclerview when loading is done */
    public abstract static class UpdateListener {
        public abstract void OnUpdate (int startIndex, int count);
    }

    /* Returns a movie object for recyclerview to display or null to attempt a load */
    /* DONE: think of a way to signal actual end of list if it is not infinite(ish)
    *  Solution: actually very simple, size given == actual list size, no nulls */
    public Movie getMovie(int position) {
     /*   Log.d(TAG, "getMovie() position: "+position);*/
        /* is size atomic? */
        if (position < mMovies.size())
            return mMovies.get(position);

        loadPage(lastLoadedPage+1);
        return null;
//
//        if (!loading) {
//            if (position >= mMovies.size()) {
//                loadPage(lastLoadedPage + 1);
//                return null;
//            }
//            return mMovies.get(position);
//        } else {
//            if (position < loadingAt )
//                return mMovies.get(position);
//            return null;
//        }
    }

    /* TODO: why do we assume this can't this be called in parallel with the same page num? */
    private void loadPage(int page) {
        if(loading)
            return;
        loading = true;
        Log.d(TAG, "loadPage() " + page);
        //Call<MovieDBApi.MovieDBList> mPageCall
        mPageGetter = new PageGetterTask().execute(page);
    }


    private synchronized void addPageToCache(int mPageNumber, ArrayList<Movie> movies) {
            if (mPageNumber <= lastLoadedPage)
                return;
            /* addAll() is a syncronized method and size() is grown after copy is complete */
            mMovies.addAll(mMovies.size(), movies);
            lastLoadedPage = mPageNumber;
            Log.d(TAG, "addPageToCache() Done with page "+mPageNumber);

    }

    class PageGetterTask extends AsyncTask<Integer, Void, ArrayList<Movie>>
    {
        final String TAG = PageGetterTask.class.getSimpleName();
        private int mPageNumber;

        @Override
        protected ArrayList<Movie> doInBackground(Integer... in) {
            mPageNumber = in[0];
            try {
                ArrayList<Movie> lMovies;

                Uri uri = Uri.parse("https://api.themoviedb.org/3").buildUpon()
                        .appendPath("movie")
                        .appendPath(PreferenceUtils.getSortOrder(mContext))
                        .appendQueryParameter("api_key", TheMovieDB.API_KEY)
                        .appendQueryParameter("language", "en-US")
                        .appendQueryParameter("page", ""+(mPageNumber+1)).build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "URL is "+uri.toString());
                String json_input = TheMovieDB.getResponseFromHttpUrl(url);
                JSONObject response = new JSONObject(json_input);
                JSONArray jsonMovies = response.getJSONArray("results");
                lMovies = new ArrayList<>();
                for (int i = 0; i < jsonMovies.length(); ++i)
                {
                    if(isCancelled())
                        return null;
                    Movie m = Movie.parseJsonToMovie(jsonMovies.getJSONObject(i));
                    if (m != null) {
                        lMovies.add(m);
                        Log.d(TAG, "Adding "+m.getTitle());
                    }
                }

                return lMovies;

            } catch (MalformedURLException uex) {
                Log.e(TAG, "Malformed URL: "+uex);
            } catch (IOException ioex) {
                Log.e(TAG, "Networking problem: "+ioex);
            } catch (JSONException jex) {
                Log.e(TAG, "Data error: "+jex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);
            if (movies == null) {
                Log.e(TAG, "Unable to retrieve remote data.");
                return;
            }
            Log.d(TAG, "onPostExecut() for page "+mPageNumber);
            int prevSize = mMovies.size();
            addPageToCache(mPageNumber, movies);
            loading = false;
            mUpdateListener.OnUpdate(prevSize, movies.size());
        }
    }
}
