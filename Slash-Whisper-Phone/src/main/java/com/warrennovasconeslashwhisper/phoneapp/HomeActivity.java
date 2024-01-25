package com.warrennovasconeslashwhisper.phoneapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnLocationCameraTransitionListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.Image;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.warrennovasconeslashwhisper.api.Verify;
import com.warrennovasconeslashwhisper.api.viewmodels.UserViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HomeActivity extends ActivityWithUser {
    public static final int RECORD_CODE = 1;
    MapView mapView;
    MarkerView userMarker;
    Uri imageUri;
    TextView sensorT, data;
    SensorManager mySensorManager;
    Sensor mySensor;
    SensorEvent event;

//    https://javapapers.com/android/android-proximity-sensor-example-app/ how I figured out the sensor worked




    private boolean talking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_home);
        sensorT = findViewById(R.id.sensor);
        data = findViewById(R.id.data);
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (mySensor == null) {
            sensorT.setText("No Proximity Sensor!");
        } else {
            mySensorManager.registerListener(sensorEventListener, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorT.setText("Chatters Near!");

        }



        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_CODE);
        EditText contact = findViewById(R.id.contact);
        EditText message = findViewById(R.id.message);
        TextView locationTV = findViewById(R.id.location);
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);





        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    MarkerViewManager markerViewManager = new MarkerViewManager(mapView, mapboxMap);
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RECORD_CODE);
                        }

                        LocationComponent locationComponent = mapboxMap.getLocationComponent();

                        locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(HomeActivity.this, style)
                                .useSpecializedLocationLayer(true)
                                .build()
                        );

                        locationComponent.setLocationComponentEnabled(true);
                        locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH, new OnLocationCameraTransitionListener() {
                            @Override
                            public void onLocationCameraTransitionFinished(int cameraMode) {
                                locationComponent.zoomWhileTracking(17,100);
                            }

                            @Override
                            public void onLocationCameraTransitionCanceled(int cameraMode) {

                            }
                        });

                        LocationComponentOptions options = locationComponent.getLocationComponentOptions().toBuilder()
                                .trackingGesturesManagement(true)
                                .trackingInitialMoveThreshold(500)
                                .build();

                        locationComponent.applyStyle(options);
                        MarkerViewManager markerViewManager = new MarkerViewManager(mapView,mapboxMap);

                        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                locationTV.setText(String.format("Location: %f by %f\nAltitude: %f\n", location.getLatitude(), location.getLongitude(), location.getAltitude()));
                                onSensorChanged(location.getLatitude(), location.getLongitude(),  markerViewManager);
                                data.setText("Location Changed");
//                                if(userMarker == null) {
//
//                                    locationTV.setText(String.format("Location: %f by %f\nAltitude: %f\n", location.getLatitude(), location.getLongitude(), location.getAltitude()));
//                                    TextView textView = new TextView(HomeActivity.this);
//                                    textView.setText("You are here");
//                                    userMarker = new MarkerView(new LatLng(location.getLatitude(), location.getLongitude()), textView);
//                                    markerViewManager.addMarker(userMarker);
//                                } else {
//                                    locationTV.setText(String.format("Location: %f by %f\nAltitude: %f\n", location.getLatitude(), location.getLongitude(), location.getAltitude()));
//                                    userMarker.setLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
//                                }
                            }
                        });
                    }
                });
            }
        });


        viewModel.getUser().observe(this,(user) ->{
            if(user != null)
            {
                viewModel.storeUserSpecificData();
            }
        });

        findViewById(R.id.logout_button).setOnClickListener((view) -> {
            viewModel.signOut();
        });

//        findViewById(R.id.cam).setOnClickListener((view)->{
//            ContentResolver resolver = getContentResolver();
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            ContentValues values = new ContentValues();
//            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "my_image_" +timeStamp+".jpg");
//            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
//            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
//
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
//            startActivityForResult(intent,0);
//
//        });

        SpeechRecognizer recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {
                System.out.println("Error: " + error);

            }

            @Override
            public void onResults(Bundle results) {

                ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String text = result.get(0);
                System.out.println(text);

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> result = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String text = result.get(0);
                System.out.println(text);


            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        recognizer.startListening(recognizerIntent);

        findViewById(R.id.speech).setOnClickListener((view) -> {
            if(!talking)
            {
                recognizer.startListening(recognizerIntent);
                talking = true;
            }else{
                talking = false;
                recognizer.stopListening();
            }
        });

        findViewById(R.id.send).setOnClickListener((view) -> {
            String contactToSend = contact.getText().toString();
            String messageToSend = message.getText().toString();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(intent.EXTRA_EMAIL, new String[]{contactToSend});
            intent.putExtra(Intent.EXTRA_TEXT, messageToSend);
            intent.setType("message/rfc822");

            startActivity(Intent.createChooser(intent, "Contact"));
        });


    }


    public void onSensorChanged(Double lat, Double lon, MarkerViewManager markerViewManager) {

                MarkerView markerView = new MarkerView(new LatLng(lat,lon),  mapView);
                markerViewManager.addMarker(markerView);

        }



    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 0) {
                    data.setText("Chatters Near");
                } else {
                    data.setText("No Chatters");
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == Activity.RESULT_OK){
            ((ImageView)findViewById(R.id.imageView)).setImageURI(imageUri);
        }
    }
}