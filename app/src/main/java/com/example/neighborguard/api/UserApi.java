package com.example.neighborguard.api;

import com.example.neighborguard.model.NewUser;
import com.example.neighborguard.model.SearchUsersResponseSchema;
import com.example.neighborguard.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;



public interface UserApi {
    @GET("/users")
    Call<SearchUsersResponseSchema> getUsers(
            @Query("email") String email,
            @Query("role") String role,
            @Query("filterByLat") Double filterByLat,
            @Query("filterByLon") Double filterByLon,
            @Query("isRequiredAssistance") boolean isRequiredAssistance
    );

    @GET("/users/recipients")
    Call<SearchUsersResponseSchema> getNearbyRecipients(
            @Query("volunteerUID") String volunteerUID,
            @Query("filterByLat") Double filterByLat,
            @Query("filterByLon") Double filterByLon
    );

    @POST("/user")
    Call<User> createUser(@Body NewUser newUser);

    @GET("/user/{email}")
    Call<User> getUserByEmail(
            @Path("email") String email
    );

    @PUT("/user/{uid}")
    Call<Void> updateUser(
            @Path("uid") String uid,
            @Body User user
    );

}