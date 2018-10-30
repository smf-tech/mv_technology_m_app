package com.sujalamsufalam.Widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by nanostuffs on 09-03-2018.
 */

class FontManager {

        private static final String ROOT = "fonts/";
    public static final String FONTAWESOME = ROOT + "fa-solid-900.ttf";

        public static Typeface getTypeface(Context context, String font) {
            return Typeface.createFromAsset(context.getAssets(), font);
        }
    private static void markAsIconContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(child,typeface);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
        }
    }
    }
