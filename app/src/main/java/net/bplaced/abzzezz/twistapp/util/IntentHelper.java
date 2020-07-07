/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util;

import java.util.Hashtable;

/**
 * Curtsy of https://stackoverflow.com/users/917553/roger-sanoli
 * Basically a hashmap holder.
 */
public class IntentHelper {

    private static IntentHelper _instance;
    private final Hashtable<String, Object> _hash;

    private IntentHelper() {
        _hash = new Hashtable<>();
    }

    private static IntentHelper getInstance() {
        if (_instance == null) {
            _instance = new IntentHelper();
        }
        return _instance;
    }

    public static void addObjectForKey(Object object, String key) {
        getInstance()._hash.put(key, object);
    }

    public static Object getObjectForKey(String key) {
        IntentHelper helper = getInstance();
        Object data = helper._hash.get(key);
        helper._hash.remove(key);
        return data;
    }
}