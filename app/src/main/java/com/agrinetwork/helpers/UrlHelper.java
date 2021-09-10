package com.agrinetwork.helpers;

import android.os.Build;

import androidx.annotation.RequiresApi;

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
            result.add(key + "=" + value.toString());
        });

        return String.join("&", result);
    }
}
