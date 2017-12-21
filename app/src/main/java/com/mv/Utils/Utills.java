package com.mv.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mv.Model.Task;
import com.mv.R;
import com.mv.Service.MyJobService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by acer on 5/18/2017.
 */

public class Utills {

    private static Dialog pgDialog;

    public static void setupUI(View view, final Activity activity) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    /**
     * get date for API calls
     *
     * @return
     */
    public static String getDateForAPI() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }

    }

    public static void showToast(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * method to get string from list
     *
     * @param mList
     * @return
     */
    public static String getStringFromList(List<?> mList) {
        String result = mList.toString();
        result = result.replace('[', ' ');
        result = result.replace(']', ' ');
        return result.trim();
    }

    public static void showDateDialog(final EditText text, Context context) {


        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        text.setText(getTwoDigit(dayOfMonth) + "/" + getTwoDigit(monthOfYear + 1) + "/" + year);


                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    public static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
    }

    /**
     * method to show progress dialog
     *
     * @param cntxt
     */
    public static void showProgressDialog(Context cntxt) {
        if (pgDialog == null) {
            pgDialog = new Dialog(cntxt);
            pgDialog.setContentView(R.layout.custome_progress_dialog);
            pgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            TextView text = (TextView) pgDialog.findViewById(R.id.tv_progress);
            text.setText(cntxt.getString(R.string.progress_please_wait));
            ImageView proImg = (ImageView) pgDialog.findViewById(R.id.img_progress);
            proImg.setBackgroundResource(R.drawable.progress_dialog);
            AnimationDrawable rocketAnimation = (AnimationDrawable) proImg.getBackground();
            rocketAnimation = (AnimationDrawable) proImg.getBackground();
            rocketAnimation.start();
            pgDialog.setCancelable(false);
            pgDialog.show();
            Window window = pgDialog.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }

    }


    public static void makedirs(String Dir) {
        File tempdir = new File(Dir);
        if (!tempdir.exists())
            tempdir.mkdirs();
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static void showProgressDialog(Context cntxt, String Msg, String Title) {
        if (pgDialog == null) {
            pgDialog = new Dialog(cntxt);
            pgDialog.setContentView(R.layout.custome_progress_dialog);
            pgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            TextView text = (TextView) pgDialog.findViewById(R.id.tv_progress);
            text.setText(Msg);
            TextView tv_desc = (TextView) pgDialog.findViewById(R.id.tv_desc);
            if (Title != null && !TextUtils.isEmpty(Title))
                tv_desc.setVisibility(View.VISIBLE);
            tv_desc.setText(Title);
            ImageView proImg = (ImageView) pgDialog.findViewById(R.id.img_progress);
            proImg.setBackgroundResource(R.drawable.progress_dialog);

            AnimationDrawable rocketAnimation = (AnimationDrawable) proImg.getBackground();
            rocketAnimation = (AnimationDrawable) proImg.getBackground();
            rocketAnimation.start();
            pgDialog.setCancelable(false);
            pgDialog.show();
            Window window = pgDialog.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }

    }

    public static void openActivity(Activity source, Class<?> destination) {
        Intent openClass = new Intent(source, destination);
        source.startActivity(openClass);
        source.overridePendingTransition(R.anim.right_in, R.anim.left_out);
        /*if (Util.isLollipop()) {
            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(source, view, mTransitionName);
            source.startActivity(openClass, transitionActivityOptions.toBundle());
        } else {

        }*/
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    /*
      method to hide progress dialog
     */


    public static void hideProgressDialog() {
        if (pgDialog != null) {
            if (pgDialog.isShowing()) {
                pgDialog.dismiss();
                pgDialog = null;
            }
        }
    }

    /**
     * method to check if internet is connected
     *
     * @return
     */


    public static void scheduleJob(Context context)
    {

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(MyJobService.class)

                // uniquely identifies the job
                .setTag("my-unique-tag")
                // one-off job
                .setRecurring(true)
                // don't persist past a device reboot
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                // start between 0 and 60 seconds from now
                .setTrigger(Trigger.executionWindow(0, 120))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)

                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                .setConstraints(
                        // only run on an unmetered network
                        Constraint.ON_UNMETERED_NETWORK,
                        // only run when the device is charging
                        Constraint.DEVICE_CHARGING
                )
                .build();

        dispatcher.mustSchedule(myJob);
    }
    public static boolean isConnected(Context cntxt) {
        NetworkInfo activeNetwork = getNetworkInfo(cntxt);
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    public static void showInternetPopUp(final Context context) {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setCancelable(true);
        alertDialog.setMessage(context.getString(R.string.error_no_internet));
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setButton(context.getString(R.string.Setting), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);

                context.startActivity(settingsIntent);
            }
        });
        alertDialog.setButton2(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }


    /**
     * method to check if internet connectivity is fast or slow
     *
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static String convertArrayListToString(ArrayList<Task> arrayList) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(arrayList);

    }

    public static ArrayList<Task> convertStringToArrayList(String jsonstring) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Task>>() {
        }.getType();
        return gson.fromJson(jsonstring, listType);
    }

    public static void saveUriToPath(Context context, Uri uri, File file) {

        final int chunkSize = 1024;  // We'll read in one kB at a time
        byte[] imageData = new byte[chunkSize];

        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(file);  // I'm assuming you already have the File object for where you're writing to

            int bytesRead;
            while ((bytesRead = in.read(imageData)) > 0) {
                out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
            }

        } catch (Exception ex) {
            Log.e("Something went wrong.", ex.getMessage());
            ex.printStackTrace();
        }
    }
}
