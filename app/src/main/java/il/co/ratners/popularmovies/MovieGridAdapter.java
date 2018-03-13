package il.co.ratners.popularmovies;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

import il.co.ratners.popularmovies.data.Movie;
import il.co.ratners.popularmovies.data.SmartMovieList;
import il.co.ratners.popularmovies.utils.TheMovieDB;

/**
 * Created by bratner on 2/24/18.
 */

class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {

    public static final String TAG = MovieGridAdapter.class.getSimpleName();
    Context mContext;
    ArrayList<Movie> mMovies;
    GridLayoutManager.SpanSizeLookup mSpanLookup;
    SmartMovieList mMovieList;

    private boolean isLoadingIndicator(int position)
    {
        if(mMovieList.getMovie(position) == null) {
            Log.d(TAG, "Loading Indicatator at position "+position);
            return true;
        }

        return false;
    }

    public MovieGridAdapter(Context context) {
        mMovies = new ArrayList<>();
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

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView mGridItemTextView;
        ImageView mMoviePosterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mGridItemTextView = itemView.findViewById(R.id.tv_movie_title);
            mMoviePosterImageView = itemView.findViewById(R.id.iv_movie_poster);
        }


    }


}
