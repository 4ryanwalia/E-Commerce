package com.nmims.zepto;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class ZeptoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
