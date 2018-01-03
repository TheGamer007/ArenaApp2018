package com.dota.arena18.api;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by TheGamer007 on 2/1/18.
 */

public interface ScoresInterface {

    @GET("leaderboard")
    Call<ArrayList<CollegeDetails>> getLeaderboard();
}