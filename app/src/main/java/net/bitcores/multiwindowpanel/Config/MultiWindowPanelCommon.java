package net.bitcores.multiwindowpanel.Config;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bitcores on 2015-06-02.
 */
public class MultiWindowPanelCommon {
    private SharedPreferences preferences;

    public static final String PREF_FILE_NAME = "settings";

    public static Boolean init = false;
    public static Boolean strict = true;
    public static Boolean skipWarning = false;
    public static List<String> launcherItems = new ArrayList<String>();

    public MultiWindowPanelCommon() {

    }

    public void initSettings(Context context) {
        if (!init) {
            preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

            strict = preferences.getBoolean("strict", true);
            skipWarning = preferences.getBoolean("skipWarning", false);
            launcherItems.clear();
            String itemString = preferences.getString("launcherItems", "");

            try {
                JSONArray itemArray = new JSONArray(itemString);

                for (int i = 0; i < itemArray.length(); i++) {
                    launcherItems.add(itemArray.getString(i));
                }
            } catch (JSONException e) {

            }

            init = true;
        }
    }

    public void saveSettings(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        JSONArray itemArray = new JSONArray(launcherItems);

        editor.putBoolean("strict", strict);
        editor.putBoolean("skipWarning", skipWarning);
        editor.putString("launcherItems", itemArray.toString());

        editor.commit();
    }
}
