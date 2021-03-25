package com.warrennovasconeslashwhisper.phoneapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;

import com.warrennovasconeslashwhisper.api.Verify;

public class MainActivity extends AppCompatActivity {
    public static final int RECORD_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Verify.verifyPhoneApp();
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},RECORD_CODE);
    }
}