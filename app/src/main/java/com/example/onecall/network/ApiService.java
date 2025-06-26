package com.example.onecall.network;

import com.example.onecall.models.UserDetails;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface ApiService {

    // Get user details by ID
    @GET("user/{id}")
    Call<UserDetails> getUserById(@Path("id") String userId);

    // Post new user details
    @POST("user")
    Call<UserDetails> createUser(@Body UserDetails userDetails);
}