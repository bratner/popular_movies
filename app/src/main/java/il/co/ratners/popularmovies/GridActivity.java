package il.co.ratners.popularmovies;


import android.content.Context;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


import il.co.ratners.popularmovies.utils.PreferenceUtils;

public class GridActivity extends AppCompatActivity {


    public final static String TAG = GridActivity.class.getSimpleName();
    private static final String KEY_INSTANCE_STATE_RV_POSITION = "grid_position";


    private RecyclerView mGridRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private MoviePagedAdapter moviePagedAdapter;
    private MovieStaticAdapter favoritesAdapter;
    private boolean mPagedGrid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);


        mGridRecyclerView = findViewById(R.id.movie_grid_rv);
        mGridRecyclerView.setHasFixedSize(true);

        int numberOfColumns = calculateNoOfColumns(this);
        Log.d(TAG, "Number of columns: "+numberOfColumns);
        mGridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        moviePagedAdapter = new MoviePagedAdapter(this);
        favoritesAdapter = new MovieStaticAdapter(this);

        /* The Adapter is responsible for orchestrating the loading. So it will tell
           the grid which is the loading indicator
          */
        mGridRecyclerView.setLayoutManager(mGridLayoutManager);
        resetGridAdaptersPagingState();
        updateTitle();
    }

    private void resetGridAdaptersPagingState()
    {
        mPagedGrid = retrievePagingState();
        if(mPagedGrid) {
            mGridLayoutManager.setSpanSizeLookup(moviePagedAdapter.mSpanLookup);
            mGridRecyclerView.setAdapter(moviePagedAdapter);
            moviePagedAdapter.notifyDataSetChanged();
        } else {
            mGridLayoutManager.setSpanCount(calculateNoOfColumns(this));
            mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.DefaultSpanSizeLookup());

            favoritesAdapter = new MovieStaticAdapter(this);
            mGridRecyclerView.setAdapter(favoritesAdapter);
        }
    }
    private boolean retrievePagingState() {
        boolean ret = true;
        if (PreferenceUtils.getGridContentType(this).equals(PreferenceUtils.FAVORITES))
            ret = false;

        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.grid_menu, menu);
        return true;
    }

    private void updateTitle() {
        String content = PreferenceUtils.getGridContentType(this);
        String newTitle = null;
        switch (content) {
            case PreferenceUtils.POPULAR:
                newTitle = getString(R.string.by_popularity);
                break;
            case PreferenceUtils.TOP_RATED:
                newTitle = getString(R.string.top_rated);
                break;
            case PreferenceUtils.FAVORITES:
                newTitle = getString(R.string.favorites);
                break;
        }

        getSupportActionBar().setTitle(newTitle);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
            case R.id.menu_about:
                ShowAboutDialog();
                break;
            case R.id.menu_sort_popularity:
                PreferenceUtils.setSortOrder(this, PreferenceUtils.GRID_CONTENT.POPULARITY);
                updateTitle();
                resetGridAdaptersPagingState();
                moviePagedAdapter.fullReset();
                break;
            case R.id.menu_sort_rating:
                PreferenceUtils.setSortOrder(this, PreferenceUtils.GRID_CONTENT.RATING);
                updateTitle();
                resetGridAdaptersPagingState();
                moviePagedAdapter.fullReset();
                break;
            case R.id.menu_favorites:
                PreferenceUtils.setSortOrder(this, PreferenceUtils.GRID_CONTENT.FAVORITES);
                updateTitle();
                resetGridAdaptersPagingState();
                break;

            default:
                ret = false;
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

    @Override
    protected void onResume() {

        Log.d(TAG, "RESUME");
        super.onResume();
        if(mPagedGrid)
            moviePagedAdapter.handleResume();
        else
            favoritesAdapter.handleResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "PAUSE");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelable(KEY_INSTANCE_STATE_RV_POSITION,
//                mGridLayoutManager.onSaveInstanceState());
        /* TODO: Save movie list unless it is favorites */
        if (mPagedGrid) {
            moviePagedAdapter.onSaveInstanceState(outState);
        }
        Log.d(TAG, "SAVE_INSTANCE_STATE");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mPagedGrid)
            moviePagedAdapter.onRestoreInstanceState(savedInstanceState);
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 180;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        if(noOfColumns < 2)
            noOfColumns = 2;
        return noOfColumns;
    }
}

