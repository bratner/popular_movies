package il.co.ratners.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by bratner on 2/24/18.
 */

class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {
    int mFakeDataItems = 100;

    public MovieGridAdapter() {
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return null;
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

    }



    @Override
    public int getItemCount() {
        return 0;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView mGridItemTextView;
        public MovieViewHolder(View itemView) {
            super(itemView);
        }
    }

}
