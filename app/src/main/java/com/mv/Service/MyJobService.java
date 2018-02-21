package com.mv.Service;

import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.User;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Nanostuffs on 13-12-2017.
 */

public class MyJobService extends JobService {
    private PreferenceHelper preferenceHelper;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;

    @Override
    public boolean onStartJob(JobParameters job) {
        // Do some work> here


        getAddress();

        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }

    private void GetMapParameters(String latitude, String longitude) {

        try {

            preferenceHelper = new PreferenceHelper(getApplicationContext());

            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("lat", latitude);
            jsonObject.put("lon", longitude);
            jsonObject.put("id", User.getCurrentUser(this).getMvUser().getId());
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/MapParameters", gsonObject).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.body() != null) {
                            String data = response.body().string();
                            if (data != null && data.length() > 0) {
                                JSONObject jsonObject = new JSONObject(data);
                                String status = jsonObject.getString("status");
                                String message = jsonObject.getString("msg");
                                if (status.equals("Success")) {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
                                    Date APICALLDATE = simpleDateFormat.parse(simpleDateFormat.format(new Date()));

                                    preferenceHelper.insetLong(PreferenceHelper.APICALLTIME,APICALLDATE.getTime());


                                } else {
                                   // Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "something went wrong", Toast.LENGTH_LONG).show();
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void getAddress() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        mFusedLocationClient.getLastLocation()

                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Log.e("location", "null");
                            return;
                        }

                        mLastLocation = location;
                       String latitude = String.valueOf(mLastLocation.getLatitude());
                        String longitude = String.valueOf(mLastLocation.getLongitude());

                        GetMapParameters(latitude, longitude);
                        if (!Geocoder.isPresent()) {
                            //Toast.makeText(getApplicationContext(),"No geocoder available",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // If the user pressed the fetch address button before we had the location,
                        // this will be set to true indicating that we should kick off the intent
                        // service after fetching the location.
                      /*  if (mAddressRequested) {
                            startIntentService();
                        }*/
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log.w(TAG, "getLastLocation:onFailure", e);
                        Log.e("fail", "unable to connect");
                    }
                });


    }


}