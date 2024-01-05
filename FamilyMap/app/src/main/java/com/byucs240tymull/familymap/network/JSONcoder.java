package com.byucs240tymull.familymap.network;

import com.google.gson.Gson;

public class JSONcoder {

    public static String encodeJSON(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static Object decodeJSON(String json, Class classy) {
        Gson gson = new Gson();
        return gson.fromJson(json, classy);
    }
}
