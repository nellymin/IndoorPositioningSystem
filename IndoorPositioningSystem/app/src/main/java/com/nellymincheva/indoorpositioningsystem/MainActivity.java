package com.nellymincheva.indoorpositioningsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static int RESULT_LOAD_IMG = 1;
    final int REQUEST_ENABLE_BT = 2;
    String imgDecodableString;

    int roomWidthInPixels = 0;
    final double[] roomScale = {1};
    SearchView searchView;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private DatabaseReference mDatabase;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        Button addVenueButton = (Button) findViewById(R.id.add_venue_button);
        addVenueButton.setOnClickListener(this);


        venues = new ArrayList<Venue>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        recyclerView = (RecyclerView)findViewById(R.id.venues_list);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                getAllVenues(dataSnapshot);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                getAllVenues(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                getAllVenues(dataSnapshot);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Button showVenueButton = (Button) findViewById(R.id.show_venue_button);
        showVenueButton.setOnClickListener(this);
        if(mAuth.getCurrentUser() == null){
            addVenueButton.setVisibility(View.INVISIBLE);
        }

        TextView LG = (TextView) findViewById(R.id.user_logged_in);
        if(mAuth.getCurrentUser() != null){
            LG.setText("Welcome, " + mAuth.getCurrentUser().getEmail());
        }
        else{
            LG.setText("Welcome, guest. To create indoor maps login first");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        activateSearch(menu);
        inflater.inflate(R.menu.main_menu, menu);
        if(mUser == null){
            menu.findItem(R.id.action_login).setVisible(true);
            menu.findItem(R.id.action_logout).setVisible(false);
        }
        else{
            menu.findItem(R.id.action_login).setVisible(false);
            menu.findItem(R.id.action_logout).setVisible(true);
        }
        return true;
    }

    private void activateSearch(Menu menu){
        /*
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, MainActivity.class)));
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                if (mUser == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                }
                return true;
            case R.id.action_logout:
                if (mUser != null) {
                    mAuth.signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                }
                return true;
        }
        return true;
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_venue_button:
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(this, "You should Log in to add a venue", Toast.LENGTH_LONG).show();
                    return;
                }
                startActivity(new Intent(this, AddVenueActivity.class));
                break;
            case R.id.show_venue_button:
                Intent intent = new Intent(MainActivity.this, VenueActivity.class);
                Bundle b = new Bundle();
                b.putString("userUid", "4S7PzPby91fFM4CCEeySVITs74C3");
                b.putString("venueId", "-K_us39u5_Xy_i69101K");
                intent.putExtras(b);
                startActivity(intent);
                break;
        }
    }

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerViewAdapter recyclerViewAdapter;
    private DatabaseReference databaseReference;
    private List<Venue> venues;
    private void getAllVenues(DataSnapshot dataSnapshot){
        for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
            Venue v = (Venue) new Gson().fromJson(new JSONObject((Map)singleSnapshot.getValue()).toString(), Venue.class);
            v.venueId = singleSnapshot.getKey();
            venues.add(v);
            recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, venues);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
    }
}
