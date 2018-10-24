package com.mv.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Model.MapUserData;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        preferenceHelper = new PreferenceHelper(this);

        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(this.getResources().getString(R.string.map));
        ImageView imgLogout = (ImageView) findViewById(R.id.img_logout);
        imgLogout.setVisibility(View.GONE);
        ImageView imgBack = (ImageView) findViewById(R.id.img_back);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //  LatLng SMF = new LatLng(18.537206, 73.829827);
//        mMap.addMarker(new MarkerOptions().position(SMF).title("SMF Offace"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(SMF));
        //Move the camera to the user's location and zoom in!
        //    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(SMF, 11.0f));
        getHolidayList();
    }

    private void getHolidayList() {
        if (Utills.isConnected(MapsActivity.this)) {
            Utills.showProgressDialog(MapsActivity.this, "Loading...", getString(R.string.progress_please_wait));
            ServiceRequest apiService = ApiClient.getClientWitHeader(MapsActivity.this).create(ServiceRequest.class);
            String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/getLocationMapData?userId=" +
                    User.getCurrentUser(getApplicationContext()).getMvUser().getId();
            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        if (response.body() != null) {
                            String data = response.body().string();
                            if (data.length() > 0) {
                                List<MapUserData> mapUserData;
                                JSONArray jsonArray = new JSONArray(data);
                                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                AppDatabase.getAppDatabase(MapsActivity.this).userDao().deleteHolidayList();
                                mapUserData = Arrays.asList(gson.fromJson(jsonArray.toString(), MapUserData[].class));

                                for (MapUserData userData : mapUserData) {
                                    if (userData.getLat() != 0.0 && userData.getLon() != 0.0) {
                                        String lastseenstring = userData.getLastSeen();
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userData.getLat(), userData.getLon()), 9.0f));
                                        mMap.addMarker(new MarkerOptions().position(new LatLng(userData.getLat(), userData.getLon()))
                                                .title(userData.getUsername() + "(" + userData.getRole() + ")")
                                                .snippet(lastseenstring));
                                    }
                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                }
            });
        }
    }

}
