package me.dmitvitalii.cosmos;

import android.content.Context;

import static me.dmitvitalii.cosmos.Actions.EPIC;

/**
 * @author Vitalii Dmitriev
 * @since 21.03.2017
 */
public abstract class PreferenceUtil {
    private static final String NAME = "CosmosWidget";
    private static final String KEY_VISIBILITY = "visible";
    private static final String KEY_PROJECT = "proj";

    private PreferenceUtil() { /* NOP */ }

    public static void setVisible(Context context, boolean visible) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_VISIBILITY, visible)
                .apply();
    }

    public static boolean isVisible(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_VISIBILITY, true);
    }

    public static boolean isChosen(Context context, String key) {
        return getChosen(context).equals(key);
    }

    public static void choose(Context context, String key) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PROJECT, key)
                .apply();
    }

    public static String getChosen(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getString(KEY_PROJECT, EPIC);
    }
}
