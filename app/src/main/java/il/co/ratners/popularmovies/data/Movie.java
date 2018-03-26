package il.co.ratners.popularmovies.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bratner on 3/4/18.
 *
 * this class is inteded to be a utility class to represent both the data object of a movie
 * and the parser code.
 */

public class Movie {

    /* Class constants */
    
    private static final String TAG = Movie.class.getSimpleName();
    /* JSON Parsing constants */
    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String ORIGINAL_TITLE_FIELD = "original_title";
    private static final String POSTER_PATH_FIELD = "poster_path";
    private static final String BACKDROP_PATH_FIELD = "backdrop_path";
    private static final String OVERVIEW_FIELD = "overview";
    private static final String VOTE_AVERAGE_FIELD = "vote_average";
    private static final String VOTE_COUNT_FIELD = "vote_count";
    private static final String RELEASE_DATE_FIELD = "release_date";

    /* Key names for passing the data to details acitivity */
    public static final String KEY_TITLE = "title";
    public static final String KEY_ORIGINAL_TITLE = "original_title";
    public static final String KEY_OVERVIEW = "overview";
    public static final String KEY_RATING = "rating";
    public static final String KEY_RELEASE_DATE = "release_date";
    public static final String KEY_POSTER_URL = "poster_url";


    private static final String INPUT_DATE_FIELD_FORMAT = "yyyy-mm-dd";
    private static final String OUTPUT_DATE_FIELD_FORMAT = "MMMM dd, yyyy";

    /* Instance variables */
    private int id;
    private String title;
    private String original_title;
    private String poster_path;
    private String backdrop_path;
    private String overview;
    private String vote_average;
    private int vote_count;
    private Date release_date;
    private String rating;

    /**
     * Takes in a JSON object and returns a newly created Movie object or null on error.

     */
    public static Movie parseJsonToMovie(JSONObject movie_json)
    {
        Movie m = new Movie();

        /* All fields are mandatory, using exceptions to catch missing fields */
        try {

            m.id = movie_json.getInt(ID_FIELD);
            m.title = movie_json.getString(TITLE_FIELD);
            m.original_title = movie_json.getString(ORIGINAL_TITLE_FIELD);
            m.poster_path = movie_json.getString(POSTER_PATH_FIELD);
            m.backdrop_path = movie_json.getString(BACKDROP_PATH_FIELD);
            m.overview = movie_json.getString(OVERVIEW_FIELD);
            m.vote_average = movie_json.getString(VOTE_AVERAGE_FIELD);
            m.vote_count = movie_json.getInt(VOTE_COUNT_FIELD);

            String release_date_string = movie_json.getString(RELEASE_DATE_FIELD);
            SimpleDateFormat sdf = new SimpleDateFormat(INPUT_DATE_FIELD_FORMAT, Locale.ENGLISH);
            m.release_date = sdf.parse(release_date_string);

        } catch (JSONException jex) {
            Log.e(TAG, "Failed parsing JSON object. Exception: "+jex);
            return null;
        } catch (ParseException dex) {
            Log.e(TAG, "Failed converting JSON strings. Exception: "+dex);
            return null;
        }

        return m;
    }

    public String getTitle() {
        return title;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getOriginalTitle() {
        return original_title;
    }

    public String getOverview() {
        return overview;
    }

    public String getRating() {
        return vote_average;
    }

    public String getFormatedDate() {
        SimpleDateFormat sd = new SimpleDateFormat(OUTPUT_DATE_FIELD_FORMAT);
        return sd.format(release_date);
    }
}
