package com.academiaexpress.Server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiEndpoints {
    @GET("orders")
    Call<JsonArray> getOrders(@Query("api_token") String token);

    @GET("working_hours")
    Call<JsonArray> getWorkingHours(@Query("api_token") String token);

    @GET("payment_cards")
    Call<JsonArray> getCards(@Query("api_token") String token);

    @GET("stuff")
    Call<JsonArray> getStuff(@Query("api_token") String token);

    @GET("edge_points")
    Call<JsonArray> getEdgePoints(@Query("api_token") String token);

    @GET("day")
    Call<JsonObject> getDay(@Query("api_token") String token);

    @GET("categories/{id}/dishes")
    Call<JsonArray> getDishes(@Path("id") int id, @Query("api_token") String token);

    @GET("categories")
    Call<JsonArray> getCategories(@Query("api_token") String token);

    @GET("user")
    Call<JsonObject> getUser(@Query("api_token") String token);

    @PATCH("user")
    Call<JsonObject> updateUser(@Body JsonObject user, @Query("api_token") String token);

    @PATCH("orders/{id}")
    Call<JsonObject> note(@Body JsonObject user, @Path("id") int id, @Query("api_token") String token);

    @POST("user")
    Call<JsonObject> createUser(@Body JsonObject user);

    @POST("orders/{id}/payments")
    Call<JsonObject> pay(@Body JsonObject payment, @Path("id") String id, @Query("api_token") String token);

    @POST("orders")
    Call<JsonObject> sendOrder(@Body JsonObject order, @Query("api_token") String token);

    @POST("payment_cards")
    Call<JsonObject> createCard(@Query("api_token") String token);

    @POST("verification_tokens")
    Call<JsonObject> authWithPhoneNumber(@Query("phone_number") String phone);

    @PATCH("verification_tokens/{id}")
    Call<JsonObject> verify(@Path("id") String id, @Query("code") String code);
}
