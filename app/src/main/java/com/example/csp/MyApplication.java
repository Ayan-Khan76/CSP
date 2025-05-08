package com.example.csp;

import android.app.Application;
import com.example.csp.CloudinaryConfig;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CloudinaryConfig.init(this);
    }
}
