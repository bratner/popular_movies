package il.co.ratners.popularmovies;

import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import il.co.ratners.popularmovies.data.FavoritesRequest;
import il.co.ratners.popularmovies.data.Movie;
import il.co.ratners.popularmovies.databinding.ActivityMovieDetailsBinding;
import il.co.ratners.popularmovies.network.MovieDBApi;
import il.co.ratners.popularmovies.network.MovieDBConnector;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener,
        FavoritesRequest.FavoritesResponseListener
{

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private static final int NO_ID = -1;
    private static final String TRAILERS_RV_POSITION = "TRAILERS_KEY";
    private MovieDBApi.MovieDBVideoList mTrailers;
    private MovieDBApi.MovieDBReviewList mReviewList;
    private ActivityMovieDetailsBinding mBinding;
    private MovieDBConnector mMovieDB;
    private TrailerAdapter mTrailerAdapter;
    private int mMovieId = NO_ID;
    private String mJson;

    private Movie mMovie;
    private LinearLayoutManager mTrailersLayoutManager;
    private Parcelable mTrailersLayoutSavedState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null )
            mTrailersLayoutSavedState = savedInstanceState.getParcelable(TRAILERS_RV_POSITION);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        ActionBar actionBar = getSupportActionBar();
        Intent i = getIntent();

        mMovie = i.getParcelableExtra(Movie.KEY_MOVIE);

        String url = MovieDBApi.getMovieImageURL(mMovie.getPoster_path());
        mMovieId = mMovie.getId();
        mJson = mMovie.toJson();

        if (mMovie.getTitle() != null)
            actionBar.setTitle(mMovie.getTitle());


        mBinding.detailsText.tvOriginalTitle.setText(mMovie.getOriginalTitle());
        mBinding.detailsText.tvOverview.setText(mMovie.getOverview());
        mBinding.detailsText.tvRating.setText(mMovie.getRating());
        mBinding.detailsText.tvReleaseDate.setText(mMovie.getFormattedDate());

        Picasso.with(this).load(url)
                .placeholder(R.drawable.poster_placeholder)
                .into(mBinding.ivMoviePoster);

        mBinding.detailsText.swFavorite.setOnCheckedChangeListener(this);
        mBinding.detailsText.swFavorite.setChecked(mMovie.isFavorite());

        mMovieDB = new MovieDBConnector(this);
        mTrailersLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.trailersAndReviews.rvHorizonatlTrailers.setLayoutManager(mTrailersLayoutManager);
        mTrailerAdapter = new TrailerAdapter();
        mBinding.trailersAndReviews.rvHorizonatlTrailers.setAdapter(mTrailerAdapter);
        /* Make sure we got a real ID before using it for web requests */
        if (mMovieId != NO_ID) {
            mMovieDB.getMovieVideos(mMovieId).enqueue(videoListCallback());
            mMovieDB.getMovieReviews(mMovieId).enqueue(reviewListCallback());
            queryFavorite();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TRAILERS_RV_POSITION, mTrailersLayoutManager.onSaveInstanceState());
    }

    private Callback<MovieDBApi.MovieDBReviewList> reviewListCallback() {
        return new Callback<MovieDBApi.MovieDBReviewList>() {
            @Override
            public void onResponse(Call<MovieDBApi.MovieDBReviewList> call, Response<MovieDBApi.MovieDBReviewList> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MovieDetailsActivity.this, R.string.failed_fetching_review,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mReviewList = response.body();
                refreshReviewsUI();
            }

            @Override
            public void onFailure(Call<MovieDBApi.MovieDBReviewList> call, Throwable t) {
                Toast.makeText(MovieDetailsActivity.this, R.string.failed_fetching_review,
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
                    Toast.makeText(MovieDetailsActivity.this, R.string.failed_fetching_trailers,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mTrailers = response.body();
                refreshTrailerUI();
            }

            @Override
            public void onFailure(Call<MovieDBApi.MovieDBVideoList> call, Throwable t) {
                Toast.makeText(MovieDetailsActivity.this,  R.string.failed_fetching_trailers,
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void refreshTrailerUI() {

        if (mTrailers.getList().isEmpty())
        {

            mBinding.trailersAndReviews.labelTrailers.setVisibility(View.GONE);
            mBinding.trailersAndReviews.rvHorizonatlTrailers.setVisibility(View.GONE);


            return;
        }
        mTrailerAdapter.setTrailers(mTrailers.getList());
        mBinding.trailersAndReviews.labelTrailers.setText(getString(R.string.trailers, mTrailers.getList().size()));
        mBinding.trailersAndReviews.labelTrailers.setVisibility(View.VISIBLE);
        mBinding.trailersAndReviews.rvHorizonatlTrailers.setVisibility(View.VISIBLE);
        if(mTrailersLayoutSavedState != null) {
            mTrailersLayoutManager.onRestoreInstanceState(mTrailersLayoutSavedState);
            mTrailersLayoutSavedState = null;
        }

    }

    /*
        Starts a youtube video

        Based on an SO answer in:
            https://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
    */
    public static void watchYoutubeVideo(Context context, String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
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

            FavoritesRequest request = new FavoritesRequest(this, FavoritesRequest.DELETE_ACTION,
                    mMovieId,this);
            request.execute();
    }



    private void addToFavorites() {
        FavoritesRequest request = new FavoritesRequest(this, FavoritesRequest.ADD_ACTION,
                mMovieId, this, mJson);
        request.execute();
    }

    private void queryFavorite() {
        FavoritesRequest request = new FavoritesRequest(this, FavoritesRequest.FIND_ACTION,
                mMovieId, this);
        request.execute();
    }

    @Override
    public void onMovieRemoved(boolean really) {
        Log.d(TAG, "Movie removed from favorites. "+really);
        onMovieFound(false);
    }

    @Override
    public void onMovieAdded(boolean really) {
        Log.d(TAG, "Movie added to favorites. "+really);
        onMovieFound(true);
    }

    @Override
    public void onMovieFound(boolean found) {
        //Log.d(TAG, "Movie "+(found ? " was found": " was NOT found"));
    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
