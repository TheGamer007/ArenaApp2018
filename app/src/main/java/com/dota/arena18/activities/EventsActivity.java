package com.dota.arena18.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dota.arena18.R;
import com.dota.arena18.api.EventsInterface;
import com.dota.arena18.api.ApiClient;
import com.dota.arena18.api.EventDetails;
import com.dota.arena18.database.Model;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.dota.arena18.activities.AboutActivity.REQUEST_LOCATION;

public class EventsActivity extends AppCompatActivity implements FoldingCellListAdapter.ListItemClickListener {
    /**
     * The EventsActivity lists all the sporting EventDetails scheduled to happen during the fest.
     * Each event will have its own DetailsActivity to display information.
     *
     */


    ArrayList<EventDetails> list = new ArrayList<>();
    ArrayList<Model> realmlist = new ArrayList<>();
    FoldingCellListAdapter adapter;
    RecyclerView theRecyclerView;
    private Realm myrealm;
    public Model model = new Model();
    int id;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    startActivity(new Intent(this, MapsActivity.class));
                } else {
                    // Permissions not granted; display reasoning, or deactivate feature
                }
                break;
            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        theRecyclerView = (RecyclerView) findViewById(R.id.mainRecyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        myrealm = Realm.getDefaultInstance();
        adapter = new FoldingCellListAdapter(EventsActivity.this,realmlist,this);
        theRecyclerView.setAdapter(adapter);

        theRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        callapi();

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        callapi();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

     }

     public void callapi()
     {
         id=0;

         EventsInterface apiservice  = ApiClient.getClient().create(EventsInterface.class);
         Call<ArrayList<EventDetails>> call = apiservice.getEvents();
         call.enqueue(new Callback<ArrayList<EventDetails>>() {
             @Override
             public void onResponse(Call<ArrayList<EventDetails>> call, Response<ArrayList<EventDetails>> response) {
                 list = response.body();

                   for(int i=0;i<list.size();i++)
                     {
                         model.setApi_id(list.get(i).getApi_id());
                         model.setDb_eventname(list.get(i).getEventname());
                         model.setDb_rules("");
                         model.setDb_prizemoney("0");
                         adddatatorealm(model);
                     }

                 adapter.notifyDataSetChanged();
             }

             @Override
             public void onFailure(Call<ArrayList<EventDetails>> call, Throwable t) {
                 Log.e(EventsActivity.class.getSimpleName(),"not connected to internet");
                 getdatafromrealm(myrealm);
             }
         });

     }

     public void getdatafromrealm(Realm realm1) {

        RealmResults<Model> results = realm1.where(Model.class).findAll();
         for (int i = 0; i < results.size(); i++) {
             realm1.beginTransaction();
             results.get(i).setId(id);
             realm1.commitTransaction();
             if((realmlist.size()-1)<id)
             {realmlist.add(id,results.get(i));}
             else
             {
                 realmlist.set(id,results.get(i));
             }
             id++;
         }
         Log.e(EventsActivity.class.getSimpleName(),String.valueOf(results.size()));

         adapter.notifyDataSetChanged();
         if(results.size()==0)
         {
             AlertDialog.Builder alertdailog = new AlertDialog.Builder(this);
             alertdailog.setMessage("Please check your network connection")
                     .setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     dialogInterface.cancel();
                 }
             }).create().show();
         }

     }

    private void adddatatorealm(Model mymodel)
       {
        myrealm.beginTransaction();
        Model checkmodel = myrealm.where(Model.class).equalTo("id",id).findFirst();
        if(checkmodel==null) {
        Model event = myrealm.createObject(Model.class);
        event.setId(id);
        event.setDb_eventname(mymodel.getDb_eventname());
        event.setDb_rules(mymodel.getDb_rules());
        event.setDb_prizemoney(mymodel.getDb_prizemoney());
        event.setApi_id(mymodel.getApi_id());
        realmlist.add(event);}

        else{
            checkmodel.setDb_eventname(mymodel.getDb_eventname());
            checkmodel.setDb_prizemoney(mymodel.getDb_prizemoney());
            checkmodel.setDb_rules(mymodel.getDb_rules());
            checkmodel.setApi_id(mymodel.getApi_id());
            if((realmlist.size()-1)<id)
            {realmlist.add(id,checkmodel);}
            else
            {
                realmlist.set(id,checkmodel);
            }
        }

        myrealm.commitTransaction();
        id++;
        adapter.notifyDataSetChanged();
       }

    @Override
    public void OnListItemClick(int clickedItemIndex,View view) {
        ((FoldingCell) view).toggle(false);
        adapter.registerToggle(clickedItemIndex);
    }
}
