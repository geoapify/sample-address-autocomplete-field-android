package com.autocomplete.view.API;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface APIInterface {
    @GET
    public Call<ResponseBody> getLocationresult(@Url String url);
}
