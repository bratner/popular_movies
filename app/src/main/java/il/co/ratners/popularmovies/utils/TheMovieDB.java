package il.co.ratners.popularmovies.utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import il.co.ratners.popularmovies.BuildConfig;

/**
 * Utility class to assist with The Movie DB Interaction
 */

public class TheMovieDB {

    /* TODO: consider getting and parsing TMDB configuration */
    private static final String IMAGES_URL = "https://image.tmdb.org/t/p/";
    private static final String DEFAULT_IMAGE_SIZE = "w185";
    public static final String API_KEY = BuildConfig.API_KEY;

    public static final String SORT_BY_POPULARITY = "popular";
    public static final String SORT_BY_RATING = "top_rated";
    public static final String DEFAULT_SORT_ORDER = SORT_BY_POPULARITY;

    private static String sImageSize = DEFAULT_IMAGE_SIZE;


    public static String getMovieImageURL(String imagePath) {
        return IMAGES_URL+"/"+sImageSize+"/"+imagePath;
    }



    /**
     * This method returns the entire result from the HTTP response.
     *
     * source: copy-pasta from Udacity Sunshine exercises.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response, null if no response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream in = urlConnection.getInputStream();
        Scanner scanner = new Scanner(in);

        scanner.useDelimiter("\\A");
        boolean hasInput = scanner.hasNext();
        String response = null;
        if (hasInput) {
            response = scanner.next();
        }
        scanner.close();

        urlConnection.disconnect();
        return response;
    }

}
