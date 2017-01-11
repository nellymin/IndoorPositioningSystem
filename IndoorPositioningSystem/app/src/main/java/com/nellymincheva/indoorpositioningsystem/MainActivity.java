package com.nellymincheva.indoorpositioningsystem;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static int RESULT_LOAD_IMG = 1;
    final int REQUEST_ENABLE_BT = 2;
    String imgDecodableString;

    int roomWidthInPixels = 0;
    final double[] roomScale = {1};
    SearchView searchView;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        Button addVenueButton = (Button) findViewById(R.id.add_venue_button);
        addVenueButton.setOnClickListener(this);

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
            LG.setText("You are not logged in");
        }

/*
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
        final ImageView userImg = (ImageView) findViewById(R.id.user_icon);
*/
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {

        if (roomWidthInPixels != 0) {
/*
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
        */}
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
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
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
}
