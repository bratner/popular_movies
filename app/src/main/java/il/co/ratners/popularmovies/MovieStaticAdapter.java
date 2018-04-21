package il.co.ratners.popularmovies;

import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import il.co.ratners.popularmovies.data.FavoritesContract;
import il.co.ratners.popularmovies.data.Movie;
import il.co.ratners.popularmovies.network.MovieDBApi;

/**
 * Created by bratner on 4/20/18.
 */

public class MovieStaticAdapter extends RecyclerView.Adapter<MovieStaticAdapter.MovieViewHolder>
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = MovieStaticAdapter.class.getSimpleName();
    private static final int STATIC_LOADER_ID = 321;

    private Cursor mCursor;
    private Context mContext;


    public MovieStaticAdapter(Context context) {
        mContext = context;
        ((GridActivity)mContext).getSupportLoaderManager().initLoader(STATIC_LOADER_ID, null, this);
    }


    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     * Taken from Exercise 9 of Sunshine
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v;
        LayoutInflater inflater = LayoutInflater.from(context);

        v = inflater.inflate(R.layout.movie_grid_item_layout, parent, false);

        return new MovieStaticAdapter.MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
            /* Nothing to set if the are not showing a movie */
        Context context = holder.mContext;
        Log.d(TAG, "Binding position " + position);
        if (holder.getItemViewType() != 0)
            return;
        Movie m = getMovie(position);
        if (m == null) {
            Log.d(TAG, "No movie was found for position " + position);
            return;
        }
        holder.mGridItemTextView.setText(m.getTitle());

        if (m.isFavorite())
            holder.mFavoriteImageView.setVisibility(View.VISIBLE);
        else
            holder.mFavoriteImageView.setVisibility(View.INVISIBLE);

        String url = MovieDBApi.getMovieImageURL(m.getPoster_path());
        Log.d(TAG, url);
        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.poster_placeholder)
                .into(holder.mMoviePosterImageView);
    }

    private Movie getMovie(int position) {
        Movie ret;


        mCursor.moveToPosition(position);
        int jsonIndex = mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.MOVIE_JSON);

        mCursor.moveToPosition(position);
        String jsonString = mCursor.getString(jsonIndex);
        ret = Movie.fromJson(jsonString);
        ret.setFavorite(true);

        return ret;
    }


    @Override
    public int getItemCount() {
        if (mCursor == null)
            return 0;
        return mCursor.getCount();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == STATIC_LOADER_ID) {
            Log.d(TAG, "Creating a CursorLoader for Favorites");
            CursorLoader loader = new CursorLoader(mContext, FavoritesContract.FavoritesEntry.CONTENT_URI,
                    null, null, null, null);
            return loader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == STATIC_LOADER_ID ) {
            Log.d(TAG, "Favorites loader finished.");
            swapCursor(data);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == STATIC_LOADER_ID)
        {
            Log.d(TAG, "Favorites loader is being reset.");
            swapCursor(null);
        }
    }

    public void handleResume() {
        Log.d(TAG, "Resuming. Restarting loader");
        ((GridActivity)mContext).getSupportLoaderManager().restartLoader(STATIC_LOADER_ID, null, this);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView mGridItemTextView;
        ImageView mMoviePosterImageView;
        ImageView mFavoriteImageView;
        Context mContext;

        public final String TAG = MovieViewHolder.class.getCanonicalName();

        public MovieViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mGridItemTextView = itemView.findViewById(R.id.tv_movie_title);
            mMoviePosterImageView = itemView.findViewById(R.id.iv_movie_poster);
            mFavoriteImageView = itemView.findViewById(R.id.iv_favorite);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Movie m = getMovie(position);

            Log.d(TAG, "Clicked on position " + position + " - " + m.getTitle());
            Intent i = new Intent(mContext, MovieDetailsActivity.class);
            //String url = MovieDBApi.getMovieImageURL(m.getPoster_path());
            i.putExtra(Movie.KEY_MOVIE, m);
            mContext.startActivity(i);
        }
    }
}
