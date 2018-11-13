package com.mv.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Adapter.ProcessDetailAdapter;
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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessDeatailActivity extends AppCompatActivity implements View.OnClickListener {

    private PreferenceHelper preferenceHelper;
    private ArrayList<Task> taskList = new ArrayList<>();
    private GPSTracker gps;
    private Activity context;

    private ProcessDetailAdapter adapter;
    private RecyclerView rvProcessDetail;
    private ImageView img_add;

    private String timestamp;
    private String comment;
    private String isSave;
    private String msg;
    private String id = "";
    private String imageId, uniqueId = "";

    private Uri outputUri = null;
    private Uri FinalUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_process_deatail);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        preferenceHelper = new PreferenceHelper(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        id = String.valueOf(Calendar.getInstance().getTimeInMillis());

        if (getIntent().getSerializableExtra(Constants.PROCESS_ID) != null) {
            taskList = getIntent().getParcelableArrayListExtra(Constants.PROCESS_ID);
        }

        initViews();
    }

    @Override
    public void onBackPressed() {
        showPopUp();
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

    private void initViews() {
        setActionbar(getString(R.string.Task_List));

        gps = new GPSTracker(ProcessDeatailActivity.this);
        rvProcessDetail = (RecyclerView) findViewById(R.id.rv_process_detail);
        rvProcessDetail.setNestedScrollingEnabled(false);
        rvProcessDetail.setHasFixedSize(true);
        adapter = new ProcessDetailAdapter(this, taskList);
        rvProcessDetail.setHasFixedSize(true);
        rvProcessDetail.setLayoutManager(new LinearLayoutManager(this));
        rvProcessDetail.setAdapter(adapter);
        timestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());

        Button submit = (Button) findViewById(R.id.btn_submit);
        submit.setOnClickListener(this);

        Button save = (Button) findViewById(R.id.btn_save);
        save.setOnClickListener(this);

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
            submit.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
        }

        LinearLayout layout_photo = (LinearLayout) findViewById(R.id.layout_photo);
        boolean isPresent = false;
        img_add = (ImageView) findViewById(R.id.img_add);
        img_add.setOnClickListener(this);

        for (Task task : taskList) {
            if (task.getTask_type__c().equalsIgnoreCase(Constants.IMAGE)) {
                isPresent = true;
                imageId = task.getTask_Response__c();
                break;
            }
        }

        if (isPresent) {
            layout_photo.setVisibility(View.VISIBLE);

            if (imageId != null && imageId.length() > 0) {
                Glide.with(this)
                        .load(Constants.IMAGEURL + imageId + ".png")
                        .placeholder(getResources().getDrawable(R.drawable.ic_add_photo))
                        .into(img_add);
            }

            if (!(preferenceHelper.getBoolean(Constants.NEW_PROCESS))) {
                String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/MV/Image/" + preferenceHelper.getString(Constants.UNIQUE) + ".jpg";

                File imageFile = new File(imageFilePath);
                if (imageFile.exists()) {
                    FinalUri = Uri.fromFile(imageFile);
                    Glide.with(this)
                            .load(FinalUri)
                            .placeholder(getResources().getDrawable(R.drawable.ic_add_photo))
                            .into(img_add);
                }
            }
        }
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

    private void showPictureDialog() {
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
            return;
        }

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
                showPopUp();
                break;

            case R.id.btn_submit:
                submitAllData();
                break;

            case R.id.img_add:
                if (!Utills.isMediaPermissionGranted(this)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO}, Constants.MEDIA_PERMISSION_REQUEST);
                    }
                } else {
                    showPictureDialog();
                }
                break;

            case R.id.btn_save:
                saveToDB();
                break;

            case R.id.btn_approve:
                comment = "";
                isSave = "true";
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
            alertDialog.setIcon(R.drawable.ic_launcher);

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
            isSave = "false";
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
            }

            if (taskList.get(i).getIs_Response_Mnadetory__c() &&
                    taskList.get(i).getTask_Response__c().equals("")) {
                mandatoryFlag = true;
                msg = "please check " + taskList.get(i).getTask_Text__c();
                break;
            }

            if (taskList.get(i).getValidationRule() != null &&
                    taskList.get(i).getValidationRule().equals("Range")) {

                double val;
                if (taskList.get(i).getTask_Response__c() == null ||
                        taskList.get(i).getTask_Response__c().equals("")) {
                    mandatoryFlag = true;
                    msg = "please check " + taskList.get(i).getTask_Text__c();
                    break;
                } else {
                    try {
                        val = Double.parseDouble(taskList.get(i).getTask_Response__c());
                    } catch (NumberFormatException nfe) {
                        mandatoryFlag = true;
                        msg = "please check " + taskList.get(i).getTask_Text__c();
                        break;
                    }
                }

                if (Double.parseDouble(taskList.get(i).getMaxRange()) < val ||
                        Double.parseDouble(taskList.get(i).getMinRange()) > val) {
                    mandatoryFlag = true;
                    msg = "please check " + taskList.get(i).getTask_Text__c();
                    break;
                }
            }

            if (taskList.get(i).getTask_type__c().equalsIgnoreCase(Constants.IMAGE)) {
                if (FinalUri != null) {
                    try {
                        /* */
                        taskList.get(i).setTask_Response__c("true");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        if (!mandatoryFlag) {
            if (Utills.isConnected(this)) {
                callApiForSubmit(taskList);
            } else {
                Utills.showToast(getString(R.string.error_no_internet), this);
            }
        } else {
            Utills.showToast(msg, context);
        }
    }

    private void callApiForSubmit(ArrayList<Task> temp) {
        try {
            Utills.showProgressDialog(context);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(temp);

            JSONArray jsonArray = new JSONArray(json);
            JSONObject jsonObject = new JSONObject();
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
                        JSONObject response1 = new JSONObject(response.body().string());
                        JSONArray resultArray = response1.getJSONArray("Records");
                        boolean isImagePresent = false;

                        for (int j = 0; j < resultArray.length(); j++) {
                            JSONObject object = resultArray.getJSONObject(j);
                            if (object.has("Task_Type")) {
                                if (object.getString("Task_Type").equalsIgnoreCase(Constants.IMAGE)) {
                                    if (object.has("Answer")) {
                                        if (object.getString("Answer").length() > 0) {
                                            isImagePresent = true;
                                            imageId = object.getString("Answer");
                                            uniqueId = object.getString("Id");
                                        }
                                    }
                                }
                            }
                        }

                        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                        String json = gson.toJson(taskList);

                        TaskContainerModel taskContainerModel = new TaskContainerModel();
                        taskContainerModel.setTaskListString(json);
                        taskContainerModel.setTaskType(Constants.TASK_ANSWER);
                        taskContainerModel.setUnique_Id(preferenceHelper.getString(Constants.UNIQUE));
                        taskContainerModel.setIsSave(Constants.PROCESS_STATE_SUBMIT);
                        taskContainerModel.setMV_Process__c(taskList.get(0).getMV_Process__c());

                        AppDatabase.getAppDatabase(context).userDao().deleteSingleTask(
                                preferenceHelper.getString(Constants.UNIQUE), taskContainerModel.getMV_Process__c());

                        if (isImagePresent && FinalUri != null) {
                            JSONObject object2 = new JSONObject();
                            object2.put("id", imageId);
                            object2.put("type", "png");

                            InputStream iStream = getContentResolver().openInputStream(FinalUri);
                            if (iStream != null) {
                                object2.put("img", Base64.encodeToString(Utills.getBytes(iStream), 0));
                            }

                            JSONArray array1 = new JSONArray();
                            array1.put(object2);
                            sendImageToServer(array1);
                        } else {
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), context);
                }
            });
        } catch (JSONException e) {
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
                            finish();
                            overridePendingTransition(R.anim.left_in, R.anim.right_out);
                        }
                    }
                } catch (Exception e) {
                    deleteSalesForceData();
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                deleteSalesForceData();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
            }
        });
    }

    private void deleteSalesForceData() {
        Utills.showProgressDialog(this);

        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        apiService.getSalesForceData(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.DeleteTaskAnswerUrl + uniqueId).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    Utills.showToast("Please try again...", getApplicationContext());
                } catch (Exception e) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
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
                String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/" + id + ".jpg";
                File imageFile = new File(imageFilePath);
                FinalUri = Uri.fromFile(imageFile);
                Crop.of(outputUri, FinalUri).start(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (FinalUri != null) {
                Glide.with(this)
                        .load(FinalUri)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(img_add);
                outputUri = null;
            }
        } else if (resultCode == RESULT_OK) {
            taskList = data.getParcelableArrayListExtra(Constants.PROCESS_ID);
            adapter = new ProcessDetailAdapter(this, taskList);
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
                Utills.showProgressDialog(this, getString(R.string.share_post),
                        getString(R.string.progress_please_wait));

                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("uniqueId", taskList.get(0).getId());
                jsonObject1.put("ApprovedBy", User.getCurrentUser(getApplicationContext()).getMvUser().getId());
                jsonObject1.put("isApproved", isSave);
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
}