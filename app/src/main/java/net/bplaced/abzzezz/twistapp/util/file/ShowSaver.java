/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.file;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Saves all shows to a preference list
 */
public class ShowSaver {

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public ShowSaver(final Context context) {
        this.preferences = context.getSharedPreferences("ShowList", Context.MODE_PRIVATE);
        this.editor = preferences.edit();
    }

    /**
     * Add show
     *
     * @param inf json object containing data
     */
    public void addShow(final JSONObject inf) {
        editor.putString(String.valueOf(preferences.getAll().size()), inf.toString());
        editor.commit();
    }

    /**
     * Remove show at given index
     *
     * @param index position to remove
     */
    public void removeShow(final int index) {
        //Remove key (int)
        editor.remove(String.valueOf(index));
        /*
        Move all upcoming entries one down
         */
        for (int i = index; i < preferences.getAll().size() - /*One gone */ 1; i++) {
            editor.putString(String.valueOf(i), preferences.getString(String.valueOf(i + /* Next one */ 1), "NULL"));
            editor.remove(String.valueOf(i + 1));
        }
        //Apply to file
        editor.apply();
    }

    /**
     * Check if any id matches @param show
     *
     * @param show id to match
     * @return matches?
     */
    public boolean containsShow(final String show) {
        return preferences.getAll().values().stream().anyMatch(o -> {
            try {
                return new JSONObject(o.toString()).getString("id").equals(show);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Get new array instance of all values
     *
     * @return new array
     */
    public ArrayList<String> getList() {
        return new ArrayList<>((Collection<? extends String>) preferences.getAll().values());
    }

    /**
     * Update entry value
     * @param index index of entry
     * @param key key to be updated
     * @param value value to update it to
     */
    public void updateEntry(final int index, final String key, final String value) {
        final String indexString = String.valueOf(index);
        try {
            final JSONObject entry = new JSONObject(preferences.getString(indexString, ""));
            editor.remove(indexString);
            entry.put(key, value);
            editor.putString(indexString, entry.toString());
            editor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get details from index
     *
     * @param index entry position
     * @return JSON containing show details
     */
    public JSONObject getShowDetails(final int index) {
        try {
            return new JSONObject(preferences.getString(String.valueOf(index), "-1"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
