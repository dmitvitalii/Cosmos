package me.dmitvitalii.cosmos;

import android.content.Context;

/**
 * @author Vitalii Dmitriev
 * @since 21.03.2017
 */
public abstract class PreferenceUtil {
    private static final String NAME = "CosmosWidget";
    private PreferenceUtil() { /* NOP */ }

    public static boolean isChosen(Context context, String key) {
         return context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(key, false);
    }

    public static void choose(Context context, String key) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(key, true)
                .apply();
    }
}
