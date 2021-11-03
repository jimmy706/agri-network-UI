package com.agrinetwork.helpers;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class UrlHelper <T>{
    public String convertFromMapToQueryString(Map<String, T> map) {
        List<String> result = new ArrayList<>();

        map.entrySet().stream().forEach(entry -> {
            String key = entry.getKey();
            T value = entry.getValue();
            try {
                result.add(key + "=" + URLEncoder.encode(value.toString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        return String.join("&", result);
    }
}
