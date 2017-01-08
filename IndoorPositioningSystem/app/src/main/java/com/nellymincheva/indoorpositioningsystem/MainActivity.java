package com.nellymincheva.indoorpositioningsystem;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
<<<<<<< HEAD
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
=======
>>>>>>> parent of b72542a... Signal data displayed & other stuff
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Messenger mBeaconsService = null;
    boolean mBeaconsBound;
<<<<<<< HEAD
    private static int RESULT_LOAD_IMG = 1;
    final int REQUEST_ENABLE_BT = 2;
    String imgDecodableString;

    int roomWidthInPixels = 0;
    final double[] roomScale = {1};
    SearchView searchView;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;


=======
>>>>>>> parent of b72542a... Signal data displayed & other stuff
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

<<<<<<< HEAD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
=======
    public void sayHello(View v) {
        if (!mBeaconsBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, UserPositionService.MSG_SAY_HELLO, 0, 0);
        try {
            mBeaconsService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int REQUEST_ENABLE_BT=1;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
>>>>>>> parent of b72542a... Signal data displayed & other stuff

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
<<<<<<< HEAD

        GridView grid = (GridView) findViewById(R.id.grid_view);
        grid.setNumColumns(15);
        grid.setNumRows(10);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        Button addVenueButton = (Button) findViewById(R.id.add_venue_button);
        addVenueButton.setOnClickListener(this);

        TextView LG = (TextView) findViewById(R.id.user_logged_in);
        if(mAuth.getCurrentUser() != null){
            LG.setText(mAuth.getCurrentUser().getEmail());
        }
        else{
            LG.setText("not logged in");
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
                    DrawBeacons();
                }
            });
        }
        final TextView textView = (TextView) findViewById(R.id.userPositionInfo);
        final TextView beacon1Info = (TextView) findViewById(R.id.beacon1Info);
        final TextView beacon2Info = (TextView) findViewById(R.id.beacon2Info);
        final TextView beacon3Info = (TextView) findViewById(R.id.beacon3Info);
        final TextView beacon4Info = (TextView) findViewById(R.id.beacon4Info);
=======
        RelativeLayout room = (RelativeLayout) findViewById(R.id.room);
        float roomWidth = 10;
        float roomHeight = 20;
        //room.getMeasuredWidth();
        //ViewGroup.LayoutParams params = room.getLayoutParams();
// Changes the height and width to the specified *pixels*
        //params.width = room.getHeight();//(int)((params.width/Math.min(roomHeight,roomWidth)) * Math.max(roomWidth, roomHeight));

        /*
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("obeacon1Mac","C9:35:A9:B1:84:9D");
        editor.putFloat("obeacon1X",1);
        editor.putFloat("obeacon1Y",3.5f);
        editor.putString("obeacon2Mac","E0:62:12:B9:F3:BE");
        editor.putFloat("obeacon2X",2.9f);
        editor.putFloat("obeacon2Y",1);
        editor.putString("obeacon3Mac","DD:12:B2:90:39:48");
        editor.putFloat("obeacon3X",3.1f);
        editor.putFloat("obeacon3Y",3.5f);
        editor.putFloat("oroomWidth",10);
        editor.putFloat("oroomHeight",15);

        editor.commit();
        */
        final TextView textView = (TextView) findViewById(R.id.main_activity_text_view);
>>>>>>> parent of b72542a... Signal data displayed & other stuff
        final ImageView userImg = (ImageView) findViewById(R.id.user_icon);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
<<<<<<< HEAD
                        double userX = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_X, 0);
                        double userY = intent.getDoubleExtra(PositioningService.EXTRA_USER_POSITION_Y, 0);
                        double signal1 = intent.getDoubleExtra(PositioningService.EXTRA_SIGNAL_1, 0);
                        double signal2 = intent.getDoubleExtra(PositioningService.EXTRA_SIGNAL_2, 0);
                        double signal3 = intent.getDoubleExtra(PositioningService.EXTRA_SIGNAL_3, 0);
                        double signal4 = intent.getDoubleExtra(PositioningService.EXTRA_SIGNAL_4, 0);
                        textView.setText("User position: (" + (double) Math.round(userX * 100) / 100 + "; " + (double) Math.round(userY * 100) / 100 + ") ");
                        beacon1Info.setText("Beacon 1 RSSI: " + signal1 + "dBm");
                        beacon2Info.setText("Beacon 2 RSSI: " + signal2 + "dBm");
                        beacon3Info.setText("Beacon 3 RSSI: " + signal3 + "dBm");
                        beacon4Info.setText("Beacon 4 RSSI: " + signal4 + "dBm");
                        userImg.setX(((float) userX * (float) roomScale[0]));
                        userImg.setY(((float) userY * (float) roomScale[0]));
=======
                        double userX = intent.getDoubleExtra(UserPositionService.EXTRA_USER_POSITION_X, 0);
                        double userY = intent.getDoubleExtra(UserPositionService.EXTRA_USER_POSITION_Y, 0);
                        textView.setText("x: " + userX + ", y: " + userY);
                        userImg.setX(((float) userX*150));
                        userImg.setY(((float) userY*150));
>>>>>>> parent of b72542a... Signal data displayed & other stuff

                    }
                }, new IntentFilter(PositioningService.ACTION_USER_POSITION_BROADCAST)
        );

        Button drawButton = (Button) findViewById(R.id.btn_draw);
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sayHello(v);
                float radius = 2;

                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                paint.setStyle(Paint.Style.STROKE);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, PositioningService.class), mConnection,
                Context.BIND_AUTO_CREATE);

<<<<<<< HEAD
        DrawBeacons();
    }

    private void DrawBeacons() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final ImageView beacon1Img = (ImageView) findViewById(R.id.beacon1);
        final ImageView beacon2Img = (ImageView) findViewById(R.id.beacon2);
        final ImageView beacon3Img = (ImageView) findViewById(R.id.beacon3);
        final ImageView beacon4Img = (ImageView) findViewById(R.id.beacon4);

        beacon1Img.setX(Float.parseFloat(sharedPreferences.getString("beacon1X", "0")) * (float) roomScale[0]);
        beacon1Img.setY(Float.parseFloat(sharedPreferences.getString("beacon1Y", "0")) * (float) roomScale[0]);
        beacon2Img.setX(Float.parseFloat(sharedPreferences.getString("beacon2X", "0")) * (float) roomScale[0]);
        beacon2Img.setY(Float.parseFloat(sharedPreferences.getString("beacon2Y", "0")) * (float) roomScale[0]);
        beacon3Img.setX(Float.parseFloat(sharedPreferences.getString("beacon3X", "0")) * (float) roomScale[0]);
        beacon3Img.setY(Float.parseFloat(sharedPreferences.getString("beacon3Y", "0")) * (float) roomScale[0]);
        beacon4Img.setX(Float.parseFloat(sharedPreferences.getString("beacon4X", "0")) * (float) roomScale[0]);
        beacon4Img.setY(Float.parseFloat(sharedPreferences.getString("beacon4Y", "0")) * (float) roomScale[0]);
    }

    @Override
    protected void onResume() {

        if (roomWidthInPixels != 0) {

            final RelativeLayout room = (RelativeLayout) findViewById(R.id.room);
            final ViewGroup.LayoutParams roomParams = room.getLayoutParams();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            final double roomWidth = Float.parseFloat(sharedPreferences.getString("roomWidth", "1"));
            final double roomHeight = Float.parseFloat(sharedPreferences.getString("roomHeight", "1"));
            roomScale[0] = roomWidthInPixels / roomWidth;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    roomWidthInPixels, (int) (roomHeight * roomScale[0]));
            room.setLayoutParams(params);
            DrawBeacons();
        }
        super.onResume();
    }

=======
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final ImageView beacon1Img = (ImageView) findViewById(R.id.beacon1);
        final ImageView beacon2Img = (ImageView) findViewById(R.id.beacon2);
        final ImageView beacon3Img = (ImageView) findViewById(R.id.beacon3);

        beacon1Img.setX(Float.parseFloat(sharedPreferences.getString("beacon1X", "0"))*150);
        beacon1Img.setY(Float.parseFloat(sharedPreferences.getString("beacon1Y", "0"))*150);
        beacon2Img.setX(Float.parseFloat(sharedPreferences.getString("beacon2X", "0"))*150);
        beacon2Img.setY(Float.parseFloat(sharedPreferences.getString("beacon2Y", "0"))*150);
        beacon3Img.setX(Float.parseFloat(sharedPreferences.getString("beacon3X", "0"))*150);
        beacon3Img.setY(Float.parseFloat(sharedPreferences.getString("beacon3Y", "0"))*150);

        //RelativeLayout room = (RelativeLayout) findViewById(R.id.room);
        //room.getMeasuredWidth();
       // ViewGroup.LayoutParams params = room.getLayoutParams();
// Changes the height and width to the specified *pixels*
       // params.width = room.getHeight();//(int)((params.width/Math.min(roomHeight,roomWidth)) * Math.max(roomWidth, roomHeight));

    }

>>>>>>> parent of b72542a... Signal data displayed & other stuff
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBeaconsBound) {
            unbindService(mConnection);
            mBeaconsBound = false;
        }
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
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
<<<<<<< HEAD
            case R.id.action_change_map:

                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
// Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                return true;
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

    public void loadImageGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = (ImageView) findViewById(R.id.imgView);
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e + "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    @Override
    public void onDestroy() {
        if (mConnection != null) {
            unbindService(mConnection);
        }
        super.onDestroy();

    }
=======
            }
        return true;
    }

>>>>>>> parent of b72542a... Signal data displayed & other stuff

    private void SwitchLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
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
                return;
        }
    }
}
