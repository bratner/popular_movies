package il.co.ratners.popularmovies.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import il.co.ratners.popularmovies.BuildConfig;
import il.co.ratners.popularmovies.utils.PreferenceUtils;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;

public class MovieDBConnector implements MovieDBApi.MovieDBFunctions {

    private static final HttpLoggingInterceptor.Level HTTP_LOG_LEVEL = BODY;
    public static final String API_KEY = BuildConfig.API_KEY;

    private final Retrofit mRetrofit;
    private final MovieDBApi.MovieDBFunctions mApi;
    private final Context mContext;

    public MovieDBConnector(Context context) {
        
        mContext = context;

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HTTP_LOG_LEVEL);
        okHttpBuilder.addInterceptor(new AddApiKeyInterceptor());
        okHttpBuilder.addInterceptor(httpLoggingInterceptor);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-mm-dd")
                .create();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(MovieDBApi.API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpBuilder.build())
                .build();

        mApi = mRetrofit.create(MovieDBApi.MovieDBFunctions.class);
    }


    @Override
    public Call<MovieDBApi.MovieDBList> popular_movies(Integer page, String language) {
        return mApi.popular_movies(page, language);
    }

    @Override
    public Call<MovieDBApi.MovieDBList> top_rated(Integer page, String language) {
        return mApi.top_rated(page, language);
    }

    @Override
    public Call<MovieDBApi.MovieDBItem> getMovieDetails(Integer id) {
        return mApi.getMovieDetails(id);
    }

    @Override
    public Call<MovieDBApi.MovieDBVideoList> getMovieVideos(Integer id) {
        return mApi.getMovieVideos(id);
    }

    @Override
    public Call<MovieDBApi.MovieDBReviewList> getMovieReviews(Integer id) {
        return mApi.getMovieReviews(id);
    }

    /* Adds API key to every request sent using this connector */
    static class AddApiKeyInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            HttpUrl originalUrl = originalRequest.url();

            HttpUrl newRequestUrl = originalUrl.newBuilder().addQueryParameter("api_key", API_KEY).build();
            Request newRequest = originalRequest.newBuilder().url(newRequestUrl).build();
            return chain.proceed(newRequest);
        }
    }

}
