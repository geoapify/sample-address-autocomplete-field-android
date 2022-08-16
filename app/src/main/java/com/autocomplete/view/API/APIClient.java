package com.autocomplete.view.API;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    // You use Geoapify Address Autocomplete API. Learn more about the API on https://www.geoapify.com/address-autocomplete/
    // Learn more about the API parameters on https://apidocs.geoapify.com/docs/geocoding/address-autocomplete/
    public static String BASE_URL = "https://api.geoapify.com/v1/geocode/autocomplete/";

    // Sign up on https://myprojects.geoapify.com/ to generate an API key
    // 1. Create a project; 2. Go to the Keys tab; 3. Replace "YOUR_API_KEY" with your API key
    public static String API_KEY = "YOUR_API_KEY";

    public static Retrofit retrofit = null;
    public static Retrofit getClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(180, TimeUnit.SECONDS)
                .connectTimeout(180, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }
}
