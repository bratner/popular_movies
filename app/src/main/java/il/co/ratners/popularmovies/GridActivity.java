package il.co.ratners.popularmovies;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GridActivity extends AppCompatActivity {
    RecyclerView mGridRecyclerView;
    GridLayoutManager mGridLayoutManager;
    MovieGridAdapter mGridAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        mGridRecyclerView = findViewById(R.id.movie_grid_rv);

        /* TODO: consider calculating the number of columns based on width in DP and DPI.
         * Might be better then using a reasonable default.
         */
        int calculate_number_of_columns = getResources().getInteger(R.integer.movie_grid_number_of_columns);
        mGridLayoutManager = new GridLayoutManager(this, calculate_number_of_columns);
        mGridAdapter = new MovieGridAdapter(this);

        /* The Adapter is responsible for orchestrating the loading. So it will tell
           the grid which is the loading indicator
          */
        mGridLayoutManager.setSpanSizeLookup(mGridAdapter.mSpanLookup);
        mGridRecyclerView.setLayoutManager(mGridLayoutManager);
        mGridRecyclerView.setAdapter(mGridAdapter);

        /* TODO: The adapter should have a scroll listener to manage loads */
        mGridRecyclerView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        Log.d("BRAT", "Scroll state changed "+newState);
                    }

                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.grid_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = false;
        if (item.getItemId() == R.id.menu_about)
        {
            ShowAboutDialog();
        }
        return ret;
    }


    /*
        Display about dialog mainly to comply with themoviedb
        attribution requirements.
        based on: http://android.okhelp.cz/create-about-dialog-android-example/
     */
    private void ShowAboutDialog() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().
                inflate(R.layout.about_dialog_layout, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher_movies);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }
}

