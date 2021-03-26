package com.warrennovasconeslashwhisper.phoneapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;

import com.warrennovasconeslashwhisper.api.Verify;
import com.warrennovasconeslashwhisper.api.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final int RECORD_CODE = 1;
    private boolean talking = false;
//    TextToSpeech textSpeech;
    UserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        Button signIn = findViewById(R.id.signIn);
        Button signUp = findViewById(R.id.signUp);

        signIn.setOnClickListener((view) -> {
            viewModel.signIn(
                    email.getText().toString(),
                    password.getText().toString()
            );
        });

        signUp.setOnClickListener((view) -> {
            viewModel.signUp(
                    email.getText().toString(),
                    password.getText().toString()
            );
        });
        Verify.verifyPhoneApp();
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},RECORD_CODE);
//        textSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                textSpeech.setLanguage(Locale.getDefault());
//            }
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
    @Override
    protected void onStart(){
        super.onStart();
        viewModel.getUser().observe(this, (user) -> {
            System.out.println("MY USER");
            System.out.println(user);
        });
    }


}