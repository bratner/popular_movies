package il.co.ratners.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.squareup.picasso.Picasso;

import il.co.ratners.popularmovies.databinding.ActivityMovieDetailsBinding;

public class MovieDetailsActivity extends AppCompatActivity {
    public static final String TAG = MovieDetailsActivity.class.getSimpleName();
    private ActivityMovieDetailsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_movie_details);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);
        ActionBar actionBar = getSupportActionBar();
        Intent i = getIntent();

        String originalTitle = i.getStringExtra(getString(R.string.key_original_title));
        String overview = i.getStringExtra(getString(R.string.key_overview));
        String releaseDate = i.getStringExtra(getString(R.string.key_date));
        String rating = i.getStringExtra(getString(R.string.key_rating));
        String title = i.getStringExtra(getString(R.string.key_title));
        String url = i.getStringExtra(getString(R.string.key_poster_url));

        actionBar.setTitle(title);

        mBinding.detailsText.tvOriginalTitle.setText(originalTitle);
        mBinding.detailsText.tvOverview.setText(overview);
        mBinding.detailsText.tvRating.setText(rating);
        mBinding.detailsText.tvReleaseDate.setText(releaseDate);
        Picasso.with(this).load(url)
                .placeholder(R.drawable.poster_placeholder)
                .into(mBinding.ivMoviePoster);
       // String posterURL = i.getStringExtra(R.string.key_poster);

    }
}
