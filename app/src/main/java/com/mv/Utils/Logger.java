package com.mv.Utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by User on 6/1/2017.
 */

public class Logger {

    public static void doToast(String message, Context cntxt){
        Toast.makeText(cntxt, message, Toast.LENGTH_SHORT).show();
    }
}
