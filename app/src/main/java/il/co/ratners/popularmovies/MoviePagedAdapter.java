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
    private Context mContext;
    private SmartMovieList mMovieList;
    private Map<Integer, String> mFavorites;
    GridLayoutManager.SpanSizeLookup mSpanLookup;


    private boolean isLoadingIndicator(int position)
    {
        if(mMovieList.getMovie(position) == null) {
            Log.d(TAG, "Loading Indicatator at position "+position);
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
            return 1;
        return 0;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case 0:
                v = inflater.inflate(R.layout.movie_grid_item_layout, parent, false);
                break;
            case 1:
                v = inflater.inflate(R.layout.movie_progress_item_layout, parent, false);
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
        if (holder.getItemViewType() != 0)
            return;
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
        Log.d(TAG, "Real count is "+mMovieList.size());
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
            String url = MovieDBApi.getMovieImageURL(m.getPoster_path());

            i.putExtra(Movie.KEY_ID, m.getId());
            i.putExtra(Movie.KEY_TITLE, m.getTitle());
            i.putExtra(Movie.KEY_ORIGINAL_TITLE, m.getOriginalTitle());
            i.putExtra(Movie.KEY_OVERVIEW, m.getOverview());
            i.putExtra(Movie.KEY_RATING, m.getRating());
            i.putExtra(Movie.KEY_RELEASE_DATE, m.getFormattedDate());
            i.putExtra(Movie.KEY_POSTER_URL, url);
            i.putExtra(Movie.KEY_FAVORITE, m.isFavorite());
            i.putExtra(Movie.KEY_JSON, m.toJson());
            Log.d(TAG, "Movie to JSON "+m.toJson());
            mContext.startActivity(i);
        }
    }

    public void fullReset() {
        mMovieList.reset();
        notifyDataSetChanged();
    }
}
