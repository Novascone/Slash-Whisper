package com.warrennovasconeslashwhisper.phoneapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.warrennovasconeslashwhisper.api.Verify;
import com.warrennovasconeslashwhisper.api.viewmodels.UserViewModel;

import java.util.ArrayList;

public class HomeActivity extends ActivityWithUser {
    public static final int RECORD_CODE = 1;


    private boolean talking = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewModel.getUser().observe(this,(user) ->{
            if(user != null)
            {
                viewModel.storeUserSpecificData();
            }
        });
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},RECORD_CODE);
        findViewById(R.id.logout_button).setOnClickListener((view) -> {
            viewModel.signOut();
        });

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
    }

}