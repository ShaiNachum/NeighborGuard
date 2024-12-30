package com.example.neighborguard.api;

import com.example.neighborguard.model.ExtendedUser;
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
            @Query("toExtendMeeting") boolean toExtendMeeting,
            @Query("role") String role,
            @Query("filterByLat") Double filterByLat,
            @Query("filterByLon") Double filterByLon,
            @Query("isRequiredAssistance") boolean isRequiredAssistance
    );

    @GET("/users/recipients")  // new 25/12 endpoint for getting nearby recipients
    Call<SearchUsersResponseSchema> getNearbyRecipients(
            @Query("volunteerUID") String volunteerUID,
            @Query("toExtendMeeting") boolean toExtendMeeting,
            @Query("filterByLat") Double filterByLat,
            @Query("filterByLon") Double filterByLon
    );

    @POST("/user")
    Call<User> createUser(@Body NewUser newUser);

    @GET("/user/{email}")  // new 25/12 endpoint for getting user by email
    Call<ExtendedUser> getUserByEmail(
            @Path("email") String email,
            @Query("toExtendMeeting") boolean toExtendMeeting
    );

    @PUT("user/{uid}")
    Call<Void> updateUser(
            @Path("uid") String uid,
            @Body User user
    );

}