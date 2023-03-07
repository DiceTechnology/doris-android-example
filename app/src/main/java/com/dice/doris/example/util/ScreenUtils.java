package com.dice.doris.example.util;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Collection of screen related utility methods.
 */
public class ScreenUtils {
    /**
     * Check whether the device is in portrait orientation currently.
     *
     * @return Whether the current orientation is portrait.
     */
    public static boolean isPortrait(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
