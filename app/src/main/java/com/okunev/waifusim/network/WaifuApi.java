package com.okunev.waifusim.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by CherryPerry on 15.04.2016.
 */
public final class WaifuApi {

    private static final WaifuApiInterface instance;

    public static WaifuApiInterface api() {
        return instance;
    }

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WaifuApiInterface.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .validateEagerly(true)
                .build();
        instance = retrofit.create(WaifuApiInterface.class);
    }
}
