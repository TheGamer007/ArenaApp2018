package com.dota.arena18.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.dota.arena18.R;
import com.dota.arena18.api.ScoresFeed;
import com.dota.arena18.api.ScoresFeedResponse;
import com.dota.arena18.api.ScoresInterface;
import com.dota.arena18.api.TestApiClient;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ashwik on 23-01-2018.
 */

public class ScoresFeedActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private List<ScoresFeed> list = new ArrayList<>();
    private ScoresFeedAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private  String TAG = ScoresFeedActivity.class.getSimpleName();
    private RecyclerView.OnScrollListener scrollListener;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean loading = true;
    LinearLayoutManager layoutManager;
    private int page=1;
    private int totalpages = -1;
    int num=1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoresfeed);

        ActionBar actionBar =getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
        }

        swipeRefreshLayout = findViewById(R.id.feed_refresh);
        recyclerView = findViewById(R.id.feed_recyclerview);
        adapter = new ScoresFeedAdapter(list,this);
        layoutManager = new LinearLayoutManager(this);

        getdatafromApi();

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                loading = true;
                page =1;
                if(list.size()!=0)
                {
                    list = new ArrayList<>();
                }
                getdatafromApi();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        scrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0) //check for scroll down
                {

                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading)
                    {
                        swipeRefreshLayout.setRefreshing(true);
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {

                            Log.v("...", "Last Item Wow !");
                            page++;
                            getdatafromApi();
                            Log.e(TAG,"current page:"+String.valueOf(page));

                        }

                    }
                    if(page == totalpages)
                    {
                        loading = false;
                    }
                }
            }
        };

       recyclerView.addOnScrollListener(scrollListener);

    }

     private  void getdatafromApi()
     {
         ScoresInterface apiservice = TestApiClient.getClient().create(ScoresInterface.class);
         Call<ScoresFeedResponse> call = apiservice.getScoresfeed(page);
         call.enqueue(new Callback<ScoresFeedResponse>() {
             @Override
             public void onResponse(@NonNull Call<ScoresFeedResponse> call, @NonNull Response<ScoresFeedResponse> response) {
                 List<ScoresFeed> result = response.body().getDocs();
                   page = response.body().getPage();
                   Log.e(TAG,"page:"+String.valueOf(page));
                 totalpages = response.body().getTotalPages();
                 Log.e(TAG,"TotalPages:"+String.valueOf(totalpages));
                 for(int i=0;i<result.size();i++)
                 {
                     list.add(result.get(i));
                 }
                 adapter.notifyDataSetChanged();
                 swipeRefreshLayout.setRefreshing(false);
                 Log.e(TAG,"connected"+String.valueOf(list.size()));
             }

             @Override
             public void onFailure(@NonNull Call<ScoresFeedResponse> call, @NonNull Throwable t) {
                 Log.e(TAG,"Not connected to internet");
                 swipeRefreshLayout.setRefreshing(false);
                 new StyleableToast.Builder(ScoresFeedActivity.this)
                         .text("No Network Connection...")
                         .textColor(Color.RED)
                         .show();
             }
         });

     }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
