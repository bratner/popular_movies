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
import il.co.ratners.popularmovies.network.MovieDBApi;
import il.co.ratners.popularmovies.network.MovieDBConnector;
import il.co.ratners.popularmovies.utils.PreferenceUtils;
import il.co.ratners.popularmovies.utils.TheMovieDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


/**
 * Holds a cache of Movie objects to simulate infinite scrolling.
 */

public class SmartMovieList implements LoaderManager.LoaderCallbacks<Cursor> {

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


    public SmartMovieList(Context context) {
        mContext = context;
        mMovies = new Vector<>(INITAL_CACHE_PAGES*ITEMS_PER_PAGE, ITEMS_PER_PAGE);
        mMovieConnector = new MovieDBConnector(context);
        mFavoritesMap = new SparseArray<>(INITAL_CACHE_PAGES*ITEMS_PER_PAGE);
        ((GridActivity)context).getSupportLoaderManager().initLoader(1, null, this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(mContext, FavoritesContract.FavoritesEntry.CONTENT_URI,
                null,null,null,null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int movieIdIndex = data.getColumnIndex(FavoritesContract.FavoritesEntry.MOVIE_ID);
        int movieJsonIndex = data.getColumnIndex(FavoritesContract.FavoritesEntry.MOVIE_JSON);
        while(data.moveToNext())
        {
            int movieId = data.getInt(movieIdIndex);
            String movieJson = data.getString(movieJsonIndex);
            mFavoritesMap.append(movieId, movieJson);
        }
        setMovieFavorites();
    }

    private void setMovieFavorites() {
        for (Movie m: mMovies)
        {
          if (mFavoritesMap.get(m.getId()) != null)
              m.setFavorite(true);
          else
              m.setFavorite(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoritesMap.clear();
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

        switch (PreferenceUtils.getSortOrder(mContext)) {
            case TheMovieDB.SORT_BY_POPULARITY:
                call = mMovieConnector.popular_movies(page+1, "en_US");
                break;
            case TheMovieDB.SORT_BY_RATING:
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
                    Toast.makeText(mContext,"Unable to fetch movie list" , Toast.LENGTH_LONG).show();
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
                    mUpdateListener.OnUpdate(prevSize, appendList.size());
                }
                loading = false;
            }

            @Override
            public void onFailure(Call<MovieDBApi.MovieDBList> call, Throwable t) {
                Log.d(TAG, "Failed fetching a page");
            }
        });

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

}
