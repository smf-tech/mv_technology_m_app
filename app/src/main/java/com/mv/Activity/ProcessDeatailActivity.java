package com.mv.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Adapter.ProcessDetailAdapter;
import com.mv.Model.Asset;
import com.mv.Model.ImageData;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.GPSTracker;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class ProcessDeatailActivity extends AppCompatActivity implements View.OnClickListener {

    private PreferenceHelper preferenceHelper;
    private ArrayList<Task> taskList = new ArrayList<>();
    public ArrayList<ImageData> imageDataList = new ArrayList<>();
    private String pickListApiFieldNames;
    private GPSTracker gps;
    private Location location;
    private Activity context;

    private Button submit,save;
    private ProcessDetailAdapter adapter;
    private RecyclerView rvProcessDetail;

    public String selectedStructure = "";
    private String timestamp;
    private String processStatus, processName, processId;
    private String comment, imageName;
    private String msg;
    private String id = "";
    private String imageId, uniqueId = "";
    private int imagePosition;
    public int selectedTaskPosition = 0;
    public int selectedFromStructurePosition = 0;
    public int selectedFromMachinePosition = 0;
    public int selectedToStructurePosition = 0;

    private Uri outputUri = null;
    private Uri finalUri = null;
    TaskContainerModel taskContainerModel = new TaskContainerModel();
    private long UPDATE_INTERVAL = 600 * 1000;  /* 10 secs */
    File imageFile1;
    String imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_process_deatail);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        preferenceHelper = new PreferenceHelper(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        id = String.valueOf(Calendar.getInstance().getTimeInMillis());

        submit = (Button) findViewById(R.id.btn_submit);
        submit.setOnClickListener(this);

        save = (Button) findViewById(R.id.btn_save);
        save.setOnClickListener(this);

        taskList = new ArrayList<>();

        if (getIntent().getStringExtra(Constants.PROCESS_NAME) != null) {
            processName = getIntent().getStringExtra(Constants.PROCESS_NAME);
        }
        if (getIntent().getStringExtra(Constants.PROCESS_ID) != null) {
            processId=getIntent().getStringExtra(Constants.PROCESS_ID);
        }
        if (getIntent().getSerializableExtra("newForm") != null) {
            if (Utills.isConnected(this)) {
                Utills.showProgressDialog(this, getString(R.string.Loading_Process), getString(R.string.progress_please_wait));
                getAllTask();
            } else {
                //fill new forms
                preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
                //get  process list only type is question (exclude answer it would always 1 record for on process  )
                TaskContainerModel taskContainerModel = AppDatabase.getAppDatabase(
                        ProcessDeatailActivity.this).userDao().getQuestion(processId, Constants.TASK_QUESTION);

                if (taskContainerModel != null) {

                    taskList = Utills.convertStringToArrayList(taskContainerModel.getTaskListString());
                    pickListApiFieldNames =  taskContainerModel.getProAnsListString();
                    setAdapter();
                } else {
                    Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
                }
            }
        } else {
            if (getIntent().getSerializableExtra(Constants.PROCESS_ID) != null) {
                taskList = getIntent().getParcelableArrayListExtra(Constants.PROCESS_ID);
//                if (taskList != null && taskList.get(0).getId() != null && taskList.get(0).getIsSave().equals("false")) {
//                    submit.setVisibility(View.GONE);
//                    save.setVisibility(View.GONE);
//                } else {
//                    submit.setVisibility(View.VISIBLE);
//                    save.setVisibility(View.VISIBLE);
//                }
                if (taskList.size() > 0) {
                    if (taskList != null && taskList.get(0).getId() != null && !preferenceHelper.getBoolean(Constants.IS_EDITABLE) && taskList.get(0).getIsSave().equals("false")) {
                        submit.setVisibility(View.GONE);
                        save.setVisibility(View.GONE);
                    } else {
                        submit.setVisibility(View.VISIBLE);
                        save.setVisibility(View.VISIBLE);
                    }
                }
            }
//            if (getIntent().getStringExtra(Constants.PICK_LIST_ID) != null) {
//            }
            pickListApiFieldNames = getProAnsList();
            setAdapter();
        }

        startLocationUpdates();
        initViews();
    }

    String getProAnsList(){
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV";
        File file = new File(filePath,"ProAnsListString.txt");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            Log.e("exeption",e.getMessage());
        }

        return(text.toString());
    }

    @Override
    public void onBackPressed() {
        if (taskList.size()>0 && (taskList.get(0).getId() == null || taskList.get(0).getIsSave().equals("true"))) {
            showPopUp();
        } else {
            finish();
        }
    }

    private void saveToDB() {
        StringBuilder sb = new StringBuilder();
        String prefix = "";

        for (int i = 0; i < taskList.size(); i++) {
            Log.d("pos", "" + i);
            taskList.get(i).setIsSave(Constants.PROCESS_STATE_SAVE);
            taskList.get(i).setTimestamp__c(timestamp);

            if (taskList.get(i).getIsHeader().equals("true")) {
                if (!taskList.get(i).getTask_Response__c().equals("Select")) {
                    sb.append(prefix);
                    prefix = " , ";
                    sb.append(taskList.get(i).getTask_Response__c());
                }
            }

            taskList.get(i).setMTUser__c(User.getCurrentUser(context).getMvUser().getId());
            if (preferenceHelper.getBoolean(Constants.NEW_PROCESS)) {
                taskList.get(i).setId(null);
            }
        }

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(taskList);
        TaskContainerModel taskContainerModel = new TaskContainerModel();
        taskContainerModel.setTaskListString(json);
        taskContainerModel.setTaskType(Constants.TASK_ANSWER);
        taskContainerModel.setHeaderPosition(sb.toString());
        taskContainerModel.setIsSave(Constants.PROCESS_STATE_SAVE);
        taskContainerModel.setMV_Process__c(taskList.get(0).getMV_Process__c());
        Long currentTime = System.currentTimeMillis();
        taskContainerModel.setTaskTimeStamp(currentTime.toString());
        taskContainerModel.setProAnsListString(pickListApiFieldNames);
        taskContainerModel.setStatus("Pending");

        if (preferenceHelper.getBoolean(Constants.NEW_PROCESS)) {
            //if process is new  INSERT it with timestamp as id
            taskContainerModel.setUnique_Id(id);
            AppDatabase.getAppDatabase(context).userDao().insertTask(taskContainerModel);
        } else {
            //if process is not new  UPDATE it with exiting id
            taskContainerModel.setUnique_Id(preferenceHelper.getString(Constants.UNIQUE));
            AppDatabase.getAppDatabase(context).userDao().updateTask(taskContainerModel);
        }

        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }
    }

    private void getAllTask() {
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetprocessTaskUrl + "?Id=" + processId
                + "&language=" + preferenceHelper.getString(Constants.LANGUAGE)
                + "&userId=" + User.getCurrentUser(this).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONArray resultArray = jsonObject.getJSONArray("tsk");

                            //list of task
                            taskContainerModel = new TaskContainerModel();
                            User user = User.getCurrentUser(getApplicationContext());
                            StringBuilder sb = new StringBuilder();
                            String prefix = "";

                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject resultJsonObj = resultArray.getJSONObject(i);

                                //task is each task detail
                                Task processList = new Task();
                                processList.setMV_Task__c_Id(resultJsonObj.getString("id"));
                                processList.setName(resultJsonObj.getString("name"));
                                processList.setIs_Completed__c(resultJsonObj.getBoolean("isCompleted"));

                                processList.setIsHeader(resultJsonObj.getString("isHeader"));
                                processList.setIs_Response_Mnadetory__c(resultJsonObj.getBoolean("isResponseMnadetory"));

                                if (!resultJsonObj.getString("lanTsaskText").equals("null")) {
                                    processList.setTask_Text___Lan_c(resultJsonObj.getString("lanTsaskText"));
                                } else {
                                    processList.setTask_Text___Lan_c(resultJsonObj.getString("taskText"));
                                }

                                if (resultJsonObj.has("status")) {
                                    processList.setStatus__c(resultJsonObj.getString("status"));
                                }
                                if (resultJsonObj.has("ValidationRule")) {
                                    processList.setValidationRule(resultJsonObj.getString("ValidationRule"));
                                }
                                if (resultJsonObj.has("MinRange")) {
                                    processList.setMinRange(resultJsonObj.getString("MinRange"));
                                }
                                if (resultJsonObj.has("MaxRange")) {
                                    processList.setMaxRange(resultJsonObj.getString("MaxRange"));
                                }
                                if (resultJsonObj.has("LimitValue")) {
                                    processList.setLimitValue(resultJsonObj.getString("LimitValue"));
                                }

                                if (resultJsonObj.has("isEditable")) {
                                    processList.setIsEditable__c(resultJsonObj.getString("isEditable"));
                                }

                                processList.setPicklist_Value_Lan__c(resultJsonObj.getString("lanPicklistValue"));
                                if (resultJsonObj.has("Process_Answer_Status__c")) {
                                    processList.setProcess_Answer_Status__c(resultJsonObj.getString("Process_Answer_Status__c"));
                                }

                                if (resultJsonObj.has("picklistValue")) {
                                    processList.setPicklist_Value__c(resultJsonObj.getString("picklistValue"));
                                }

                                if (!resultJsonObj.getString("locationLevel").equals("null")) {
                                    processList.setLocationLevel(resultJsonObj.getString("locationLevel"));

                                    switch (resultJsonObj.getString("locationLevel")) {
                                        case "State":
                                            processList.setTask_Response__c(user.getMvUser().getState());
                                            break;

                                        case "District":
                                            processList.setTask_Response__c(user.getMvUser().getDistrict());
                                            break;

                                        case "Taluka":
                                            processList.setTask_Response__c(user.getMvUser().getTaluka());
                                            break;

                                        case "Cluster":
                                            processList.setTask_Response__c(user.getMvUser().getCluster());
                                            break;

                                        case "Village":
                                            processList.setTask_Response__c(user.getMvUser().getVillage());
                                            break;

                                        case "School":
                                            processList.setTask_Response__c(user.getMvUser().getSchool_Name());
                                            break;
                                    }

                                    if (resultJsonObj.getString("isHeader").equals("true")) {
                                        if (!processList.getTask_Response__c().equals("Select")) {
                                            sb.append(prefix);
                                            prefix = " , ";
                                            sb.append(processList.getTask_Response__c());
                                        }
                                    }
                                }

                                if (resultJsonObj.has("referenceField")) {
                                    processList.setReferenceField(resultJsonObj.getString("referenceField"));
                                }
                                if (resultJsonObj.has("filterFields")) {
                                    processList.setFilterFields(resultJsonObj.getString("filterFields"));
                                }
                                if (resultJsonObj.has("aPIFieldName")) {
                                    processList.setaPIFieldName(resultJsonObj.getString("aPIFieldName"));
                                }

                                processList.setIsExactLength(resultJsonObj.getBoolean("isExactLength"));
                                processList.setMV_Process__c(resultJsonObj.getString("mVProcess"));
                                processList.setTask_Text__c(resultJsonObj.getString("taskText"));
                                processList.setTask_type__c(resultJsonObj.getString("tasktype"));
                                processList.setValidation(resultJsonObj.getString("validaytionOnText"));
                                processList.setIsSave(Constants.PROCESS_STATE_SAVE);
                                taskList.add(processList);
                            }

                            // each task list  convert to String and stored in process task filled
                            taskContainerModel.setTaskListString(Utills.convertArrayListToString(taskList));
                            taskContainerModel.setHeaderPosition(sb.toString());
                            taskContainerModel.setIsSave(Constants.PROCESS_STATE_SAVE);

                            //task without answer
                            taskContainerModel.setTaskType(Constants.TASK_QUESTION);
                            taskContainerModel.setMV_Process__c(processId);

                            //delete old question
                            AppDatabase.getAppDatabase(getApplicationContext())
                                    .userDao().deleteQuestion(processId, Constants.TASK_QUESTION);

                            //add new question
                            AppDatabase.getAppDatabase(getApplicationContext()).userDao().insertTask(taskContainerModel);

                            JSONArray pickListArray = jsonObject.getJSONArray("proAnsList");

                            pickListApiFieldNames = pickListArray.toString();
                            Utills.hideProgressDialog();
                            setAdapter();
                            if (taskList.size() > 0) {
                                if (taskList != null && taskList.get(0).getId() != null && !preferenceHelper.getBoolean(Constants.IS_EDITABLE) && taskList.get(0).getIsSave().equals("false")) {
                                    submit.setVisibility(View.GONE);
                                    save.setVisibility(View.GONE);
                                } else {
                                    submit.setVisibility(View.VISIBLE);
                                    save.setVisibility(View.VISIBLE);
                                }
//                                preferenceHelper.insertBoolean(Constants.NEW_PROCESS, true);
//                                Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
//                                openClass.putExtra(Constants.PICK_LIST_ID, pickListArray.toString());
//                                openClass.putExtra(Constants.PROCESS_NAME, processName);
//                                openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
//                                startActivity(openClass);
//                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            } else {
                                Utills.showToast(getString(R.string.No_Task), ProcessDeatailActivity.this);
                            }
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.No_Task), ProcessDeatailActivity.this);
            }
        });
    }

    void setAdapter(){
        adapter = new ProcessDetailAdapter(this, taskList, pickListApiFieldNames);

        rvProcessDetail = (RecyclerView) findViewById(R.id.rv_process_detail);
        rvProcessDetail.setNestedScrollingEnabled(false);
        rvProcessDetail.setHasFixedSize(true);
        rvProcessDetail.setHasFixedSize(true);
        rvProcessDetail.setLayoutManager(new LinearLayoutManager(this));
        rvProcessDetail.setAdapter(adapter);
    }

    private void initViews() {
        setActionbar(processName);

        gps = new GPSTracker(ProcessDeatailActivity.this);

        timestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());

        Button approve = (Button) findViewById(R.id.btn_approve);
        approve.setOnClickListener(this);

        Button reject = (Button) findViewById(R.id.btn_reject);
        reject.setOnClickListener(this);

        if (preferenceHelper.getString(Constants.PROCESS_TYPE).equals(Constants.APPROVAL_PROCESS)) {
            approve.setVisibility(View.VISIBLE);
            reject.setVisibility(View.VISIBLE);
            submit.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
        } else if (preferenceHelper.getString(Constants.PROCESS_TYPE).equals(Constants.MANGEMENT_PROCESS)) {
            approve.setVisibility(View.GONE);
            reject.setVisibility(View.GONE);
//            if (taskList != null && taskList.get(0).getId() != null && !preferenceHelper.getBoolean(Constants.IS_EDITABLE)) {
//                submit.setVisibility(View.GONE);
//                save.setVisibility(View.GONE);
//            } else {
//                submit.setVisibility(View.VISIBLE);
//                save.setVisibility(View.VISIBLE);
//            }
        }

        ImageView img_add = (ImageView) findViewById(R.id.img_add);
        img_add.setOnClickListener(this);
    }

    // Trigger new location updates at interval
    private void startLocationUpdates() {
        if (!Utills.isLocationPermissionGranted(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.LOCATION_PERMISSION_REQUEST);
            }
        } else {
            getLocationProviderClient();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocationProviderClient() {
        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    private void onLocationChanged(Location location) { this.location = location;
    }

    private void setActionbar(String Title) {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    public void showPictureDialog(String imgName, int position) {
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_choosepicture));
        String[] items = {getString(R.string.text_gallary), getString(R.string.text_camera)};

        dialog.setItems(items, (dialog1, which) -> {
            switch (which) {
                case 0:
                    sendToGallery(imgName, position);
                    break;

                case 1:
                    sendToCamera(imgName, position);
                    break;
            }
        });

        dialog.show();
    }

    public void sendToCamera(String imgName, int position) {
        imageName = imgName;
        imagePosition = position;

        if (!Utills.isMediaPermissionGranted(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.MEDIA_PERMISSION_REQUEST);
            }
        } else {
            showPictureDialog();
        }
    }

    public void sendToGallery(String imgName, int position) {
        imageName = imgName;
        imagePosition = position;

        if (!Utills.isMediaPermissionGranted(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.MEDIA_PERMISSION_REQUEST);
            }
        } else {
            choosePhotoFromGallery();
        }
    }

    private void choosePhotoFromGallery() {
        try {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, Constants.CHOOSE_IMAGE_FROM_GALLERY);
        } catch (ActivityNotFoundException e) {
            String errorMessage = "Problem in taking photo from gallery, please use camera to take photo.";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void showPictureDialog() {
        try {
            //use standard intent to capture an image
            String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture.jpg";
            File imageFile = new File(imageFilePath);
            outputUri = FileProvider.getUriForFile(getApplicationContext(),
                    getPackageName() + ".fileprovider", imageFile);

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(takePictureIntent, Constants.CHOOSE_IMAGE_FROM_CAMERA);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        } catch (SecurityException se) {
            String errorMessage = "App do not have permission to take a photo, please allow it.";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.MEDIA_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPictureDialog();
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;

            case R.id.btn_submit:
                submitAllData();
                break;

            case R.id.img_add:
                if (!gps.canGetLocation()) {
                    gps.showSettingsAlert();
                    return;
                }
                break;

            case R.id.btn_save:
                saveToDB();
                break;

            case R.id.btn_approve:
                comment = "";
                processStatus = "Approved";
                sendApprovedData();
                break;

            case R.id.btn_reject:
                showDialog();
                break;
        }
    }

    @SuppressWarnings("deprecation")
    private void showPopUp() {
        if (preferenceHelper.getString(Constants.PROCESS_TYPE).equals(Constants.MANGEMENT_PROCESS)) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            // Setting Dialog Title
            alertDialog.setTitle(getString(R.string.app_name));

            // Setting Dialog Message
            alertDialog.setMessage(getString(R.string.are_you_really));

            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.app_logo);

            // Setting CANCEL Button
            alertDialog.setButton2(getString(R.string.cancel), (dialog, which) -> {
                alertDialog.dismiss();
                finish();
            });

            // Setting OK Button
            alertDialog.setButton(getString(R.string.ok), (dialog, which) -> saveToDB());

            // Showing Alert Message
            alertDialog.show();
        } else {
            finish();
        }
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProcessDeatailActivity.this);
        alertDialog.setTitle(getString(R.string.comments));
        alertDialog.setMessage("Please Enter Comment");

        final EditText input = new EditText(ProcessDeatailActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            processStatus = "Rejected";
            comment = input.getText().toString();

            if (!comment.isEmpty()) {
                sendApprovedData();
            } else {
                Utills.showToast("Please Enter Comment", ProcessDeatailActivity.this);
            }
        });

        alertDialog.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    public void saveDataToList(Task answer, int position) {
        taskList.set(position, answer);
    }

    private void submitAllData() {
        Boolean mandatoryFlag = false;

        for (int i = 0; i < taskList.size(); i++) {
            taskList.get(i).setTimestamp__c(timestamp);
            taskList.get(i).setMTUser__c(User.getCurrentUser(context).getMvUser().getId());
            taskList.get(i).setIsSave(Constants.PROCESS_STATE_SUBMIT);

            if (preferenceHelper.getBoolean(Constants.NEW_PROCESS)) {
                taskList.get(i).setId(null);
            }else{
                if(taskList.get(i).getId()==null || taskList.get(i).getId().equalsIgnoreCase("null")){
                    taskList.remove(i);
                    break;
                }
            }

            if (taskList.get(i).getIs_Response_Mnadetory__c()) {
                if(taskList.get(i).getTask_type__c().equalsIgnoreCase(Constants.CHECK_BOX) && taskList.get(i).getTask_Response__c().equals("false")){
                    mandatoryFlag = true;
                    msg = "please check " + taskList.get(i).getTask_Text__c();
                    break;
                } else if (taskList.get(i).getTask_type__c().equalsIgnoreCase(Constants.TASK_PICK_LIST)
                        && taskList.get(i).getTask_Response__c().equals("Select")) {
                    mandatoryFlag = true;
                    msg = "please check " + taskList.get(i).getTask_Text__c();
                    break;
                } else if (taskList.get(i).getTask_type__c().equalsIgnoreCase(Constants.TASK_SELECTION)
                        && taskList.get(i).getTask_Response__c().equals("Select")) {
                    mandatoryFlag = true;
                    msg = "please check " + taskList.get(i).getTask_Text__c();
                    break;
                } else if (taskList.get(i).getTask_Response__c().equals("")) {
                    mandatoryFlag = true;
                    msg = "please check " + taskList.get(i).getTask_Text__c();
                    break;
                }
            }

            if(taskList.get(i).getTask_Response__c()!=null && taskList.get(i).getTask_Response__c().length()>0) {
                if (taskList.get(i).getValidationRule() != null && taskList.get(i).getValidationRule().equals("Range")) {
                    double val;
                    if (taskList.get(i).getTask_Response__c() == null || taskList.get(i).getTask_Response__c().equals("")) {
                        mandatoryFlag = true;
                        msg = "please enter " + taskList.get(i).getTask_Text__c();
                        break;
                    } else {
                        try {
                            val = Double.parseDouble(taskList.get(i).getTask_Response__c());
                            if (Double.parseDouble(taskList.get(i).getMaxRange()) < val) {
                                mandatoryFlag = true;
                                msg = "please enter " + taskList.get(i).getTask_Text__c() + " value less than " + taskList.get(i).getMaxRange();
                                break;
                            } else if (Double.parseDouble(taskList.get(i).getMinRange()) > val) {
                                mandatoryFlag = true;
                                msg = "please enter " + taskList.get(i).getTask_Text__c() + " value grater than " + taskList.get(i).getMinRange();
                                break;
                            }
                        } catch (NumberFormatException nfe) {
                            mandatoryFlag = true;
                            msg = "please check " + taskList.get(i).getTask_Text__c();
                            break;
                        }
                    }
                } else if (taskList.get(i).getValidationRule() != null && taskList.get(i).getValidationRule().equals("Length")) {
                    if (taskList.get(i).getTask_Response__c() == null || taskList.get(i).getTask_Response__c().equals("")) {
                        mandatoryFlag = true;
                        msg = "please enter " + taskList.get(i).getTask_Text__c();
                        break;
                    } else {
                        if (taskList.get(i).isExactLength()) {
                            if (Integer.parseInt(taskList.get(i).getLimitValue()) != taskList.get(i).getTask_Response__c().length()) {
                                mandatoryFlag = true;
                                msg = "please enter valid " + taskList.get(i).getTask_Text__c();
                                break;
                            }
                        } else if (Integer.parseInt(taskList.get(i).getLimitValue()) < taskList.get(i).getTask_Response__c().length()) {
                            mandatoryFlag = true;
                            msg = "please enter valid " + taskList.get(i).getTask_Text__c();
                            break;
                        }
                    }
                }
            }
        }

        if (!mandatoryFlag) {
            if (Utills.isConnected(this)) {
                boolean hasMVUser = false;
                for (int i = 0; i < taskList.size(); i++) {
                    if (taskList.get(i).getTask_type__c().equalsIgnoreCase(Constants.TASK_MV_USER)) {
                        if(taskList.get(i).getTask_Response__c()!=null && taskList.get(i).getTask_Response__c().length()>0){
                            getUserName(taskList.get(i).getTask_Response__c(), i);
                            hasMVUser = true;
                        } else {
                            hasMVUser = false;
                        }
                        break;
                    }
                }
                if (!hasMVUser) {
                    callApiForSubmit(taskList);
                }
            } else {
                Utills.showToast(getString(R.string.error_no_internet), this);
                submit.setEnabled(true);
            }
        } else {
            Utills.showToast(msg, context);
            submit.setEnabled(true);
        }
    }

    private void getUserName(String number, int position) {
        Utills.showProgressDialog(this, "Sending", this.getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserThroughMobileNo + "?mobileNo=" + number.trim();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            Asset asset = gson.fromJson(data, Asset.class);
                            String id = asset.getAsset_id();
                            String firstName = asset.getName();
                            String lastName = asset.getLast_Name__c();

                            for (int i = 0; i < taskList.size(); i++) {
                                if (taskList.get(i).getTask_type__c().equalsIgnoreCase(Constants.TASK_MV_USER_ANSWER)) {
                                    if (lastName != null)
                                        taskList.get(i).setTask_Response__c(firstName + " " + lastName + "(" + id + ")");
                                    else {
                                        taskList.get(i).setTask_Response__c(firstName + "(" + id + ")");
                                    }
                                }
                            }

                            callApiForSubmit(taskList);
                        }
                    } else {
                        Toast.makeText(ProcessDeatailActivity.this, ProcessDeatailActivity.this.getResources()
                                .getString(R.string.enter_moblie_no), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ProcessDeatailActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callApiForSubmit(ArrayList<Task> temp) {
        submit.setEnabled(false);
        try {
            Utills.showProgressDialog(context);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(temp);

            JSONArray jsonArray = new JSONArray(json);
            JSONObject jsonObject = new JSONObject();
            if(location!=null){
                jsonObject.put("formLat", location.getLatitude());
                jsonObject.put("formLong", location.getLongitude());
            }
            jsonObject.put("listtaskanswerlist", jsonArray);

            ServiceRequest apiService = ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
            JsonParser jsonParser = new JsonParser();
            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

            apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + Constants.InsertAnswerForProcessAnswerUrl, gsonObject).enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        if (response.isSuccess() && response.body() != null) {
                            JSONObject response1 = new JSONObject(response.body().string());
                            JSONArray resultArray = response1.getJSONArray("Records");

                            for (int j = 0; j < resultArray.length(); j++) {
                                JSONObject object = resultArray.getJSONObject(j);
                                if (object.has("Task_Type")) {
                                    if (object.getString("Task_Type").equalsIgnoreCase(Constants.IMAGE)) {
                                        if (object.has("Answer")) {
                                            if (object.getString("Answer").length() > 0) {
                                                for (ImageData id : imageDataList) {
                                                    if (id.getPosition() == j) {
                                                        imageId = object.getString("Answer");
                                                        uniqueId = object.getString("Id");
                                                        id.setImageId(imageId);
                                                        id.setImageUniqueId(uniqueId);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            String json = gson.toJson(taskList);

                        //    TaskContainerModel taskContainerModel = new TaskContainerModel();
                            taskContainerModel.setTaskListString(json);
                            taskContainerModel.setTaskType(Constants.TASK_ANSWER);
                            taskContainerModel.setUnique_Id(preferenceHelper.getString(Constants.UNIQUE));
                            taskContainerModel.setIsSave(Constants.PROCESS_STATE_SUBMIT);
                            taskContainerModel.setMV_Process__c(taskList.get(0).getMV_Process__c());

//                            AppDatabase.getAppDatabase(context).userDao().deleteSingleTask(
//                                    preferenceHelper.getString(Constants.UNIQUE), taskContainerModel.getMV_Process__c());


                            if (imageDataList.size() > 0) {
                                sendImage(imageDataList.get(0));
                            } else {
                                AppDatabase.getAppDatabase(context).userDao().deleteSingleTask(
                                        preferenceHelper.getString(Constants.UNIQUE), taskContainerModel.getMV_Process__c());
                                submit.setEnabled(true);
                                finish();
                            }
                        } else {
                            Utills.hideProgressDialog();
                            submit.setEnabled(true);
                            Utills.showToast(getString(R.string.error_something_went_wrong), context);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        submit.setEnabled(true);
                        Utills.showToast(getString(R.string.error_something_went_wrong), context);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    submit.setEnabled(true);
                    Utills.showToast(getString(R.string.error_something_went_wrong), context);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            submit.setEnabled(true);
            Utills.showToast(getString(R.string.error_something_went_wrong), context);
        }
    }

    private void sendImage(ImageData imgData) {
        try {
            JSONObject object2 = new JSONObject();
            object2.put("id", imgData.getImageId());
            object2.put("type", "png");

            InputStream iStream = getContentResolver().openInputStream(imgData.getImageUri());
            if (iStream != null) {
                object2.put("img", Base64.encodeToString(Utills.getBytes(iStream), 0));
            }

            JSONArray array1 = new JSONArray();
            array1.put(object2);

            // Remove uploaded image
            if (imageDataList.size() > 0) {
                imageDataList.remove(0);
            }

            // Upload image to server
            sendImageToServer(array1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendImageToServer(JSONArray jsonArray) {
        Utills.showProgressDialog(this);
        ServiceRequest apiService = ApiClient.getImageClient().create(ServiceRequest.class);
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("json_data", jsonArray.toString()).build();

        apiService.sendImageToPHP(requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    String str = response.body().string();
                    JSONObject object = new JSONObject(str);
                    if (object.has("status")) {
                        if (object.getString("status").equalsIgnoreCase("1")) {
                            if (!imageDataList.isEmpty()) {
                                sendImage(imageDataList.get(0));
                            } else {
                                AppDatabase.getAppDatabase(context).userDao().deleteSingleTask(
                                        preferenceHelper.getString(Constants.UNIQUE), taskContainerModel.getMV_Process__c());
                                finish();
                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                            }
                        }
                    }
                } catch (Exception e) {
                    deleteSalesForceData();
                //    Utills.hideProgressDialog();
                //    Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                deleteSalesForceData();
            }
        });
    }

    private void deleteSalesForceData() {
    //    Utills.showProgressDialog(this);

        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
//        apiService.getSalesForceData(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
//                + Constants.DeleteTaskAnswerUrl + uniqueId).enqueue(new Callback<ResponseBody>() {
        apiService.getSalesForceData(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/deleteProcessAnswer?processAnswerId="
                + uniqueId).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    saveToDB();
                    Utills.showToast(getString(R.string.not_Submitted_Slow_Interenet), getApplicationContext());
                    finish();
                } catch (Exception e) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.not_Submitted_Slow_Interenet), getApplicationContext());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                finish();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CHOOSE_IMAGE_FROM_CAMERA && resultCode == RESULT_OK) {
            try {
                imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/" + imageName + ".jpg";
                imageFile1 = new File(imageFilePath);
                finalUri = Uri.fromFile(imageFile1);
                Crop.of(outputUri, finalUri).start(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == Constants.CHOOSE_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    outputUri = data.getData();
                    imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/" + imageName + ".jpg";
                    imageFile1 = new File(imageFilePath);
                    finalUri = Uri.fromFile(imageFile1);
                    Crop.of(outputUri, finalUri).start(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (finalUri != null) {
                outputUri = null;
            }
        //    decodeFile(imageFile1);
            compressImage(imageFilePath);
            ImageData id = new ImageData();
            id.setPosition(imagePosition);
            id.setImageUri(finalUri);
            imageDataList.add(id);

            adapter.notifyDataSetChanged();
        } else if (resultCode == RESULT_OK) {
            taskList = data.getParcelableArrayListExtra(Constants.PROCESS_ID);
            adapter = new ProcessDetailAdapter(this, taskList, pickListApiFieldNames);
            rvProcessDetail.setAdapter(adapter);
        } else if (requestCode == 100) {
            if (!gps.canGetLocation()) {
                gps.showSettingsAlert();
            }
        }
    }

    private void sendApprovedData() {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this, getString(R.string.share_post), getString(R.string.progress_please_wait));
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("uniqueId", taskList.get(0).getId());
                jsonObject1.put("ApprovedBy", User.getCurrentUser(getApplicationContext()).getMvUser().getId());
                jsonObject1.put("isApproved", processStatus);
                jsonObject1.put("comment", comment);

                ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + Constants.ApproveCommentforProcessUrl, gsonObject).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            Utills.setIsActionDone(true);
                            Utills.showToast(getString(R.string.submitted_successfully), ProcessDeatailActivity.this);
                            finish();
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), ProcessDeatailActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), ProcessDeatailActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), ProcessDeatailActivity.this);
            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), ProcessDeatailActivity.this);
        }
    }

    public void showDialogFullImage(String imageName){
        final Dialog dialog = new Dialog(ProcessDeatailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_full_image);

        ImageView iv_image = (ImageView) dialog.findViewById(R.id.iv_image);
        Glide.with(ProcessDeatailActivity.this)
                .load(Constants.IMAGEURL + imageName + ".png")
                .placeholder(ProcessDeatailActivity.this.getResources().getDrawable(R.drawable.ic_add_photo))
                .into(iv_image);
        ImageView iv_close = (ImageView) dialog.findViewById(R.id.iv_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

//    private Bitmap decodeFile(File f) {
//        Bitmap b = null;
//
//        //Decode image size
//        BitmapFactory.Options o = new BitmapFactory.Options();
//        o.inJustDecodeBounds = true;
//
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(f);
//            BitmapFactory.decodeStream(fis, null, o);
//            fis.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        int IMAGE_MAX_SIZE = 512;
//        int scale = 1;
//        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
//            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
//                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
//        }
//
//        //Decode with inSampleSize
//        BitmapFactory.Options o2 = new BitmapFactory.Options();
//        o2.inSampleSize = scale;
//        try {
//            fis = new FileInputStream(f);
//            b = BitmapFactory.decodeStream(fis, null, o2);
//            fis.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/" + imageName + ".jpg";
//        imageFile1 = new File(imageFilePath);
//        try {
//            FileOutputStream out = new FileOutputStream(imageFile1);
//            b.compress(Bitmap.CompressFormat.PNG, 100, out);
//            out.flush();
//            out.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return b;
//    }

    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612
        try {
            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;
//      width and height values are set maintaining the aspect ratio of the image
            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;
                }
            }
        }catch (ArithmeticException e){
            Utills.showToast("Please try again.", this);
        }catch (Exception e){
            Utills.showToast("Please try again.", this);
        }
//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
            Utills.showToast("Something went wrong,Please try again.",this);
        }
        catch (Exception e) {
            e.printStackTrace();
            Utills.showToast("Something went wrong,Please try again.",this);
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
    //    String filename = getFilename();
        try {
            out = new FileOutputStream(imageFilePath);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return imageFilePath;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
}