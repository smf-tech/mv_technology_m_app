package com.mv.Service;

import android.app.IntentService;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.mv.Activity.LoginActivity;
import com.mv.Activity.ProcessListActivity;
import com.mv.Model.LocationModel;
import com.mv.Model.Task;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nanostuffs on 30-10-2017.
 */

public class LocationService extends IntentService {
    public static Boolean locationLoaded;

    private PreferenceHelper preferenceHelper;
    List<LocationModel> locationModelArrayList=new ArrayList<>();
    public LocationService() {
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        // Gets data from the incoming Intent
        locationModelArrayList= AppDatabase.getAppDatabase(getApplicationContext()).userDao().getLocation();
        if(locationModelArrayList.size()==0)
        callLoationApi();

    }

    private void callLoationApi() {

        preferenceHelper = new PreferenceHelper(getApplicationContext());
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getAllLocation?StateName=" + User.getCurrentUser(getApplicationContext()).getState() + "&DistrictName=" + User.getCurrentUser(getApplicationContext()).getDistrict();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                Gson gson = new Gson();
                try {
                    String data = response.body().string();
                    //    Type listType = new TypeToken<ArrayList<LocationModel>>() {}.getType();
                    JSONArray arrayData=new JSONArray(data);
                   locationModelArrayList=new ArrayList<>();

                    for (int i=0;i<arrayData.length();i++)
                    {
                        LocationModel locationModel=new LocationModel();
                        JSONObject object=arrayData.getJSONObject(i);
                        locationModel.setState(object.getString("State"));
                        locationModel.setDistrict(object.getString("District"));
                        locationModel.setTaluka(object.getString("Taluka"));
                        locationModel.setCluster(object.getString("Cluster"));
                        locationModel.setSchoolName(object.getString("SchoolName"));
                        locationModel.setSchoolCode(object.getString("SchoolName"));
                        locationModel.setVillage(object.getString("Village"));
                        locationModel.setCreatedDate(object.getString("createdDate"));
                        locationModel.setId(object.getString("Id"));
                        locationModelArrayList.add(locationModel);

                    }

                    AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertLoaction(locationModelArrayList);
                    String Abc;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
            }
        });
    }
}