package com.okunev.waifusim;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by gwa on 4/6/16.
 */
public interface WaifuApi {
    @GET("/api/v1/waifu/messages")
    Call<WaifuMessages> loadMessages();
}
