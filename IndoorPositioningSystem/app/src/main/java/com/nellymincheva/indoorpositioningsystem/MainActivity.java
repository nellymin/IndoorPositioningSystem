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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
        SharedPreferences sharedPreferences = getSharedPreferences("test", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("beacon1Mac","C9:35:A9:B1:84:9D");
        editor.putFloat("beacon1X",2);
        editor.putFloat("beacon1Y",2);
        editor.putString("beacon2Mac","E0:62:12:B9:F3:BE");
        editor.putFloat("beacon2X",4);
        editor.putFloat("beacon2Y",1);
        editor.putString("beacon3Mac","DD:12:B2:90:39:48");
        editor.putFloat("beacon3X",4);
        editor.putFloat("beacon3Y",3);

        editor.commit();


        final ImageView beacon1Img = (ImageView) findViewById(R.id.beacon1);
        final ImageView beacon2Img = (ImageView) findViewById(R.id.beacon2);
        final ImageView beacon3Img = (ImageView) findViewById(R.id.beacon3);

        beacon1Img.setX(sharedPreferences.getFloat("beacon1X",0)*50);
        beacon1Img.setY(sharedPreferences.getFloat("beacon1Y",0)*50);
        beacon2Img.setX(sharedPreferences.getFloat("beacon2X",0)*50);
        beacon2Img.setY(sharedPreferences.getFloat("beacon2Y",0)*50);
        beacon3Img.setX(sharedPreferences.getFloat("beacon3X",0)*50);
        beacon3Img.setY(sharedPreferences.getFloat("beacon3Y",0)*50);

        final TextView textView = (TextView) findViewById(R.id.main_activity_text_view);
        final ImageView userImg = (ImageView) findViewById(R.id.user_icon);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double userX = intent.getDoubleExtra(UserPositionService.EXTRA_USER_POSITION_X, 0);
                        double userY = intent.getDoubleExtra(UserPositionService.EXTRA_USER_POSITION_Y, 0);
                        textView.setText("x: " + userX + ", y: " + userY);
                        userImg.setX(((float) userX*50));
                        userImg.setY(((float) userY*50));

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



    private void SwitchLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }
}
