package com.example.onecall.network;


import com.example.onecall.models.ModelData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface ApiService {
    @POST("users/{userId}/location.json")
    Call<Void> sendLocation(@Path("userId") String userId, @Body ModelData location);
}