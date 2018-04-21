package il.co.ratners.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import il.co.ratners.popularmovies.network.MovieDBApi;

/**
 * Created by bratner on 3/4/18.
 *
 * this class is inteded to be a utility class to represent both the data object of a movie
 * and the parser code.
 */

public class Movie implements Parcelable{

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
    public static final String KEY_ID = "id";
    public static final String KEY_FAVORITE = "favorite";
    public static final String KEY_JSON = "json";
    public static final String KEY_MOVIE = "themovie";

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
    private boolean favorite;

    public Movie() {}
    public Movie(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public static Movie movieDBItemToMovie(MovieDBApi.MovieDBItem movieDBitem)

    {
        Movie m = new Movie();

        m.id = movieDBitem.id;
        m.title = movieDBitem.title;
        m.original_title = movieDBitem.original_title;
        m.overview = movieDBitem.overview;
        m.poster_path = movieDBitem.poster_path;
        m.vote_average = String.format("%.2f", movieDBitem.vote_average);
        m.release_date = movieDBitem.release_date;
        m.vote_count = movieDBitem.vote_count;

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

    public String getFormattedDate() {
        SimpleDateFormat sd = new SimpleDateFormat(OUTPUT_DATE_FIELD_FORMAT);
        return sd.format(release_date);
    }

    public int getId() {
        return id;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String toJson() {
        Gson gson = new Gson();
        String ret = gson.toJson(this);

        return ret;
    }



    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public static Movie fromJson(String s) {
        Gson gson = new Gson();
        Movie m = gson.fromJson(s, Movie.class);
        return m;
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        original_title = in.readString();
        poster_path = in.readString();
        backdrop_path = in.readString();
        overview = in.readString();
        vote_average = in.readString();
        vote_count = in.readInt();
        release_date = (Date)in.readSerializable();
        favorite = in.readInt()==1;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(original_title);
        dest.writeString(poster_path);
        dest.writeString(backdrop_path);
        dest.writeString(overview);
        dest.writeString(vote_average);
        dest.writeInt(vote_count);
        dest.writeSerializable(release_date);
        dest.writeInt(favorite?1:0);
    }
}
