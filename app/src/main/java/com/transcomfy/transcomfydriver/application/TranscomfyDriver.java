package com.transcomfy.transcomfydriver.application;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class TranscomfyDriver extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
