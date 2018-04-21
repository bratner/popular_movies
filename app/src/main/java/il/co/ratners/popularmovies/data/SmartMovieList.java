package il.co.ratners.popularmovies.data;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Vector;

import il.co.ratners.popularmovies.GridActivity;
import il.co.ratners.popularmovies.R;
import il.co.ratners.popularmovies.network.MovieDBApi;
import il.co.ratners.popularmovies.network.MovieDBConnector;
import il.co.ratners.popularmovies.utils.PreferenceUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


/**
 * Holds a cache of Movie objects to simulate infinite scrolling.
 */

public class SmartMovieList implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String SMART_LIST_PAGE_COUNT = "SMART_LIST_PAGE_COUNT";
    public static final String SMART_LIST_MOVIE_COUNT = "SMART_LIST_MOVIE_COUNT";
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
    private final MovieDBConnector mMovieConnector;
    private SparseArray<String> mFavoritesMap;



    private boolean mRestoringState = false;
    private int mRestorePagesToLoad;
    private int mRestoreMoviesCount;


    public SmartMovieList(Context context) {

        mContext = context;
        mMovies = new Vector<>(INITAL_CACHE_PAGES*ITEMS_PER_PAGE, ITEMS_PER_PAGE);
        mMovieConnector = new MovieDBConnector(context);
        mFavoritesMap = new SparseArray<>(INITAL_CACHE_PAGES*ITEMS_PER_PAGE);
    }

    public void refreshFavorites() {
        ((GridActivity)mContext).getSupportLoaderManager().restartLoader(1, null, this);
    }
    public void setUpdateListener(UpdateListener listener) {
        mUpdateListener = listener;
    }

    public int size() {
        int ret = mMovies.size();
        if (mRestoringState) {
            Log.d(TAG, "Restoring state, lying about available movie list size.");
            ret = mRestoreMoviesCount;
        }
        Log.d(TAG, "size() returning "+ret);
        return ret;
    }

    public void reset() {
        if (mRestoringState)
        {
            Log.d(TAG, "Can't reset. Restoring state");
            return;
        }
        Log.d(TAG, "Reseting movie list.");
        lastLoadedPage = -1;
        if (loading)
        {
            if (mPageGetter != null)
                mPageGetter.cancel(true);
            loading = false;
        }
        mMovies.clear();
        mMovies.ensureCapacity(INITAL_CACHE_PAGES*ITEMS_PER_PAGE);
        refreshFavorites();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Creating a CursorLoader for Favorites");
        CursorLoader loader = new CursorLoader(mContext, FavoritesContract.FavoritesEntry.CONTENT_URI,
                null,null,null,null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Favorites loader finished with: "+data.getCount()+" rows.");

        int movieIdIndex = data.getColumnIndex(FavoritesContract.FavoritesEntry.MOVIE_ID);
        int movieJsonIndex = data.getColumnIndex(FavoritesContract.FavoritesEntry.MOVIE_JSON);

        mFavoritesMap.clear();
        while(data.moveToNext())
        {
            int movieId = data.getInt(movieIdIndex);
            String movieJson = data.getString(movieJsonIndex);
            mFavoritesMap.append(movieId, movieJson);
        }

        setMovieFavorites();
    }

    private void setMovieFavorites() {
        for (int idx = 0; idx < mMovies.size(); idx++)
        {
            Movie m = mMovies.get(idx);
            boolean changed = false;

            /* if movie in favorites and not marked as such or not in favorites but is marked*/
            if (mFavoritesMap.get(m.getId()) != null) {
                if (!m.isFavorite()) {
                    m.setFavorite(true);
                    changed = true;
                }
            } else if (m.isFavorite()) {
                m.setFavorite(false);
                changed = true;
            }

            /* let recyclerview know */
            if (changed && !mRestoringState)
                mUpdateListener.OnUpdate(idx, 1);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoritesMap.clear();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SMART_LIST_PAGE_COUNT, lastLoadedPage+1);
        outState.putInt(SMART_LIST_MOVIE_COUNT, mMovies.size());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(SMART_LIST_PAGE_COUNT) ) {
            mRestorePagesToLoad = savedInstanceState.getInt(SMART_LIST_PAGE_COUNT);
            mRestoreMoviesCount = savedInstanceState.getInt(SMART_LIST_MOVIE_COUNT);
            mRestoringState = true;
            Log.d(TAG, "Restoring state with "+mRestorePagesToLoad+" pages, totaling "+mRestoreMoviesCount+" movies");
            loadPage(0);
        }

    }

    public void fetchMoreMovies() {
        getMovie(mMovies.size());
    }


    /* Callback mechanism to update the recyclerview when loading is done */
    public interface UpdateListener {
        void OnUpdate (int startIndex, int count);
    }

    /* Returns a movie object for recyclerview to display or null to attempt a load */
    /* DONE: think of a way to signal actual end of list if it is not infinite(ish)
    *  Solution: actually very simple, size given == actual list size, no nulls */
    public Movie getMovie(int position) {
       Log.d(TAG, "getMovie() position: "+position+" current size "+mMovies.size());
       if (mRestoringState)
           return null;

        if (position < mMovies.size())
            return mMovies.get(position);

        loadPage(lastLoadedPage+1);
        return null;
    }

    private void loadPage(int page) {

        Call<MovieDBApi.MovieDBList> call;

        if(loading)
            return;
        loading = true;
        Log.d(TAG, "loadPage() " + page);
        switch (PreferenceUtils.getGridContentType(mContext)) {
            case PreferenceUtils.POPULAR:
                call = mMovieConnector.popular_movies(page+1, "en_US");
                break;
            case PreferenceUtils.TOP_RATED:
                call = mMovieConnector.top_rated(page+1, "en_US");
                break;
            default:
                call = null;
        }

        if(call == null)
            return;

        call.enqueue(new Callback<MovieDBApi.MovieDBList>() {
            @Override
            public void onResponse(Call<MovieDBApi.MovieDBList> call, Response<MovieDBApi.MovieDBList> response) {

                if(!response.isSuccessful()) {
                    Log.e(TAG, "Failed fetching movie list");
                    Toast.makeText(mContext, R.string.failed_fetching_movies , Toast.LENGTH_LONG).show();
                    return;
                }
                MovieDBApi.MovieDBList movieList = response.body();
                ArrayList<Movie> appendList = new ArrayList<Movie>();
                for( MovieDBApi.MovieDBItem result : movieList.getResults())
                {
                    appendList.add(Movie.movieDBItemToMovie(result));
                }

                if (appendList.size()>0)
                {
                    int prevSize = mMovies.size();
                    addPageToCache(response.body().page-1, appendList);
                    loading = false;
                    if(!mRestoringState)
                        mUpdateListener.OnUpdate(prevSize, appendList.size());
                    else
                        mRestorePagesToLoad--;
                }
                loading = false;
                if (mRestoringState && mRestorePagesToLoad == 0) {
                    finishedSateRestore();
                }
                if(mRestoringState && mRestorePagesToLoad > 0)
                    loadPage(lastLoadedPage+1);
            }

            @Override
            public void onFailure(Call<MovieDBApi.MovieDBList> call, Throwable t) {
                Log.d(TAG, "Failed fetching a page");
            }
        });
    }

    private void finishedSateRestore() {
        Log.d(TAG, "Finished restoring SmartMovies to its previous state. Last page:"+lastLoadedPage+" total movies: "+mMovies.size());
        mRestoringState = false;
        mUpdateListener.OnUpdate(0, mMovies.size());

    }

    private synchronized void addPageToCache(int mPageNumber, ArrayList<Movie> movies) {
            if (mPageNumber <= lastLoadedPage)
                return;
            /* addAll() is a syncronized method and size() is grown after copy is complete */
            mMovies.addAll(mMovies.size(), movies);
            setMovieFavorites();
            lastLoadedPage = mPageNumber;
            Log.d(TAG, "addPageToCache() Done with page "+mPageNumber);
    }

    public boolean isRestoringState() {
        return mRestoringState;
    }

}
