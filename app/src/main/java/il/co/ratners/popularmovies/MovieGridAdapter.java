package il.co.ratners.popularmovies;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import il.co.ratners.popularmovies.data.Movie;
import il.co.ratners.popularmovies.data.SmartMovieList;
import il.co.ratners.popularmovies.utils.TheMovieDB;

/**
 * Created by bratner on 2/24/18.
 */

class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {
   // int mFakeDataItems = 100;
    public static final String TAG = MovieGridAdapter.class.getSimpleName();
    int mListStartPosition = 0;
    Context mContext;
    ArrayList<Movie> mMovies;
    GridLayoutManager.SpanSizeLookup mSpanLookup;
    SmartMovieList mMovieList;

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response, null if no response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }

    private boolean isLoadingIndicator(int position)
    {
        if(mMovieList.getMovie(position) == null)
            return true;
//        if (position == mMovies.size())
//                return true;
        return false;
    }

    class MovieGetterTask extends AsyncTask<Void, Void, ArrayList<Movie>>
    {
        final String TAG = MovieGetterTask.class.getSimpleName();
        @Override
        protected ArrayList<Movie> doInBackground(Void... voids) {
            try {
                ArrayList<Movie> lMovies;
                //URL url = new URL("https://api.themoviedb.org/3/discover/movie?api_key=1ba61ad61368b70c6437f62af9bd3345&language=en-US&sort_by=popularity.desc&include_adult=false&include_video=false&page=1");
                //URL url = new URL("https://api.themoviedb.org/3/movie/popular?api_key=1ba61ad61368b70c6437f62af9bd3345&language=en-US&page=1");
                Uri uri = Uri.parse("https://api.themoviedb.org/3").buildUpon()
                        .appendPath("movie")
                        .appendPath("popular")
                        .appendQueryParameter("api_key", "1ba61ad61368b70c6437f62af9bd3345")
                        .appendQueryParameter("language", "en-US")
                        .appendQueryParameter("page", "1").build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "URL is "+uri.toString());
                String json_input = getResponseFromHttpUrl(url);
                JSONObject response = new JSONObject(json_input);
                JSONArray jsonMovies = response.getJSONArray("results");
                lMovies = new ArrayList<>();
                for(int i = 0; i < jsonMovies.length(); ++i)
                {
                    Movie m = Movie.parseJsonToMovie(jsonMovies.getJSONObject(i));
                    if (m != null) {
                        lMovies.add(m);
                        Log.d(TAG, "Adding "+m.getTitle());
                    }
                }

                return lMovies;

            } catch (MalformedURLException uex) {
                Log.e(TAG, "Malformed URL: "+uex);
            } catch (IOException ioex) {
                Log.e(TAG, "Networking problem: "+ioex);
            } catch (JSONException jex) {
                Log.e(TAG, "Data error: "+jex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);
            if (movies == null) {
                Log.e(TAG, "Unable to retrieve remote data.");
                return;
            }
            mMovies = movies;
            MovieGridAdapter.this.notifyDataSetChanged();
        }
    };

    public MovieGridAdapter(Context context) {
        mMovies = new ArrayList<>();
        mContext = context;
        mMovieList = new SmartMovieList(mContext);
        mSpanLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (isLoadingIndicator(position))
                    return mContext.getResources().getInteger(R.integer.movie_grid_number_of_columns);
                return 1;
            }
        };
        mMovieList.setUpdateListener(new SmartMovieList.UpdateListener() {

            @Override
            public void OnUpdate(ArrayList<Integer> updated_positions) {
                MovieGridAdapter.this.notifyDataSetChanged();
            }
        });
        /* MovieGetterTask task = new MovieGetterTask();
         task.execute(); */
        /* TODO: Something like mMovieList.fillCache() might be in order */
        mMovieList.getMovie(0);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingIndicator(position))
            return 1;
        return 0;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case 0:
                v = inflater.inflate(R.layout.movie_grid_item_layout, parent, false);
                break;
            case 1:
                v = inflater.inflate(R.layout.movie_progress_item_layout, parent, false);
                break;
                /* TODO: think of error handling */
            default:
                return null;
        }
        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        /* TODO: get a movie poster from cache or from the web */
        if (holder.getItemViewType() != 0)
            return;
        Movie m = mMovieList.getMovie(position);
        if (m == null) {
            Log.d(TAG, "No movie was found for position "+position);
            return;
        }
        holder.mGridItemTextView.setText(m.getTitle());
        String url = TheMovieDB.getMovieImageURL(m.getPoster_path());
        Log.d(TAG, url);
        Picasso.with(mContext)
                .load(url).into(holder.mMoviePosterImageView);
    }



    @Override
    public int getItemCount() {
        return mMovieList.getEndPosition()+1;
       // return mMovies.size()+1;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView mGridItemTextView;
        ImageView mMoviePosterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mGridItemTextView = itemView.findViewById(R.id.tv_movie_title);
            mMoviePosterImageView = itemView.findViewById(R.id.iv_movie_poster);
        }


    }

}
