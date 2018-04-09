package il.co.ratners.popularmovies.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import il.co.ratners.popularmovies.utils.PreferenceUtils;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;

public class MovieDBConnector implements MovieDBApi.MovieDBFunctions {

    private static final HttpLoggingInterceptor.Level HTTP_LOG_LEVEL = BODY;

    private final Retrofit mRetrofit;
    private final MovieDBApi.MovieDBFunctions mApi;
    private final Context mContext;

    public MovieDBConnector(Context context) {
        
        mContext = context;

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HTTP_LOG_LEVEL);
        okHttpBuilder.addInterceptor(new MovieDBApi.AddApiKeyInterceptor());
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
    public Call<MovieDBApi.MovieDBItem> getMovieVideos(Integer id) {
        return mApi.getMovieVideos(id);
    }

    @Override
    public Call<MovieDBApi.MovieDBItem> getMovieReviews(Integer id) {
        return getMovieReviews(id);
    }

}
