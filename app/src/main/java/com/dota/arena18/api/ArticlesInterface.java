package com.dota.arena18.api;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by TheGamer007 on 11/1/18.
 */

public interface ArticlesInterface {
    @GET("news")
    Call<ArrayList<ArticleDetails>> getArticlesList(@Query("fields") String fields);

    @GET("news/{id}")
    Call<ArticleDetails> getArticle(@Path("id") String id);
}
