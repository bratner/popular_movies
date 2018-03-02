package il.co.ratners.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;

public class GridActivity extends AppCompatActivity {
    RecyclerView mGridRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        mGridRecyclerView = findViewById(R.id.movie_grid_rv);

        /*TODO: actually calculate the number of columns based on width DP*/
        int calculate_number_of_columns = getResources().getInteger(R.integer.movie_grid_number_of_columns);
        mGridRecyclerView.setLayoutManager(
                new GridLayoutManager(this,calculate_number_of_columns));

        mGridRecyclerView.setAdapter(new MovieGridAdapter());
    }
}
