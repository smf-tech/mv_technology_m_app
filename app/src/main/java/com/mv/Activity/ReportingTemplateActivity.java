package com.mv.Activity;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Content;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityReportingTemplateBinding;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ReportingTemplateActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {


    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityReportingTemplateBinding binding;
    private Uri FinalUri = null;
    private Uri outputUri = null;
    private String imageFilePath;
    private int mSelectDistrict = 0, mSelectTaluka = 0, mSelectReportingType = 0;
    private List<String> mListDistrict;
    private List<String> mListTaluka;
    private List<String> mListReportingType;
    private ArrayAdapter<String> district_adapter, taluka_adapter;
    private PreferenceHelper preferenceHelper;
    private Content content;

    private String img_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reporting_template);
        binding.setActivity(this);
        initViews();


    }

    private void showPopUp() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void getDistrict() {

        Utills.showProgressDialog(this, "Loading Districts", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getDistrict_Name__c?StateName=Maharashtra";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    mListDistrict.clear();
                    mListDistrict.add("Select");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        mListDistrict.add(jsonArray.getString(i));
                    }
                    district_adapter.notifyDataSetChanged();
                    binding.spinnerDistrict.setSelection(mListDistrict.indexOf(User.getCurrentUser(ReportingTemplateActivity.this).getProject_Name__c()));

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    private void getTaluka() {

        Utills.showProgressDialog(this, "Loading Talukas", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getTaluka(User.getCurrentUser(ReportingTemplateActivity.this).getState(), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListTaluka.add(jsonArr.getString(i));
                    }
                    taluka_adapter.notifyDataSetChanged();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void initViews() {
        setActionbar("Reporting Template");

        preferenceHelper = new PreferenceHelper(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerIssue.setOnItemSelectedListener(this);

        mListDistrict = new ArrayList<String>();
        mListTaluka = new ArrayList<String>();
        mListReportingType = new ArrayList<String>();

        mListReportingType = Arrays.asList(getResources().getStringArray(R.array.array_of_reporting_type));


        mListDistrict.add("Select");
        mListDistrict.add(User.getCurrentUser(this).getDistrict());


        mListTaluka.add("Select");
        if (!Utills.isConnected(this)) {
            List<String> list = AppDatabase.getAppDatabase(this).userDao().getTaluka(User.getCurrentUser(this).getState(), User.getCurrentUser(this).getDistrict());
            if (list.size() == 0) {
                showPopUp();
            } else {

                for (int k = 0; k < list.size(); k++) {
                    mListTaluka.add(list.get(k));
                }
            }
        }

        district_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListDistrict);
        district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(district_adapter);
        binding.spinnerDistrict.setSelection(1);
        binding.spinnerDistrict.setEnabled(false);
        if (Utills.isConnected(this)) {
            binding.spinnerDistrict.setEnabled(true);
            getDistrict();
        }
        taluka_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListTaluka);
        taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTaluka.setAdapter(taluka_adapter);
        if (Constants.shareUri != null) {
            Glide.with(this)
                    .load(Constants.shareUri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.addImage);
            Constants.shareUri = null;
        }
    }

    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    public void onAddImageClick() {
        showPictureDialog();
    }


    public void onBtnSubmitClick() {
        if (isValidate()) {
            content = new Content();
            content.setDescription(binding.editTextDescription.getText().toString().trim());
            content.setTitle(binding.editTextContent.getText().toString().trim());
            content.setDistrict(mListDistrict.get(mSelectDistrict));
            content.setTaluka(mListTaluka.get(mSelectTaluka));
            content.setIssue_priority(mListReportingType.get(mSelectReportingType));
            content.setUser_id(User.getCurrentUser(this).getId());
            content.setCommunity_id(preferenceHelper.getString(PreferenceHelper.COMMUNITYID));
            content.setTemplate(preferenceHelper.getString(PreferenceHelper.TEMPLATEID));
            setdDataToSalesForcce();
        }

    }

    private void setdDataToSalesForcce() {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this);
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(content);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject(json);
                JSONArray jsonArrayAttchment = new JSONArray();
                if (FinalUri != null) {

                    try {
                        InputStream iStream = null;
                        iStream = getContentResolver().openInputStream(FinalUri);
                        img_str = Base64.encodeToString(Utills.getBytes(iStream), 0);
                        JSONObject jsonObjectAttachment = new JSONObject();
                        jsonObjectAttachment.put("Body", img_str);
                        jsonObjectAttachment.put("Name", content.getTitle());
                        jsonObjectAttachment.put("ContentType", "image/png");


                        jsonArrayAttchment.put(jsonObjectAttachment);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
                /*JSONObject jsonObjectAttachment = new JSONObject();
                jsonArrayAttchment.put(jsonObjectAttachment);*/
                jsonObject1.put("attachments", jsonArrayAttchment);
                jsonArray.put(jsonObject1);
                jsonObject.put("listVisitsData", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/insertContent", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                          /* */
                            String str = response.body().string();
                            JSONObject object = new JSONObject(str);
                            JSONArray array = object.getJSONArray("Records");
                            if (array.length() > 0) {
                                JSONObject object1 = array.getJSONObject(0);
                                if (object1.has("Id") && FinalUri != null) {
                                  /*  JSONObject object2 = new JSONObject();
                                    object2.put("id", object1.getString("Id"));
                                    object2.put("img", img_str);
                                    JSONArray array1 = new JSONArray();
                                    array1.put(object2);
                                    sendImageToServer(array1);*/
                                   Utills.showToast("Report submitted successfully...", getApplicationContext());
                                    finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                } else {
                                    Utills.showToast("Report submitted successfully...", getApplicationContext());
                                    finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                }
                            } else {
                                Utills.showToast("Report submitted successfully...", getApplicationContext());
                                finish();
                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                            }

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
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
            }
        } else {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate1 = df1.format(c.getTime());
            content.setTemplateName(preferenceHelper.getString(PreferenceHelper.TEMPLATENAME));
            content.setSynchStatus(Constants.STATUS_LOCAL);
            content.setTime(formattedDate1);
            content.setLikeCount(0);
            content.setUserName(User.getCurrentUser(this).getName());
            content.setUserAttachmentId(User.getCurrentUser(this).getImageId());
            content.setCommentCount(0);
            content.setIsLike(false);
            if (FinalUri != null) {
                String tempDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image" + "/";
                String currentTime = "" + System.currentTimeMillis();
                String current = currentTime + ".png";
                Utills.makedirs(tempDir);
                content.setAttachmentId(currentTime);
                File mypath = new File(tempDir, current);
                Utills.saveUriToPath(this, FinalUri, mypath);
            }
            AppDatabase.getAppDatabase(ReportingTemplateActivity.this).userDao().insertChats(content);
            Utills.showToast("Report submitted successfully...", getApplicationContext());
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    private void sendImageToServer(JSONArray jsonArray) {
        Utills.showProgressDialog(this);
        JsonParser jsonParser = new JsonParser();
        JsonArray gsonObject = (JsonArray) jsonParser.parse(jsonArray.toString());
        ServiceRequest apiService =
                ApiClient.getImageClient().create(ServiceRequest.class);
        apiService.sendImageToSalesforce("http://18.216.227.14/upload.php", gsonObject).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    String str = response.body().string();
                    JSONObject object = new JSONObject(str);
                    if (object.has("status")) {
                        if (object.getString("status").equalsIgnoreCase("1")) {
                            Utills.showToast("Report submitted successfully...", getApplicationContext());
                            finish();
                            overridePendingTransition(R.anim.left_in, R.anim.right_out);
                        }
                    }
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


    private boolean isValidate() {
        String str = "";

        if (mSelectDistrict == 0) {
            str = "Please select district";
        } else if (mSelectTaluka == 0) {
            str = "Please select taluka";
        } else if (mSelectReportingType == 0) {
            str = "Please select reporting type";
        } else if (binding.editTextContent.getText().toString().trim().length() == 0) {
            str = "Please enter Content";
        } else if (binding.editTextDescription.getText().toString().trim().length() == 0) {
            str = "Please enter Description";
        }
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        Utills.showToast(str, ReportingTemplateActivity.this);
        return false;
    }

    private void showPictureDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_choosepicture));
        String[] items = {getString(R.string.text_gallary),
                getString(R.string.text_camera)};

        dialog.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                switch (which) {
                    case 0:
                        choosePhotoFromGallery();
                        break;
                    case 1:
                        takePhotoFromCamera();
                        break;

                }
            }
        });
        dialog.show();
    }

    private void takePhotoFromCamera() {

        try {
            //use standard intent to capture an image
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture.jpg";
            File imageFile = new File(imageFilePath);
            outputUri = Uri.fromFile(imageFile); // convert path to Uri
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            startActivityForResult(takePictureIntent, Constants.CHOOSE_IMAGE_FROM_CAMERA);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void choosePhotoFromGallery() {
     /*   Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, Constants.CHOOSE_IMAGE_FROM_GALLERY);*/

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.CHOOSE_IMAGE_FROM_GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CHOOSE_IMAGE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture_crop.jpg";
                File imageFile = new File(imageFilePath);
                FinalUri = Uri.fromFile(imageFile);
                Crop.of(outputUri, FinalUri).start(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == Constants.CHOOSE_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    outputUri = data.getData();
                    String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture_crop.jpg";
                    File imageFile = new File(imageFilePath);
                    FinalUri = Uri.fromFile(imageFile);
                    Crop.of(outputUri, FinalUri).start(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            Glide.with(this)
                    .load(FinalUri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.addImage);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    if (Utills.isConnected(this)) {
                        getTaluka();
                    } else {

                    }

                }
                mListTaluka.clear();
                List<String> list = AppDatabase.getAppDatabase(this).userDao().getTaluka(User.getCurrentUser(this).getState(), User.getCurrentUser(this).getDistrict());
                mListTaluka.add("Select");
                for (int k = 0; k < list.size(); k++) {
                    mListTaluka.add(list.get(k));
                }
                taluka_adapter.notifyDataSetChanged();
                break;
            case R.id.spinner_taluka:
                mSelectTaluka = i;
                break;
            case R.id.spinner_issue:
                mSelectReportingType = i;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
