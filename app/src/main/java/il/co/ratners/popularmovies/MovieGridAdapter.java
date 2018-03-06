package il.co.ratners.popularmovies;

import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

/**
 * Created by bratner on 2/24/18.
 */

class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {
   // int mFakeDataItems = 100;
    int mListStartPosition = 0;
    ArrayList<Movie> mMovies;

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


    class MovieGetterTask extends AsyncTask<Void, Void, ArrayList<Movie>>
    {
        final String TAG = MovieGetterTask.class.getSimpleName();
        @Override
        protected ArrayList<Movie> doInBackground(Void... voids) {
            try {
                ArrayList<Movie> lMovies;
                URL url = new URL("https://api.themoviedb.org/3/discover/movie?api_key=1ba61ad61368b70c6437f62af9bd3345&language=en-US&sort_by=popularity.desc&include_adult=false&include_video=false&page=1");
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
            mMovies = movies;
        }
    };

    public MovieGridAdapter() {
        MovieGetterTask task = new MovieGetterTask();
        mMovies = new ArrayList<>();
        task.execute();


    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.movie_grid_item_layout, parent, false);
        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        /* TODO: get a movie poster from cache or from the web */
        holder.mGridItemTextView.setText(mMovies.get(position).getTitle());
        holder.mMoviePosterImageView.setImageResource(R.drawable.zootopia_poster_small);

    }



    @Override
    public int getItemCount() {
        return mMovies.size();
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
