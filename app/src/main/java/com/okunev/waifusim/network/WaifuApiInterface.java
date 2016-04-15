package com.okunev.waifusim.network;

import com.okunev.waifusim.Token;
import com.okunev.waifusim.WaifuMessages;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by gwa on 4/6/16.
 */
public interface WaifuApiInterface {
    String URL = "http://ec2-52-38-11-210.us-west-2.compute.amazonaws.com/";

    @GET("api/v1/waifu/1/messages")
    Call<WaifuMessages> loadMessages(@Query("token") String token);

    @POST("api/v1/user")
    Call<Token> user();
}
