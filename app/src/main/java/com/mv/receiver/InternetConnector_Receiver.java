package com.mv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Content;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rohit Gujar on 01-11-2017.
 */

public class InternetConnector_Receiver extends BroadcastReceiver {
    private static boolean firstConnect = true;
    private static int cnt = 0;

    public InternetConnector_Receiver() {

    }

    private PreferenceHelper preference;
    private Context mContext;
    private List<Content> contentList;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            preference = new PreferenceHelper(context);
            mContext = context;
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager != null ? connectivityManager
                    .getActiveNetworkInfo() : null;
            // Check internet connection and accrding to state change the
            // text of activity by calling method
            if (networkInfo != null) {

                if (networkInfo.isConnected()) {
                    if (firstConnect) {
                        firstConnect = false;

                        if (preference.getBoolean(PreferenceHelper.CONTENTISSYNCHED)) {
                            sendContent();
                        }
                    } else {
                        firstConnect = true;
                    }
                } else {
                    firstConnect = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendContent() {
        try {

            contentList = AppDatabase.getAppDatabase(mContext).userDao().getAllASynchChats();
            if (contentList.size() == 0) {
                return;
            }
            preference.insertBoolean(PreferenceHelper.CONTENTISSYNCHED, false);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < contentList.size(); i++) {
                String json = gson.toJson(contentList.get(i));
                JSONObject jsonObject1 = new JSONObject(json);
                JSONArray jsonArrayAttchment = new JSONArray();
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image" + "/" + contentList.get(i).getAttachmentId() + ".png");
                if (file.exists()) {
                    Uri FinalUri = Uri.fromFile(file);
                    if (FinalUri != null) {
                        try {

                            InputStream iStream = null;
                            iStream = mContext.getContentResolver().openInputStream(FinalUri);
                            String img_str = Base64.encodeToString(Utills.getBytes(iStream), 0);
                            JSONObject jsonObjectAttachment = new JSONObject();
                            jsonObjectAttachment.put("Body", img_str);
                            jsonObjectAttachment.put("Name", contentList.get(i).getTitle());
                            jsonObjectAttachment.put("ContentType", "image/png");
                            jsonArrayAttchment.put(jsonObjectAttachment);
                        } catch (FileNotFoundException e) {
                            preference.insertBoolean(PreferenceHelper.CONTENTISSYNCHED, true);
                            e.printStackTrace();
                        } catch (IOException e) {
                            preference.insertBoolean(PreferenceHelper.CONTENTISSYNCHED, true);
                            e.printStackTrace();
                        }
                    }
                }
                jsonObject1.put("attachments", jsonArrayAttchment);
                jsonArray.put(jsonObject1);
            }
            jsonObject.put("listVisitsData", jsonArray);
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
            apiService.sendDataToSalesforce(preference.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/insertContent", gsonObject).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        preference.insertBoolean(PreferenceHelper.CONTENTISSYNCHED, true);
                        String str = response.body().string();
                        JSONObject object = new JSONObject(str);
                        JSONArray array = object.getJSONArray("Records");
                        for (int i = 0; i < array.length(); i++) {
                            if (i < contentList.size()) {
                                JSONObject object1 = array.getJSONObject(i);
                                if (object1.has("Id")) {
                                    contentList.get(i).setId(object1.getString("Id"));
                                }
                                if (object1.has("attachmentId")) {
                                    contentList.get(i).setAttachmentId(object1.getString("attachmentId"));
                                }
                                if (object1.has("CreatedDate")) {
                                    contentList.get(i).setTime(object1.getString("CreatedDate"));
                                }
                                contentList.get(i).setSynchStatus(null);
                                AppDatabase.getAppDatabase(mContext).userDao().updateContent(contentList.get(i));
                            }
                        }

                    } catch (Exception e) {

                        preference.insertBoolean(PreferenceHelper.CONTENTISSYNCHED, true);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    preference.insertBoolean(PreferenceHelper.CONTENTISSYNCHED, true);
                }
            });
            Log.i("JSON data", jsonObject.toString());
        } catch (JSONException e) {
            preference.insertBoolean(PreferenceHelper.CONTENTISSYNCHED, true);
            e.printStackTrace();
        }
    }


}
