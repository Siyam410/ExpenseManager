package com.example.expensemanager;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase once for the whole app
        FirebaseApp.initializeApp(this);
    }
}

