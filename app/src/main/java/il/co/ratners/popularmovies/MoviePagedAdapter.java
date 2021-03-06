package il.co.ratners.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Map;

import il.co.ratners.popularmovies.data.Movie;
import il.co.ratners.popularmovies.data.SmartMovieList;
import il.co.ratners.popularmovies.network.MovieDBApi;

/**
 * MoviePagedAdapter - serves as a glue between RecylcerView requests and SmartMovieList
 * which manages web request and keeps the actual movie data.
 */

class MoviePagedAdapter extends RecyclerView.Adapter<MoviePagedAdapter.MovieViewHolder>
        implements SmartMovieList.UpdateListener
{

    private static final String TAG = MoviePagedAdapter.class.getSimpleName();
    private static final int TYPE_MOVIE = 0;
    private static final int TYPE_LOADING = 1;
    private static final int TYPE_RESTORING = 2;

    private Context mContext;
    private SmartMovieList mMovieList;
    private Map<Integer, String> mFavorites;
    GridLayoutManager.SpanSizeLookup mSpanLookup;


    private boolean isLoadingIndicator(int position)
    {
        if(position == mMovieList.size()) {
            Log.d(TAG, "Loading Indicatator at position "+position);

            mMovieList.fetchMoreMovies();
            return true;
        }
        return false;
    }

    public MoviePagedAdapter(Context context) {
        mContext = context;
        mMovieList = new SmartMovieList(mContext);

        /* Make progress indicator span the whole width of the screen and not just one cell */
        mSpanLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (isLoadingIndicator(position))
                    return GridActivity.calculateNoOfColumns(mContext);
                return 1;
            }
        };

        mMovieList.setUpdateListener(this);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingIndicator(position))
            return TYPE_LOADING;
        if (mMovieList.isRestoringState())
            return TYPE_RESTORING;
        return TYPE_MOVIE;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case TYPE_MOVIE:
                v = inflater.inflate(R.layout.movie_grid_item_layout, parent, false);
                break;
            case TYPE_LOADING:
                v = inflater.inflate(R.layout.movie_progress_item_layout, parent, false);
                break;
            case TYPE_RESTORING:
                v = inflater.inflate(R.layout.movie_grid_item_layout, parent, false);
                break;
            default:
                Log.d(TAG, "onCreateViewHolder() - Inavlid viewType "+viewType);
                return null;
        }
        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        /* Nothing to set if the are not showing a movie */
        Log.d(TAG, "Binding position "+position);
        if (holder.getItemViewType() == TYPE_LOADING)
            return;
        if (holder.getItemViewType() == TYPE_RESTORING)
        {
            holder.mMoviePosterImageView.setImageResource(R.drawable.poster_placeholder);
            holder.mGridItemTextView.setText(R.string.loading);
            return;
        }
        Movie m = mMovieList.getMovie(position);
        if (m == null) {
            Log.d(TAG, "No movie was found for position "+position);
            return;
        }
        holder.mGridItemTextView.setText(m.getTitle());

        if (m.isFavorite())
            holder.mFavoriteImageView.setVisibility(View.VISIBLE);
        else
            holder.mFavoriteImageView.setVisibility(View.INVISIBLE);

        String url = MovieDBApi.getMovieImageURL(m.getPoster_path());
        Log.d(TAG, url);
        Picasso.with(mContext)
                .load(url)
                .placeholder(R.drawable.poster_placeholder)
                .into(holder.mMoviePosterImageView);
    }

    /* Returns the size fo the internal list plus one.
        for infinite/paged
            The last one is always the loading progress bar item.
     */
    @Override
    public int getItemCount() {
       // Log.d(TAG, "Real count is "+mMovieList.size());
        return mMovieList.size()+1;
    }

    public void handleResume() {
        mMovieList.refreshFavorites();
    }

    /* For paging updates, they just add movies to the list */
    @Override
    public void OnUpdate(int startPos, int itemCount) {
        Log.d(TAG, "OnUpdate() start: "+startPos+" itemCount: "+itemCount);
        notifyItemRangeChanged(startPos, itemCount);
    }

    public void onSaveInstanceState(Bundle outState) {
        mMovieList.onSaveInstanceState(outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mMovieList.onRestoreInstanceState(savedInstanceState);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView mGridItemTextView;
        ImageView mMoviePosterImageView;
        ImageView mFavoriteImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mGridItemTextView = itemView.findViewById(R.id.tv_movie_title);
            mMoviePosterImageView = itemView.findViewById(R.id.iv_movie_poster);
            mFavoriteImageView = itemView.findViewById(R.id.iv_favorite);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Movie m = mMovieList.getMovie(position);
            Log.d(TAG, "Clicked on position "+position+" - "+mMovieList.getMovie(position).getTitle());
            Intent i = new Intent(mContext, MovieDetailsActivity.class);
            i.putExtra(Movie.KEY_MOVIE, m);
            mContext.startActivity(i);
        }
    }

    public void fullReset() {
        mMovieList.reset();
        notifyDataSetChanged();
    }
}
