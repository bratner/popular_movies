package il.co.ratners.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MovieDetailsActivity extends AppCompatActivity {
    public static final String TAG = MovieDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ActionBar actionBar = getSupportActionBar();
        Intent i = getIntent();
        String originalTitle = i.getStringExtra(getString(R.string.key_original_title));
        String overview = i.getStringExtra(getString(R.string.key_overview));
        String releaseDate = i.getStringExtra(getString(R.string.key_date));
        String rating = i.getStringExtra(getString(R.string.key_rating));
        String title = i.getStringExtra(getString(R.string.key_title));

        actionBar.setTitle(title);


       // String posterURL = i.getStringExtra(R.string.key_poster);

    }
}
