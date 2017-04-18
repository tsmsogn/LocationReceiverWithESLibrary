package com.tsmsogn.locationreceiverwitheslibrary;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ubhave.dataformatter.DataFormatter;
import com.ubhave.dataformatter.json.JSONFormatter;
import com.ubhave.datahandler.except.DataHandlerException;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.SensorDataListener;
import com.ubhave.sensormanager.config.pull.LocationConfig;
import com.ubhave.sensormanager.config.pull.PullSensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.pull.LocationData;
import com.ubhave.sensormanager.sensors.SensorUtils;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = "MainActivity";
    private JSONFormatter formatter;
    private EditText editText;
    private Button subscibeButton;
    private Button unsubscribeButton;
    private ESSensorManager sm;
    private LocationSensorDataListener listener;
    private boolean isSensing = false;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText1);
        subscibeButton = (Button) findViewById(R.id.button1);
        unsubscribeButton = (Button) findViewById(R.id.button2);

        subscibeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
            }
        });
        unsubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsubscribe();
            }
        });

        formatter = DataFormatter.getJSONFormatter(this, SensorUtils.SENSOR_TYPE_LOCATION);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            initialize();
        }
    }

    private void subscribe() {
        if (!isSensing) {
            try {
                id = sm.subscribeToSensorData(SensorUtils.SENSOR_TYPE_LOCATION, listener);
                isSensing = true;
            } catch (ESException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Task is already running.", Toast.LENGTH_SHORT).show();
        }
    }

    private void unsubscribe() {
        if (isSensing) {
            try {
                sm.unsubscribeFromSensorData(id);
                isSensing = false;
            } catch (ESException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Task is not running.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initialize() {
        try {
            sm = ESSensorManager.getSensorManager(this);
            sm.setSensorConfig(SensorUtils.SENSOR_TYPE_LOCATION, LocationConfig.ACCURACY_TYPE, LocationConfig.LOCATION_ACCURACY_FINE);
            sm.setSensorConfig(SensorUtils.SENSOR_TYPE_LOCATION, PullSensorConfig.SENSE_WINDOW_LENGTH_MILLIS, 1000L);
            sm.setSensorConfig(SensorUtils.SENSOR_TYPE_LOCATION, PullSensorConfig.POST_SENSE_SLEEP_LENGTH_MILLIS, 1000L);
            listener = new LocationSensorDataListener();
        } catch (ESException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initialize();
                }
            }
        }
    }

    private class LocationSensorDataListener implements SensorDataListener {
        private static final String TAG = "LocationSensorDataListener";

        @Override
        public void onDataSensed(SensorData sensorData) {
            Log.d(TAG, "onDataSensed");
            LocationData locationData = (LocationData) sensorData;
            updateUI(locationData);
        }

        @Override
        public void onCrossingLowBatteryThreshold(boolean b) {

        }
    }

    private void updateUI(final LocationData locationData) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    editText.setText(formatter.toString(locationData));
                } catch (DataHandlerException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
