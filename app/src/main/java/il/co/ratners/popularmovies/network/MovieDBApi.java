package il.co.ratners.popularmovies.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import il.co.ratners.popularmovies.BuildConfig;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;

public class MovieDBApi {

    public static final String API_URL = "https://api.themoviedb.org/3/";
    public static final String INPUT_DATE_FIELD_FORMAT = "yyyy-mm-dd";


    private static final String IMAGES_URL = "https://image.tmdb.org/t/p/";
    private static final String DEFAULT_IMAGE_SIZE = "w185";



    /* public static final  String SORT_BY_POPULARITY = "popular"; */
    /* public static final  String SORT_BY_RATING = "top_rated"; */

    private static String sImageSize = DEFAULT_IMAGE_SIZE;




    public static String getMovieImageURL(String imagePath) {
        return IMAGES_URL+"/"+sImageSize+"/"+imagePath;
    }

    public static class MovieDBList {
        public Integer page;
        Integer total_results;
        Integer total_pages;
        ArrayList<MovieDBItem> results;

        public ArrayList<MovieDBItem> getResults() {
            return results;
        }
    }

    public static class MovieDBItem {
        public Integer id;
        public String title;
        public String original_title;
        public Double popularity;
        public Double vote_average;
        public Integer vote_count;
        public String poster_path;
        public String backdrop_path;
        public String overview;
        public Date release_date;
    }

    public static class MovieDBVideoList {
        Integer id;
        ArrayList<MovieDBVideo> results;

        public ArrayList<MovieDBVideo> getList() {
            return results;
        }
    }

    public static class MovieDBVideo {
        String id; //This is a hash for the video object
        String iso_639_1; //language
        String iso_3166_1; //country code
        String key;
        String name; //Title of the video
        String site; //"YouTube"
        Integer size;
        String type; //"Teaser", "Trailer", maybe more

        public String toString() {
            return name;
        }

        public String getVideoLink() {
            String link = "http://youtube.com/watch?v="+key;
            return link;
        }

        public String getVideoKey() {
            return key;
        }
    }

    public static class MovieDBReviewList {
        Integer id;
        ArrayList<MovieDBReview> results;

        public ArrayList<MovieDBReview> getReviewList() {
            return results;
        }
    }

    public static class MovieDBReview {
        String id;
        String author;
        String content;
        String url;

        public String toString() {
            return author +" says \"" + content + "\"";
        }
    }
    public interface MovieDBFunctions {
        @GET("movie/popular")
        Call<MovieDBList> popular_movies(
                @Query("page") Integer page,
                @Query("language") String language
        );

        @GET("movie/top_rated")
        Call<MovieDBList> top_rated(
                @Query("page") Integer page,
                @Query("language") String language
        );

        @GET("movie/{id}")
        Call<MovieDBItem> getMovieDetails(
                @Path("id") Integer id
        );

        @GET("movie/{id}/videos")
        Call<MovieDBVideoList> getMovieVideos(
                @Path("id") Integer id
        );

        @GET("movie/{id}/reviews")
        Call<MovieDBReviewList> getMovieReviews(
                @Path("id") Integer id
        );

    }




}
