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
    @POST("/users")
    Call<User> createUser(@Body NewUser newUser);

    @GET("/users")
    Call<SearchUsersResponseSchema> findUser(
            @Query("email") String email,
            @Query("toExtendMeeting") boolean toExtendMeeting,
            @Query("role") String role,
            @Query("filterByLat") Double filterByLat,
            @Query("filterByLon") Double filterByLon,
            @Query("isRequiredAssistance") boolean isRequiredAssistance
    );;

    @PUT("users/{uid}")
    Call<Void> updateUser(
            @Path("uid") String uid,
            @Body User user
    );

}














//    @GET("admin/users")
//    Call<User[]> getAllUsers(
//            @Query("userSuperapp") String userSuperapp,
//            @Query("userEmail") String userEmail,
//            @Query("size") int size,
//            @Query("page") int page
//    );