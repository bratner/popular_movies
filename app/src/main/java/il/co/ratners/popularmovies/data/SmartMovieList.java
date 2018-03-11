package il.co.ratners.popularmovies.data;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.Collections;
import java.util.Scanner;


/**
 * Holds a cache of Movie objects to simulate infinite scrolling.
 */

public class SmartMovieList {

    public static String TAG = SmartMovieList.class.getSimpleName();
    /* Themoviedb returns 20 results per page. Assuming this as true for now */
    private final int RESULTS_PER_PAGE = 20;
    private final int CACHE_PAGES = 5;
    private final int CACHE_SIZE = CACHE_PAGES*RESULTS_PER_PAGE;

    private ArrayList<Movie> mMovies;
    private ArrayList<Integer> mPositionToPage;
    private Context mContext;
    private UpdateListener mUpdateListener;
    private ArrayList<Integer> mPageOrder;

    /* Cache management */
    private int mStartPosition = -1;
    private int mEndPosition = -1;
    private int mStartPage = -1;
    private int mEndPage = -1;
    private int mItemsCount = 0;
    private int mInsertIndex = -1;
    private int mAppendIndex = -1;


    public SmartMovieList(Context context) {
        mContext = context;
        mMovies = new ArrayList<Movie>(CACHE_SIZE);

        mPositionToPage = new ArrayList<>(CACHE_SIZE);

        /* No add should be used on mMovies from this point */
        for (int i = 0; i < CACHE_SIZE ; i++) {
            mMovies.add(null);
        }

    }

    public void setUpdateListener(UpdateListener listener) {
        mUpdateListener = listener;
    }

    public int getEndPosition() {
        return mEndPosition;
    }

    /* Callback mechanism to update the recyclerview when loading is done */
    public abstract static class UpdateListener {
        public abstract void OnUpdate (ArrayList<Integer> updated_positions);
    }

    /* Returns a movie object for recyclerview to display or null to attempt a load */
    /* TODO: think of a way to signal actual end of list if it is not infinite(ish) */
    public Movie getMovie(int position) {
        Movie ret;
        Log.d(TAG, "Requested position: "+position);

        /* Wait! But what if we are updating the movie list right now!?
         * No panic! Main datastructure modifications are done on the main thread.
         * RecyclerView calls come on the same thread. So no collision should be possible.
         * Something about Handlers i think. Will investigate later.
         */

        if (isPositionCached(position)) {
            ret = mMovies.get(positionToIndex(position));
        } else {
            loadPage(position % RESULTS_PER_PAGE);
            ret = null;
        }
        return ret;
    }

    private void loadPage(int page) {
        new PageGetterTask().execute(page);
    }

    private boolean isPositionCached(int position) {
        if (position >= mStartPosition && position <= mEndPosition)
            return true;
        return false;
    }

    private int getCacheSize() {
        return CACHE_SIZE;
    }

    private int positionToIndex(int position) {
        if (position < 0)
            return 0;
        return (mStartPosition + position) % CACHE_SIZE;
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
                //URL url = new URL("https://api.themoviedb.org/3/discover/movie?api_key=1ba61ad61368b70c6437f62af9bd3345&language=en-US&sort_by=popularity.desc&include_adult=false&include_video=false&page=1");
                //URL url = new URL("https://api.themoviedb.org/3/movie/popular?api_key=1ba61ad61368b70c6437f62af9bd3345&language=en-US&page=1");
                Uri uri = Uri.parse("https://api.themoviedb.org/3").buildUpon()
                        .appendPath("movie")
                        .appendPath("popular")
                        .appendQueryParameter("api_key", "1ba61ad61368b70c6437f62af9bd3345")
                        .appendQueryParameter("language", "en-US")
                        .appendQueryParameter("page", ""+mPageNumber+1).build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "URL is "+uri.toString());
                String json_input = getResponseFromHttpUrl(url);
                JSONObject response = new JSONObject(json_input);
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
            addPageToCache(mPageNumber, movies);
            ArrayList<Integer> positionsList = new ArrayList<>(movies.size());
            for (int i = 0; i < movies.size(); i++) {
                positionsList.add(mPageNumber*RESULTS_PER_PAGE+i);
            }
            SmartMovieList.this.mUpdateListener.OnUpdate(positionsList);
        }
        /**
         * This method returns the entire result from the HTTP response.
         *
         * @param url The URL to fetch the HTTP response from.
         * @return The contents of the HTTP response, null if no response
         * @throws IOException Related to network and stream reading
         */
        private String getResponseFromHttpUrl(URL url) throws IOException {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = urlConnection.getInputStream();

                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                String response = null;
                if (hasInput) {
                    response = scanner.next();
                }
                scanner.close();
                return response;
            } finally {
                urlConnection.disconnect();
            }
        }
    }

    private void addPageToCache(int mPageNumber, ArrayList<Movie> movies) {

        int startIndex = 0;
        /* first page handling */
        if (mItemsCount == 0) {
            mStartPage = mPageNumber;
            mEndPage = mPageNumber;
            mInsertIndex = 0;
            mAppendIndex = RESULTS_PER_PAGE;
            mItemsCount += RESULTS_PER_PAGE; //movies.size();

            for (int i = 0; i < RESULTS_PER_PAGE; i++) {
                Movie m = (i < movies.size()) ? movies.get(i) : null;
                mMovies.set(i, m);
            }
            return;
        }

        if (mPageNumber == mStartPage-1)
        {
            /* Overwrites the highest page num data */
            /* Updates mStartPage and mStartPosition */
            startIndex = positionToIndex(mEndPage*RESULTS_PER_PAGE);
            mEndPage--;
            mEndPosition = (mEndPage+1)*RESULTS_PER_PAGE-1;
        }

        if (mPageNumber == mEndPage+1) {
            /* Overwrites the lowest page num data */
            /* Updates mEndPage and mEndPosition */
            startIndex = positionToIndex(mStartPage*RESULTS_PER_PAGE);
            mStartPage++;
            mStartPosition = mStartPage*RESULTS_PER_PAGE;
            if (mEndPage == -1)
            {
                mEndPage = mStartPage;
            }

        }
        for (int i = 0; i < movies.size(); i++) {
            mMovies.set(startIndex+i, movies.get(i));
        }

    }


}
