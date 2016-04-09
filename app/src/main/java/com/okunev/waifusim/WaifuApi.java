package com.okunev.waifusim;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by gwa on 4/6/16.
 */
public interface WaifuApi {
    @GET("/api/v1/waifu/messages")
    Call<WaifuMessages> loadMessages();
}
