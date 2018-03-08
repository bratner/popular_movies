package il.co.ratners.popularmovies;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

        mGridRecyclerView.setAdapter(new MovieGridAdapter(this));
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

        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
//        TextView textView = (TextView) messageView.findViewById(R.id);
//        int defaultColor = textView.getTextColors().getDefaultColor();
//        textView.setTextColor(defaultColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher_movies);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }
}

