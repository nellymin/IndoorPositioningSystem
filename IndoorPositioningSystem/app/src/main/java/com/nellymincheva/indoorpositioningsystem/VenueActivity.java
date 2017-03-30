package com.nellymincheva.indoorpositioningsystem;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Map;

public class VenueActivity extends AppCompatActivity {
    private static final boolean AUTO_HIDE = true;

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private DatabaseReference mDatabase;
    public Venue venue;
    private String venueId, userUid;

    Messenger mBeaconsService = null;
    boolean mBeaconsBound;
    int roomWidthInPixels = 0 ;
    final double[] roomScale = {1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_venue);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        Bundle b = getIntent().getExtras();
        venueId = "";
        userUid = "";
        if (b != null) {
            venueId = b.getString("venueId");
            userUid = b.getString("userUid");
            Log.wtf("opa", venueId + "");

        } else {
            Intent intent = new Intent(VenueActivity.this, MainActivity.class);
            startActivity(intent);
        }


        final RelativeLayout room = (RelativeLayout) findViewById(R.id.room);
        final ViewGroup.LayoutParams roomParams = room.getLayoutParams();
        ViewTreeObserver viewTreeObserver = room.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    room.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    roomWidthInPixels = room.getMeasuredWidth();
                }
            });
        }
        final ImageView userImgEuclideanDistance = (ImageView) findViewById(R.id.user_icon_euclidean_distance);
        final ImageView userImgNearestSieve = (ImageView) findViewById(R.id.user_icon_nearest_sieve);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double userXEucledeanDistance= intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_X_EUCLIDEAN_DISTANCE, 0);
                        double userYEucledeanDistance= intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_Y_EUCLIDEAN_DISTANCE, 0);
                        userImgEuclideanDistance.setY(((float) userXEucledeanDistance*(float)roomScale[0] + (float)roomScale[0]/2));
                        userImgEuclideanDistance.setX(((float) userYEucledeanDistance*(float)roomScale[0] + (float)roomScale[0]/2));
                        double userXNearestSieve = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_X_NEAREST_SIEVE, 0);
                        double userYNearestSieve = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_Y_NEAREST_SIEVE, 0);
                        userImgNearestSieve.setY(((float) userXNearestSieve*(float)roomScale[0] + (float)roomScale[0]/2)-10);
                        userImgNearestSieve.setX(((float) userYNearestSieve*(float)roomScale[0] + (float)roomScale[0]/2)-10);
                        TextView data = (TextView) findViewById(R.id.data);
                        //data.setText(userX + " " + userY + " " + userX / userY + " * " + userImg.getX() + " " + userImg.getY() + " " + userImg.getX() / userImg.getY());
                        displayVenue();

                    }
                }, new IntentFilter(PositioningService.ACTION_USER_POSITION_BROADCAST)
        );

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBeaconsService = new Messenger(service);
            mBeaconsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mBeaconsService = null;
            mBeaconsBound = false;
        }
    };


    @Override
    protected void onStart() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        super.onStart();
        final ImageView userImgEuclideanDistance = (ImageView) findViewById(R.id.user_icon_euclidean_distance);
        final ImageView userImgNearestSieve = (ImageView) findViewById(R.id.user_icon_nearest_sieve);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double userXEucledeanDistance= intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_X_EUCLIDEAN_DISTANCE, 0);
                        double userYEucledeanDistance= intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_Y_EUCLIDEAN_DISTANCE, 0);
                        userImgEuclideanDistance.setY(((float) userXEucledeanDistance*(float)roomScale[0] + (float)roomScale[0]/2));
                        userImgEuclideanDistance.setX(((float) userYEucledeanDistance*(float)roomScale[0] + (float)roomScale[0]/2));
                        double userXNearestSieve = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_X_NEAREST_SIEVE, 0);
                        double userYNearestSieve = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_Y_NEAREST_SIEVE, 0);
                        userImgNearestSieve.setY(((float) userXNearestSieve*(float)roomScale[0] + (float)roomScale[0]/2)-10);
                        userImgNearestSieve.setX(((float) userYNearestSieve*(float)roomScale[0] + (float)roomScale[0]/2)-10);
                        TextView data = (TextView) findViewById(R.id.data);
                        //data.setText(userX + " " + userY + " " + userX / userY + " * " + userImg.getX() + " " + userImg.getY() + " " + userImg.getX() / userImg.getY());
                        displayVenue();

                    }
                }, new IntentFilter(PositioningService.ACTION_USER_POSITION_BROADCAST)
        );

        final Intent positioningServiceIntent = new Intent(this, PositioningService.class);
        Query q = mDatabase.child("venues/" + venueId);
        q.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                venue = (Venue) new Gson().fromJson(new JSONObject((Map)snapshot.getValue()).toString(), Venue.class);

                String venueGson = new Gson().toJson(venue);
                Log.wtf("zdre", venueGson);

                positioningServiceIntent.putExtra("venue", venueGson);
                bindService(positioningServiceIntent, mConnection,
                        Context.BIND_AUTO_CREATE);
                getSupportActionBar().setTitle("" + venue.name);
                displayVenue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    public void displayVenue() {
        GridView grid = (GridView) findViewById(R.id.grid_view);
        grid.setNumColumns(venue.maxX);
        grid.setNumRows(venue.maxY);
        final RelativeLayout room = (RelativeLayout) findViewById(R.id.room);
        final ViewGroup.LayoutParams roomParams = room.getLayoutParams();
        roomScale[0] = room.getMeasuredWidth() / venue.width;
        roomParams.height = (int) (venue.height * roomScale[0]);
    }

    @Override
    public void onDestroy() {
        if (mConnection != null) {
            unbindService(mConnection);
        }
        super.onDestroy();

    }

    @SuppressLint("InlinedApi")
    private void show() {
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
