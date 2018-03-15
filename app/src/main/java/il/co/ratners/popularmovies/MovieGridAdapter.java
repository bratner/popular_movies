package il.co.ratners.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import il.co.ratners.popularmovies.data.Movie;
import il.co.ratners.popularmovies.data.SmartMovieList;
import il.co.ratners.popularmovies.utils.TheMovieDB;

/**
 * MovieGridAdapter - serves as a glue between RecylcerView requests and SmartMovieList
 * which manages web request and keeps the actual movie data.
 */

class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {

    public static final String TAG = MovieGridAdapter.class.getSimpleName();
    private Context mContext;
    private SmartMovieList mMovieList;

    GridLayoutManager.SpanSizeLookup mSpanLookup;


    private boolean isLoadingIndicator(int position)
    {
        if(mMovieList.getMovie(position) == null) {
            Log.d(TAG, "Loading Indicatator at position "+position);
            return true;
        }
        return false;
    }

    public MovieGridAdapter(Context context) {
        mContext = context;
        mMovieList = new SmartMovieList(mContext);

        /* Make progress indicator span the whole width of the screen and not just one cell */
        mSpanLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (isLoadingIndicator(position))
                    return mContext.getResources().getInteger(R.integer.movie_grid_number_of_columns);
                return 1;
            }
        };

        mMovieList.setUpdateListener(new SmartMovieList.UpdateListener() {
            @Override
            public void OnUpdate(int startPos, int itemCount) {
                Log.d(TAG, "OnUpdate() start: "+startPos+" itemCount: "+itemCount);
                MovieGridAdapter.this.notifyItemRangeChanged(startPos, itemCount);
            }
        });

        /* Start the download */
       // mMovieList.getMovie(0);
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
        String url = TheMovieDB.getMovieImageURL(m.getPoster_path());
        Log.d(TAG, url);
        Picasso.with(mContext)
                .load(url)
                .placeholder(R.drawable.poster_placeholder)
                .into(holder.mMoviePosterImageView);
    }

    @Override
    public int getItemCount() {
        int ret = mMovieList.size()+1;
        //Log.d(TAG, "getItemCount() - "+ret);
        return ret;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView mGridItemTextView;
        ImageView mMoviePosterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mGridItemTextView = itemView.findViewById(R.id.tv_movie_title);
            mMoviePosterImageView = itemView.findViewById(R.id.iv_movie_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Movie m = mMovieList.getMovie(position);
            Log.d(TAG, "Clicked on position "+position+" - "+mMovieList.getMovie(position).getTitle());
            Intent i = new Intent(mContext, MovieDetailsActivity.class);
            /*TODO: fill intent with enough extras */
            i.putExtra(mContext.getString(R.string.key_title),m.getTitle());
            i.putExtra(mContext.getString(R.string.key_original_title),m.getOriginalTitle());
            i.putExtra(mContext.getString(R.string.key_overview), m.getOverview());
            i.putExtra(mContext.getString(R.string.key_rating), m.getRating());
            i.putExtra(mContext.getString(R.string.key_release_date), m.getFormatedDate());
            mContext.startActivity(i);
        }
    }

    public void fullReset() {
        mMovieList.reset();
        notifyDataSetChanged();
    }

}
