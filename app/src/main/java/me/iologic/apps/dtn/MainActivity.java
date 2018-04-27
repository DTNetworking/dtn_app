package me.iologic.apps.dtn;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private boolean permissionToRecordAccepted = false;
    private boolean permissionToAccessLocationAccepted = false;
    private boolean permissionToReadWriteStorageAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AskForLocation();
        AskForRWStorage();
        AskForAudioRecordingPermission();
        setBtDiscovery();
    }

    public void AskForLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.Permissions.REQUEST_LOCATION_PERMISSION);
        }
    }

    public void AskForRWStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.Permissions.REQUEST_READ_WRITE_STORAGE);
        }
    }

    public void AskForAudioRecordingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, Constants.Permissions.REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    public void setBtDiscovery() {
        // Make Device Discoverable
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    public void openOneScenario(View view) {
        if ((permissionToReadWriteStorageAccepted) && (permissionToAccessLocationAccepted) && (permissionToRecordAccepted)) {
            Intent intent = new Intent(this, OneScenario.class);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Please accept all permissions to continue. Restart the application.", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            switch (requestCode) {
                case Constants.Permissions.REQUEST_RECORD_AUDIO_PERMISSION:
                    permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    Log.i(Constants.TAG, "permissionToRecordAccepted: " + grantResults[0]);
                    break;
                case Constants.Permissions.REQUEST_LOCATION_PERMISSION:
                    permissionToAccessLocationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    Log.i(Constants.TAG, "permissionToAccessLocationAccepted: " + grantResults[0]);
                    break;
                case Constants.Permissions.REQUEST_READ_WRITE_STORAGE:
                    permissionToReadWriteStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    Log.i(Constants.TAG, "permissionToReadWriteStorageAccepted: " + grantResults[0]);
                    break;
            }

            if (!permissionToRecordAccepted) {
                Toast.makeText(getApplicationContext(), "Permission of Audio Recording is not accepted by the user", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Permission of Audio Recording is accepted by the user", Toast.LENGTH_SHORT).show();
            }

            if (!permissionToAccessLocationAccepted) {
                Toast.makeText(getApplicationContext(), "Permission of Location is not accepted by the user", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Permission of Location is accepted by the user", Toast.LENGTH_SHORT).show();
            }

            if (!permissionToReadWriteStorageAccepted) {
                Toast.makeText(getApplicationContext(), "Permission of Read/Write Storage is not accepted by the user", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Permission of Read/Write Storage is accepted by the user", Toast.LENGTH_SHORT).show();
            }
        }
    }

   /* public void sendMessage(View view){
        object.connectDevice();
    } */
}
