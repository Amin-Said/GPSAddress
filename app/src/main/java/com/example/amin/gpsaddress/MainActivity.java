package com.example.amin.gpsaddress;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.amin.gpsaddress.helper.LocationAddress;
import com.example.amin.gpsaddress.helper.LocationResult;
import com.example.amin.gpsaddress.helper.MyLocation;

public class MainActivity extends AppCompatActivity implements LocationResult {

    TextView address;
    Button getLocation, showAddress;
    private MyLocation myLocation = null;


    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int INITIAL_REQUEST = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        address = findViewById(R.id.address);
        myLocation = new MyLocation();


        getLocation = findViewById(R.id.get_location_btn);

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean networkPresent = myLocation.getLocation(MainActivity.this, MainActivity.this);
                if (!networkPresent) {
                    showSettingsAlert();
                }
            }

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case INITIAL_REQUEST:
                if (canAccessLocation() && canAccessCoreLocation()) {
                    boolean networkPresent = myLocation.getLocation(MainActivity.this, this);
                    if (!networkPresent) {
                        showSettingsAlert();
                    }
                }

                break;

        }
    }

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean canAccessCoreLocation() {
        return (hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    private boolean hasPermission(String perm) {

        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(MainActivity.this, perm));
    }


    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(
                MainActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MainActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void gotLocation(Location location) {

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final String result = "Latitude: " + location.getLatitude() +
                " Longitude: " + location.getLongitude();


        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                address.setText(result);
                LocationAddress locationAddress = new LocationAddress();
                locationAddress.getAddressFromLocation(latitude, longitude,
                        getApplicationContext(), new GeocoderHandler());
            }
        });


    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            address.setText(locationAddress);
        }
    }


}
