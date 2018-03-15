package il.co.ratners.popularmovies.utils;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import il.co.ratners.popularmovies.BuildConfig;

/**
 * Created by bratner on 3/4/18.
 */

public class TheMovieDB {

    /* TODO: consider getting and parsing TMDB configuration */
    public static final String IMAGES_URL = "https://image.tmdb.org/t/p/";
    public static final String DEFAULT_IMAGE_SIZE = "w185";
    public static final String API_KEY = BuildConfig.API_KEY;

    public static String sImageSize = DEFAULT_IMAGE_SIZE;


    public static String getMovieImageURL(String imagePath) {

        String ret = IMAGES_URL+"/"+sImageSize+"/"+imagePath;

        return ret;

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

}
