package com.example.neighborguard.api;

import com.example.neighborguard.model.Meeting;
import com.example.neighborguard.model.NewMeeting;
import com.example.neighborguard.model.SearchMeetingsResponseSchema;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface MeetingApi {
    @POST("/meeting")
    Call<Meeting> createMeeting(@Body NewMeeting newMeeting);

    @DELETE("/meeting/{id}")
    Call<Void> cancelMeeting(@Path("id") String meetingId);

    @GET("/meetings")
    Call<SearchMeetingsResponseSchema> getMeetings(
            @Query("userId") String userId,
            @Query("status") String status
    );

    @PUT("/meeting/{id}/status")
    Call<Meeting> updateMeetingStatus(
            @Path("id") String meetingId,
            @Query("status") String status
    );
}