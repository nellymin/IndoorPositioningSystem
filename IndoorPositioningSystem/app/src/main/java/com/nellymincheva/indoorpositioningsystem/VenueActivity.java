package com.nellymincheva.indoorpositioningsystem;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VenueActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
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
            // Delayed display of UI elements
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
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
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

        // Set up the user interaction to manually show or hide the system UI.
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final double roomWidth = Float.parseFloat(sharedPreferences.getString("roomWidth", "1"));
        final double roomHeight = Float.parseFloat(sharedPreferences.getString("roomHeight", "1"));
        ViewTreeObserver viewTreeObserver = room.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    room.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    roomWidthInPixels = room.getMeasuredWidth();
                    roomScale[0] = room.getMeasuredWidth() / roomWidth;
                    roomParams.height = (int) (roomHeight * roomScale[0]);
                }
            });
        }
        final ImageView userImg = (ImageView) findViewById(R.id.user_icon);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double userX = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_X, 0);
                        double userY = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_Y, 0);
                        userImg.setX(((float) userX*(float)roomScale[0]));
                        userImg.setY(((float) userY*(float)roomScale[0]));

                    }
                }, new IntentFilter(PositioningService.ACTION_USER_POSITION_BROADCAST)
        );

    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mBeaconsService = new Messenger(service);
            mBeaconsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mBeaconsService = null;
            mBeaconsBound = false;
        }
    };


    @Override
    protected void onStart() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        super.onStart();
        // Bind to the service
        Bundle b = new Bundle();
        venue = new Venue();
        venue.name = "PATKAN";
        List<PositionRecord> pr = new ArrayList<>();
        PositionRecord rec = new PositionRecord(0,0);
        double rssi = -30;
        rec.AddRecord("EE:86:9C:E0:19:F9", rssi);
        venue.maxX=2;
        venue.maxY=2;
        pr.add(rec);
        rec = new PositionRecord(0,1);
        rssi = -60;
        String[] bec = new String[1];
        bec[0]  ="EE:86:9C:E0:19:F9";
        venue.setBeacons(bec);
        rec.AddRecord("EE:86:9C:E0:19:F9", rssi);
        pr.add(rec);
        venue.setCalibrationData(pr);
        Intent positioningServiceIntent = new Intent(this, PositioningService.class);
        String venueGson = new Gson().toJson(venue);
        positioningServiceIntent.putExtra("venue", venueGson);
        bindService(positioningServiceIntent, mConnection,
                Context.BIND_AUTO_CREATE);

        ImageView userIcon = (ImageView) findViewById(R.id.user_icon);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double userX = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_X, 0);
                        double userY = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_Y, 0);

                    }
                }, new IntentFilter(PositioningService.ACTION_USER_POSITION_BROADCAST)
        );

        Query q = mDatabase.child("venues/" + venueId);
        q.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                venue =  (Venue) new Gson().fromJson(snapshot.getValue().toString(), Venue.class);
                getSupportActionBar().setTitle("" + venue.name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
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
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    public void displayVenue() {
        /*
        GridView grid = (GridView) findViewById(R.id.grid_view);
        grid.setNumColumns(venue.maxX);
        grid.setNumRows(venue.maxY);
        */
        PositioningService.venue = venue;
        Log.wtf("opa", venue.name);
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
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
