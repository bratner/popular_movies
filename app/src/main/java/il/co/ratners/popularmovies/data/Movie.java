package il.co.ratners.popularmovies.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bratner on 3/4/18.
 *
 * this class is inteded to be a utility class to represent both the data object of a movie
 * and the parser code.
 */

public class Movie {

    public static final String TAG = Movie.class.getSimpleName();
    public static final String id_field = "id";
    public static final String title_field = "title";
    public static final String original_title_field = "original_title";
    public static final String poster_path_field = "poster_path";
    public static final String backdrop_path_field = "backdrop_path";
    public static final String overview_field = "overview";
    public static final String vote_average_field = "vote_average";
    public static final String vote_count_field = "vote_count";
    public static final String release_date_field = "release_date";
    public static final String date_field_format = "yyyy-mm-dd";



    int id;
    String title;
    String original_title;
    String poster_path;
    String backdrop_path;
    String overview;
    Double vote_average;
    int vote_count;
    Long release_date;

    /**
     * Takes in a JSON object and returns a newly created Movie object

     */
    public static Movie parseJsonToMovie(JSONObject movie_json)
    {
        Movie m = new Movie();

        /* All fields are mandatory, using exceptions to catch missing fields */
        try {

            m.id = movie_json.getInt(id_field);
            m.title = movie_json.getString(title_field);
            m.original_title = movie_json.getString(original_title_field);
            m.poster_path = movie_json.getString(poster_path_field);
            m.backdrop_path = movie_json.getString(backdrop_path_field);
            m.overview = movie_json.getString(overview_field);
            m.vote_average = movie_json.getDouble(vote_average_field);
            m.vote_count = movie_json.getInt(vote_count_field);

            String release_date_string = movie_json.getString(release_date_field);
            SimpleDateFormat sdf = new SimpleDateFormat(date_field_format);
            Date release_date = sdf.parse(release_date_string);
            
            m.release_date = release_date.getTime();

        } catch (JSONException jex) {
            Log.e(TAG, "Failed parsing JSON object. Exception: "+jex);
            return null;
        } catch (ParseException dex) {
            Log.e(TAG, "Failed converting JSON strings. Exception: "+dex);
            return null;
        }

        return m;
    }
}
