package com.sujalamsufalam.Service;

/**
 * Created by Rohit Gujar on 05-12-2017.
 */

import android.app.IntentService;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sujalamsufalam.Model.Attendance;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.ApiClient;
import com.sujalamsufalam.Retrofit.AppDatabase;
import com.sujalamsufalam.Retrofit.ServiceRequest;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendAttendance extends IntentService {
    private PreferenceHelper preferenceHelper;
    private Attendance mAttendance;

    public SendAttendance() {
        super("Send Attendance Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        preferenceHelper = new PreferenceHelper(getApplicationContext());
        mAttendance = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getUnSynchAttendance();
        sendAttendanceToserver();
    }

    private void sendAttendanceToserver() {
        if (Utills.isConnected(this)) {
            try {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(mAttendance);
                JSONObject object = new JSONObject();
                JSONObject jsonObject = new JSONObject(json);
                object.put("att", jsonObject);
                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(object.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/saveAttendance", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.body() != null) {
                                if (response != null && response.isSuccess()) {
                                    String data = response.body().string();
                                    if (data != null && data.length() > 0) {
                                        JSONObject object = new JSONObject(data);
                                        if (object.has("Status")) {
                                            if (object.getString("Status").equalsIgnoreCase("done")) {
                                                AppDatabase.getAppDatabase(getApplicationContext()).userDao().deleteAttendance(mAttendance);
                                                JSONObject object1 = object.getJSONObject("Records");
                                                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                                Attendance attendance1 = gson.fromJson(object1.toString(), Attendance.class);
                                                AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertAttendance(attendance1);
                                                mAttendance = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getUnSynchAttendance();
                                                if (mAttendance != null)
                                                    sendAttendanceToserver();
                                            } else if(object.getString("Status").equalsIgnoreCase("Already checked In")){
                                                mAttendance.setSynch("true");
                                                AppDatabase.getAppDatabase(getApplicationContext()).userDao().updateAttendance(mAttendance);
                                                mAttendance = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getUnSynchAttendance();
                                                if (mAttendance != null)
                                                    sendAttendanceToserver();
                                            } else if(object.getString("Status").equalsIgnoreCase("Already Checked Out")){
                                                mAttendance.setSynch("true");
                                                AppDatabase.getAppDatabase(getApplicationContext()).userDao().updateAttendance(mAttendance);
                                                mAttendance = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getUnSynchAttendance();
                                                if (mAttendance != null)
                                                    sendAttendanceToserver();
                                            }else {
                                                Utills.showToast(object.getString("Status"), getApplicationContext());
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
            }
        }
    }

}
