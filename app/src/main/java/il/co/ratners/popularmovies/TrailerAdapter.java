package il.co.ratners.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import il.co.ratners.popularmovies.network.MovieDBApi;


/* Adapter for horizontally scrolling list of movie trailers on the details activity */
class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private ArrayList<MovieDBApi.MovieDBVideo> mTrailers;




    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.trailer_item_layout, parent, false);
        TrailerViewHolder ret = new TrailerViewHolder(v);

        return ret;
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        Picasso.with(holder.mMoviePosterImageView.getContext()).load(mTrailers.get(position)
                .getThumbnailLink())
                .into(holder.mMoviePosterImageView);
        holder.mGridItemTextView.setText(mTrailers.get(position).getName());

    }

    @Override
    public int getItemCount() {
        if(mTrailers == null)
            return 0;
        return mTrailers.size();
    }

    public void setTrailers(ArrayList<MovieDBApi.MovieDBVideo> trailers) {
        mTrailers = trailers;
        notifyDataSetChanged();
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mGridItemTextView;
        ImageView mMoviePosterImageView;
        ImageView mFavoriteImageView;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            mGridItemTextView = itemView.findViewById(R.id.tv_movie_title);
            mMoviePosterImageView = itemView.findViewById(R.id.iv_movie_poster);
            mFavoriteImageView = itemView.findViewById(R.id.iv_favorite);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            MovieDetailsActivity.watchYoutubeVideo(v.getContext(),
                    mTrailers.get(getAdapterPosition()).getVideoKey());
        }
    }
}
