package il.co.ratners.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MovieDetailsActivity extends AppCompatActivity {
    public static final String TAG = MovieDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Intent i = getIntent();
        String originalTitle = i.getStringExtra(getString(R.string.key_original_title));
        String overview = i.getStringExtra(getString(R.string.key_overview));

    }
}
