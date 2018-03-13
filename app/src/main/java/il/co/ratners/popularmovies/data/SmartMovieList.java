package il.co.ratners.popularmovies.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import il.co.ratners.popularmovies.utils.TheMovieDB;


/**
 * Holds a cache of Movie objects to simulate infinite scrolling.
 */

public class SmartMovieList {

    public static String TAG = SmartMovieList.class.getSimpleName();
    /* Themoviedb returns 20 results per page. Assuming this as true for now */
    private final int INITAL_CACHE_PAGES = 3;
    private final int ITEMS_PER_PAGE = 20;

    private Vector<Movie> mMovies;

    private Context mContext;
    private UpdateListener mUpdateListener;

    int lastLoadedPage = -1;
    int mTotalMovies = -1;
    boolean loading = false;
    int loadingAt = -1;

    public SmartMovieList(Context context) {
        mContext = context;
        mMovies = new Vector<Movie>(INITAL_CACHE_PAGES*ITEMS_PER_PAGE, ITEMS_PER_PAGE);
    }

    public void setUpdateListener(UpdateListener listener) {
        mUpdateListener = listener;
    }

    public int size() {
        //Log.d(TAG, "size() returning "+mMovies.size());
        return mMovies.size();
    }

    /* Callback mechanism to update the recyclerview when loading is done */
    public abstract static class UpdateListener {
        public abstract void OnUpdate (int startIndex, int count);
    }

    /* Returns a movie object for recyclerview to display or null to attempt a load */
    /* TODO: think of a way to signal actual end of list if it is not infinite(ish) */
    public Movie getMovie(int position) {
     /*   Log.d(TAG, "getMovie() position: "+position);*/
        if (!loading) {
            if (position >= mMovies.size()) {
                loadPage(lastLoadedPage + 1);
                return null;
            }
            return mMovies.get(position);
        } else {
            if (position < loadingAt )
                return mMovies.get(position);
            return null;
        }
    }

    private void loadPage(int page) {
        if(loading)
            return;
        loading = true;
        loadingAt = mMovies.size();
        Log.d(TAG, "loadPage() " + page);
        new PageGetterTask().execute(page);
    }


    private void addPageToCache(int mPageNumber, ArrayList<Movie> movies) {
            mMovies.addAll(mPageNumber*20, movies);
            lastLoadedPage = mPageNumber;
            Log.d(TAG, "addPageToCache() Done with page "+mPageNumber);

    }

    class PageGetterTask extends AsyncTask<Integer, Void, ArrayList<Movie>>
    {
        final String TAG = PageGetterTask.class.getSimpleName();
        private int mPageNumber;
        private int mTotalItems;

        @Override
        protected ArrayList<Movie> doInBackground(Integer... in) {
            mPageNumber = in[0];
            try {
                ArrayList<Movie> lMovies;

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                Uri uri = Uri.parse("https://api.themoviedb.org/3").buildUpon()
                        .appendPath("movie")
                        .appendPath(sp.getString("sort_by","popular"))
                        .appendQueryParameter("api_key", "1ba61ad61368b70c6437f62af9bd3345")
                        .appendQueryParameter("language", "en-US")
                        .appendQueryParameter("page", ""+(mPageNumber+1)).build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "URL is "+uri.toString());
                String json_input = TheMovieDB.getResponseFromHttpUrl(url);
                JSONObject response = new JSONObject(json_input);
                mTotalItems = response.optInt("total_results",0);
                JSONArray jsonMovies = response.getJSONArray("results");
                lMovies = new ArrayList<>();
                for(int i = 0; i < jsonMovies.length(); ++i)
                {
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
            addPageToCache(mPageNumber, movies);
            loading = false;
            SmartMovieList.this.mUpdateListener.OnUpdate(loadingAt, movies.size());
        }
    }
}
