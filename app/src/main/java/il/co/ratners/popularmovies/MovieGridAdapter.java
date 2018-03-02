package il.co.ratners.popularmovies;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by bratner on 2/24/18.
 */

class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {
    int mFakeDataItems = 100;

    public MovieGridAdapter() {
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.movie_grid_item_layout, parent, false);
        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        /* TODO: get a movie poster from cache or from the web */
        holder.mGridItemTextView.setText("Movie: "+position);
        holder.mMoviePosterImageView.setImageResource(R.drawable.deadpool_international_poster);
    }



    @Override
    public int getItemCount() {
        return mFakeDataItems;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView mGridItemTextView;
        ImageView mMoviePosterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);

            mGridItemTextView = itemView.findViewById(R.id.tv_movie_title);
            mMoviePosterImageView = itemView.findViewById(R.id.iv_movie_poster);
            //mMoviePosterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        }


    }

}
