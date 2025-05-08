package com.example.csp;

import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {
    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "drxajyngt");
            config.put("api_key", "278383696121239");
            config.put("api_secret", "kx_cOM8GXjcawAm--rbyZAATkU8");
            config.put("secure", "true");
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }

}

