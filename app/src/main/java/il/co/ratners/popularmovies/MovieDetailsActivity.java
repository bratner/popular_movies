package il.co.ratners.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import il.co.ratners.popularmovies.data.FavoritesProvider;
import il.co.ratners.popularmovies.data.Movie;
import il.co.ratners.popularmovies.databinding.ActivityMovieDetailsBinding;
import il.co.ratners.popularmovies.network.MovieDBApi;
import il.co.ratners.popularmovies.network.MovieDBConnector;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private static final int NO_ID = -1;
    private MovieDBApi.MovieDBVideoList mTrailers;
    private MovieDBApi.MovieDBReviewList mReviewList;
    private ActivityMovieDetailsBinding mBinding;
    private MovieDBConnector mMovieDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_movie_details);


        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        ActionBar actionBar = getSupportActionBar();
        Intent i = getIntent();

        String originalTitle = i.getStringExtra(Movie.KEY_ORIGINAL_TITLE);
        String overview = i.getStringExtra(Movie.KEY_OVERVIEW);
        String releaseDate = i.getStringExtra(Movie.KEY_RELEASE_DATE);
        String rating = i.getStringExtra(Movie.KEY_RATING);
        String title = i.getStringExtra(Movie.KEY_TITLE);
        String url = i.getStringExtra(Movie.KEY_POSTER_URL);
        int id = i.getIntExtra(Movie.KEY_ID, NO_ID);

        if (title != null)
            actionBar.setTitle(title);

        mBinding.detailsText.tvOriginalTitle.setText(originalTitle);
        mBinding.detailsText.tvOverview.setText(overview);
        mBinding.detailsText.tvRating.setText(rating);
        mBinding.detailsText.tvReleaseDate.setText(releaseDate);
        Picasso.with(this).load(url)
                .placeholder(R.drawable.poster_placeholder)
                .into(mBinding.ivMoviePoster);

        mBinding.detailsText.swFavorite.setOnCheckedChangeListener(this);

        mMovieDB = new MovieDBConnector(this);

        /* Make sure we got a real ID before using it for web requests */
        if (id != NO_ID) {
            mMovieDB.getMovieVideos(id).enqueue(videoListCallback());
            mMovieDB.getMovieReviews(id).enqueue(reviewListCallback());
        }
       // String posterURL = i.getStringExtra(R.string.key_poster);
    }



    private Callback<MovieDBApi.MovieDBReviewList> reviewListCallback() {
        return new Callback<MovieDBApi.MovieDBReviewList>() {
            @Override
            public void onResponse(Call<MovieDBApi.MovieDBReviewList> call, Response<MovieDBApi.MovieDBReviewList> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MovieDetailsActivity.this, "Failed fetching review list",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mReviewList = response.body();
                refreshReviewsUI();
            }

            @Override
            public void onFailure(Call<MovieDBApi.MovieDBReviewList> call, Throwable t) {
                Toast.makeText(MovieDetailsActivity.this, "Failed fetching review list",
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void refreshReviewsUI() {

        if (mReviewList.getReviewList().isEmpty())
        {
            mBinding.trailersAndReviews.labelReviews.setVisibility(View.GONE);
            mBinding.trailersAndReviews.lvReviewsList.setVisibility(View.GONE);

            return;
        }

         ArrayAdapter<MovieDBApi.MovieDBReview> adapter = new ArrayAdapter<MovieDBApi.MovieDBReview>(this,
                R.layout.review_list_item, R.id.tv_review_content,mReviewList.getReviewList());

        mBinding.trailersAndReviews.lvReviewsList.setAdapter(adapter);

        mBinding.trailersAndReviews.labelReviews.setText(getString(R.string.reviews, mReviewList.getReviewList().size()));
        mBinding.trailersAndReviews.labelReviews.setVisibility(View.VISIBLE);
        mBinding.trailersAndReviews.lvReviewsList.setVisibility(View.VISIBLE);
    }

    private Callback<MovieDBApi.MovieDBVideoList> videoListCallback() {
        return new Callback<MovieDBApi.MovieDBVideoList>() {
            @Override
            public void onResponse(Call<MovieDBApi.MovieDBVideoList> call, Response<MovieDBApi.MovieDBVideoList> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MovieDetailsActivity.this, "Failed fetching trailer list",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mTrailers = response.body();
                refreshTrailerUI();
            }

            @Override
            public void onFailure(Call<MovieDBApi.MovieDBVideoList> call, Throwable t) {
                Toast.makeText(MovieDetailsActivity.this, "Failed fetching trailer list",
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void refreshTrailerUI() {

        if (mTrailers.getList().isEmpty())
        {

            mBinding.trailersAndReviews.labelTrailers.setVisibility(View.GONE);
            mBinding.trailersAndReviews.lvTrailerList.setVisibility(View.GONE);

            return;
        }
        ArrayAdapter<MovieDBApi.MovieDBVideo> adapter = new ArrayAdapter<MovieDBApi.MovieDBVideo>(this,
                R.layout.trailer_list_item, R.id.tv_video_description,mTrailers.getList());

        mBinding.trailersAndReviews.lvTrailerList.setAdapter(adapter);

        mBinding.trailersAndReviews.lvTrailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieDBApi.MovieDBVideo video = mTrailers.getList().get(position);
                watchYoutubeVideo(video.getVideoKey());
            }
        });

        mBinding.trailersAndReviews.labelTrailers.setText(getString(R.string.trailers, mTrailers.getList().size()));
        mBinding.trailersAndReviews.labelTrailers.setVisibility(View.VISIBLE);
        mBinding.trailersAndReviews.lvTrailerList.setVisibility(View.VISIBLE);

    }

    /*
        Starts a youtube video

        Based on an SO answer in:
            https://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
    */
    private void watchYoutubeVideo(String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, isChecked ? "Adding to favorites":"Removing from favorites");
        if (isChecked) {
            addToFavorites();
        } else {
            removeFromFavorites();
        }


    }

    private void removeFromFavorites() {

    }

    private void addToFavorites() {

    }

    class FavoritesDBTask extends AsyncTask<Integer, Void, Cursor> {

        public FavoritesDBTask() {

        }

        @Override
        protected Cursor doInBackground(Integer... integers) {
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);

        }
    }
}
