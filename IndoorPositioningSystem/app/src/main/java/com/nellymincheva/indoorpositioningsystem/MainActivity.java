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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Messenger mBeaconsService = null;
    boolean mBeaconsBound;
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

        SwitchLanguage("bg");
        setContentView(R.layout.activity_main);
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
        final ImageView userImg = (ImageView) findViewById(R.id.user_icon);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double userX = intent.getDoubleExtra(UserPositionService.EXTRA_USER_POSITION_X, 0);
                        double userY = intent.getDoubleExtra(UserPositionService.EXTRA_USER_POSITION_Y, 0);
                        textView.setText("x: " + userX + ", y: " + userY);
                        userImg.setX(((float) userX*150));
                        userImg.setY(((float) userY*150));

                    }
                }, new IntentFilter(UserPositionService.ACTION_USER_POSITION_BROADCAST)
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
        bindService(new Intent(this, UserPositionService.class), mConnection,
                Context.BIND_AUTO_CREATE);

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
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
        return true;
    }


    private void SwitchLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }
}
