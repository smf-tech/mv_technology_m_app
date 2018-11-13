package com.mv.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.mv.Model.Content;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.GetFilePathFromDevice;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityReportingTemplateBinding;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class ReportingTemplateActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private Content mContent;
    private PreferenceHelper preferenceHelper;
    private ActivityReportingTemplateBinding binding;

    private Uri finalUri = null;
    private Uri outputUri = null;
    private Uri audioUri = null;
    private Uri pdfUri = null;

    private int mSelectDistrict = 0, mSelectTaluka = 0, mSelectReportingType = 0;

    private List<String> mListDistrict;
    private List<String> mListTaluka;
    private List<String> mListReportingType;
    private ArrayAdapter<String> districtAdapter, talukaAdapter;

    private String imgStr;
    private String stringId = "";
    private String audioFilePath =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/coach/random.mp3";

    private MediaPlayer mp;
    private Dialog dialogRecord;
    private TextView recText;

    private File auxFileAudio;
    private MediaRecorder mediaRecorder;

    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isFirstTime = false;
    private boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reporting_template);
        binding.setActivity(this);

        if (!Utills.isConnected(this)) {
            showPopUp();
        }

        initViews();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @SuppressWarnings("deprecation")
    private void showPopUp() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), (dialog, which) -> {
            alertDialog.dismiss();
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void getDistrict() {
        Utills.showProgressDialog(this, "Loading Districts", getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getDistrict(User.getCurrentUser(ReportingTemplateActivity.this)
                .getMvUser().getState()).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListDistrict.clear();
                            mListDistrict.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListDistrict.add(jsonArr.getString(i));
                            }

                            districtAdapter.notifyDataSetChanged();

                            for (int i = 0; i < mListDistrict.size(); i++) {
                                if (mListDistrict.get(i).equalsIgnoreCase(User.getCurrentUser(
                                        ReportingTemplateActivity.this).getMvUser().getDistrict())) {
                                    binding.spinnerDistrict.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
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
        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(User.getCurrentUser(ReportingTemplateActivity.this).getMvUser()
                .getState(), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            mListTaluka.clear();
                            mListTaluka.add("Select");

                            JSONArray jsonArr = new JSONArray(data);
                            for (int i = 0; i < jsonArr.length(); i++) {
                                mListTaluka.add(jsonArr.getString(i));
                            }

                            talukaAdapter.notifyDataSetChanged();

                            for (int i = 0; i < mListTaluka.size(); i++) {
                                if (mListTaluka.get(i).equalsIgnoreCase(User.getCurrentUser(
                                        ReportingTemplateActivity.this).getMvUser().getTaluka())) {
                                    binding.spinnerTaluka.setSelection(i);
                                    break;
                                }
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
            }
        });
    }

    private void initViews() {
        setActionbar();

        if (getIntent().getExtras() != null) {
            isEdit = getIntent().getExtras().getBoolean("EDIT");
        }

        preferenceHelper = new PreferenceHelper(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerIssue.setOnItemSelectedListener(this);
        binding.layoutMore.setOnClickListener(this);

        mListReportingType = new ArrayList<>();
        mListReportingType = Arrays.asList(getResources().getStringArray(R.array.array_of_reporting_type));

        mListDistrict = new ArrayList<>();
        mListDistrict.add("Select");
        mListDistrict.add(User.getCurrentUser(this).getMvUser().getDistrict());

        if (User.getCurrentUser(ReportingTemplateActivity.this).getMvUser().getRoll().equalsIgnoreCase("TC")) {
            binding.txtSpinner.setVisibility(View.VISIBLE);
            binding.spinnerIssue.setVisibility(View.VISIBLE);
        }

        mListTaluka = new ArrayList<>();
        mListTaluka.add("Select");

        if (!Utills.isConnected(this)) {
            List<String> list = AppDatabase.getAppDatabase(this).userDao().getTaluka(
                    User.getCurrentUser(this).getMvUser().getState(),
                    User.getCurrentUser(this).getMvUser().getDistrict());

            if (list.size() == 0) {
                showPopUp();
            } else {
                mListTaluka.addAll(list);
            }
        }

        districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListDistrict);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spinnerDistrict.setAdapter(districtAdapter);
        binding.spinnerDistrict.setSelection(1);
        binding.spinnerDistrict.setEnabled(false);

        if (Utills.isConnected(this)) {
            binding.spinnerDistrict.setEnabled(true);
            getDistrict();
        }

        talukaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListTaluka);
        talukaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTaluka.setAdapter(talukaAdapter);

        if (Constants.shareUri != null) {
            Glide.with(this)
                    .load(Constants.shareUri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.addImage);
            Constants.shareUri = null;
        }

        if (isEdit) {
            mContent = (Content) getIntent().getExtras().getSerializable(Constants.CONTENT);
            if (mContent != null) {
                binding.editTextContent.setText(mContent.getTitle());
                binding.editTextDescription.setText(mContent.getDescription());
            }

            List<String> mList = new ArrayList<>();
            Collections.addAll(mList, getResources().getStringArray(R.array.array_of_reporting_type));
            binding.spinnerIssue.setSelection(mList.indexOf(mContent.getReporting_type()));
        }
    }

    private void setActionbar() {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Reporting Template");

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
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

            case R.id.layoutMore:
                if (binding.layoutMoreDetail.getVisibility() == View.GONE) {
                    binding.layoutMoreDetail.setVisibility(View.VISIBLE);

                    if (!User.getCurrentUser(ReportingTemplateActivity.this)
                            .getMvUser().getRoll().equalsIgnoreCase("TC")) {
                        binding.txtSpinner.setVisibility(View.VISIBLE);
                        binding.spinnerIssue.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.layoutMoreDetail.setVisibility(View.GONE);
                    if (!User.getCurrentUser(ReportingTemplateActivity.this)
                            .getMvUser().getRoll().equalsIgnoreCase("TC")) {
                        binding.txtSpinner.setVisibility(View.GONE);
                        binding.spinnerIssue.setVisibility(View.GONE);
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    public void onAddImageClick() {
        if (!Utills.isMediaPermissionGranted(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO}, Constants.MEDIA_PERMISSION_REQUEST);
            }
        } else {
            showMediaDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.MEDIA_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showMediaDialog();
                }
                break;
        }
    }

    private void showMediaDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_mediatype));
        String[] items = {getString(R.string.text_image), getString(R.string.text_audio), getString(R.string.text_video)};

        dialog.setItems(items, (dialog1, which) -> {
            switch (which) {
                case 0:
                    showPictureDialog();
                    break;

                case 1:
                    showAudioDialog();
                    break;

                case 2:
                    showVideoDialog();
                    break;

                case 3:
                    Intent intent = new Intent();
                    intent.setType("application/pdf");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    }

                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.CHOOSE_PDF);
                    break;
            }
        });

        dialog.show();
    }

    private void showVideoDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_choosevideo));
        String[] items = {getString(R.string.text_gallary), getString(R.string.text_camera)};

        dialog.setItems(items, (dialog1, which) -> {
            switch (which) {
                case 0:
                    chooseVideoFromGallery();
                    break;

                case 1:
                    takeVideoFromCamera();
                    break;
            }
        });

        dialog.show();
    }

    private void chooseVideoFromGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"),
                Constants.CHOOSE_VIDEO_FROM_GALLERY);
    }

    private void takeVideoFromCamera() {
        try {
            // create a file to save the video
            outputUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

            // set the video duration
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, Constants.CHOOSE_VIDEO_FROM_CAMERA);
        } catch (ActivityNotFoundException anfe) {
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri(int type) {
        // return Uri.fromFile(getOutputMediaFile(type));
        File videoFile = getOutputMediaFile(type);
        return FileProvider.getUriForFile(getApplicationContext(),
                getPackageName() + ".fileprovider", videoFile);
    }

    /**
     * Create a File for saving an image or video
     */
    @SuppressLint("SimpleDateFormat")
    private File getOutputMediaFile(int type) {
        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Video");

        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Toast.makeText(ReportingTemplateActivity.this,
                        "Failed to create directory MyCameraVideo.", Toast.LENGTH_LONG).show();

                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }

        // Create a media file name
        // For unique file name appending current timeStamp with file name
        File mediaFile;
        Date date = new Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date.getTime());

        if (type == MEDIA_TYPE_VIDEO) {
            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void onBtnSubmitClick() {
        if (isValidate()) {
            Content content = new Content();
            if (isEdit) {
                content.setId(mContent.getId());
            }

            content.setDescription(binding.editTextDescription.getText().toString().trim());
            content.setTitle(binding.editTextContent.getText().toString().trim());
            content.setDistrict(mListDistrict.get(mSelectDistrict));
            content.setTaluka(mListTaluka.get(mSelectTaluka));
            content.setReporting_type(mListReportingType.get(mSelectReportingType));
            content.setUser_id(User.getCurrentUser(this).getMvUser().getId());
            content.setCommunity_id(preferenceHelper.getString(PreferenceHelper.COMMUNITYID));
            content.setTemplate(preferenceHelper.getString(PreferenceHelper.TEMPLATEID));

            setDataToSalesForce(content);
        }
    }

    private void setDataToSalesForce(Content content) {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this);
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(content);
                JSONObject jsonObject1 = new JSONObject(json);

                if (finalUri != null) {
                    try {
                        jsonObject1.put("contentType", "Image");
                        jsonObject1.put("isAttachmentPresent", "true");

                        InputStream iStream = getContentResolver().openInputStream(finalUri);
                        if (iStream != null) {
                            imgStr = Base64.encodeToString(Utills.getBytes(iStream), 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (outputUri != null) {
                    jsonObject1.put("contentType", "Video");
                    jsonObject1.put("isAttachmentPresent", "true");
                    imgStr = getVideoString(outputUri);
                } else if (audioUri != null) {
                    jsonObject1.put("contentType", "Audio");
                    jsonObject1.put("isAttachmentPresent", "true");
                    imgStr = getVideoString(audioUri);
                } else if (pdfUri != null) {
                    jsonObject1.put("contentType", "Pdf");
                    jsonObject1.put("isAttachmentPresent", "true");

                    try {
                        InputStream iStream = getContentResolver().openInputStream(pdfUri);
                        if (iStream != null) {
                            imgStr = Base64.encodeToString(Utills.getBytes(iStream), 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                JSONArray jsonArrayAttachment = new JSONArray();
                jsonObject1.put("attachments", jsonArrayAttachment);

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("listVisitsData", jsonArray);

                ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + Constants.InsertContentUrl, gsonObject).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            String str = response.body().string();
                            JSONObject object = new JSONObject(str);
                            JSONArray array = object.getJSONArray("Records");

                            if (array.length() > 0) {
                                JSONObject object1 = array.getJSONObject(0);

                                if (object1.has("Id") && (pdfUri != null || finalUri != null
                                        || outputUri != null || audioUri != null)) {

                                    stringId = object1.getString("Id");
                                    JSONObject object2 = new JSONObject();
                                    object2.put("id", stringId);

                                    if (finalUri != null) {
                                        object2.put("type", "png");
                                    } else if (outputUri != null) {
                                        object2.put("type", "mp4");
                                    } else if (audioUri != null) {
                                        object2.put("type", "mp3");
                                    } else if (pdfUri != null) {
                                        object2.put("type", "pdf");
                                    }

                                    object2.put("img", imgStr);

                                    JSONArray array1 = new JSONArray();
                                    array1.put(object2);
                                    sendImageToServer(array1);
                                } else {
                                    Utills.hideProgressDialog();
                                    Utills.showToast("Report submitted successfully...", getApplicationContext());
                                    finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                }
                            } else {
                                Utills.hideProgressDialog();
                                Utills.showToast("Report submitted successfully...", getApplicationContext());
                                finish();
                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                            }
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            e.printStackTrace();
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
            showPopUp();
        }
    }

    private boolean checkSizeExceed(String filePath) {
        File f = new File(filePath);
        // Get length of file in bytes
        long fileSizeInBytes = f.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;
        return fileSizeInMB > 5;
    }

    private void showRecordDialog() {
        dialogRecord = new Dialog(ReportingTemplateActivity.this);
        dialogRecord.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogRecord.setCancelable(true);
        dialogRecord.setContentView(R.layout.activity_recordaudio);

        final LinearLayout record = dialogRecord.findViewById(R.id.record);
        record.setOnClickListener(v -> {
            if (isRecording) {
                record.setBackgroundResource(R.drawable.blue_box_mic_radius);
                stopClicked(v);
            } else {
                record.setBackgroundResource(R.drawable.red_box_mic_radius);
                if (hasMicrophone()) {
                    recordAudio(v);
                }
            }
        });

        final ImageView play = dialogRecord.findViewById(R.id.play);
        play.setOnClickListener(v -> {
            if (auxFileAudio != null) {
                if (mp == null) {
                    mp = new MediaPlayer();
                }

                mp.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    isFirstTime = false;
                    mp.stop();
                    play.setImageResource(R.drawable.play_song);
                });

                try {
                    if (isPlaying) {
                        isPlaying = false;
                        mp.pause();
                        play.setImageResource(R.drawable.play_song);
                    } else {
                        isPlaying = true;
                        play.setImageResource(R.drawable.pause_song);

                        if (!isFirstTime) {
                            isFirstTime = true;
                            mp.reset();
                            mp.setDataSource(audioFilePath);//Write your location here
                            mp.prepare();
                            mp.start();
                        } else {
                            mp.start();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ReportingTemplateActivity.this, "Please record Audio", Toast.LENGTH_LONG).show();
            }
        });

        recText = dialogRecord.findViewById(R.id.rectext);
        TextView done = dialogRecord.findViewById(R.id.done);
        done.setOnClickListener(v -> {
            if (mp != null) {
                mp.pause();
            }
            stopClicked(v);

            if (audioUri != null) {
                binding.addImage.setImageResource(R.drawable.mic_audio);
            }

            dialogRecord.dismiss();
        });

        TextView cancel = dialogRecord.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            audioUri = null;
            binding.addImage.setImageResource(R.drawable.add);
            dialogRecord.dismiss();
        });

        dialogRecord.show();
    }

    public void recordAudio(View view) {
        isRecording = true;
        recText.setText("Done");

        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/coach");
            if (!folder.exists()) {
                boolean dirCreated = folder.mkdir();
                System.out.print(dirCreated);
            }

            auxFileAudio = new File(audioFilePath);
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    protected boolean hasMicrophone() {
        PackageManager pManager = getPackageManager();
        return pManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    public void stopClicked(View view) {
        try {
            if (isRecording) {
                recText.setText("Start");

                if (mediaRecorder != null) {
                    mediaRecorder.stop();
                }

                if (mediaRecorder != null) {
                    mediaRecorder.release();
                }

                mediaRecorder = null;
                isRecording = false;
                audioUri = Uri.fromFile(new File(audioFilePath));
            } else {
                if (mp != null) {
                    mp.release();
                    mp = null;
                    audioUri = Uri.fromFile(new File(audioFilePath));
                }
            }
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
                    if (response.body() != null) {
                        String str = response.body().string();
                        JSONObject object = new JSONObject(str);

                        if (object.has("status")) {
                            if (object.getString("status").equalsIgnoreCase("1")) {
                                Utills.showToast("Report submitted successfully...", getApplicationContext());
                                finish();
                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                            }
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
                Utills.showToast(getString(R.string.error_something_went_wrong), ReportingTemplateActivity.this);
            }
        });
    }

    private void deleteSalesForceData() {
        Utills.showProgressDialog(ReportingTemplateActivity.this);
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        apiService.getSalesForceData(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.DeletePostUrl + stringId).enqueue(new Callback<ResponseBody>() {

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
                Utills.showToast(ReportingTemplateActivity.this.getString(R.string.error_something_went_wrong), ReportingTemplateActivity.this);
            }
        });
    }

    private boolean isValidate() {
        String str = "";

        if (User.getCurrentUser(ReportingTemplateActivity.this).getMvUser().getRoll().equalsIgnoreCase("TC")
                && mSelectReportingType == 0) {
            str = "Please Select Reporting Type";
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

    private void showAudioDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_chooseaudio));
        String[] items = {getString(R.string.text_record), getString(R.string.text_select_audio)};

        dialog.setItems(items, (dialog1, which) -> {
            switch (which) {
                case 0:
                    showRecordDialog();
                    break;

                case 1:
                    showSelectRecordDialog();
                    break;
            }
        });

        dialog.show();
    }

    private void showSelectRecordDialog() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Constants.SELECT_AUDIO);
    }

    private void showPictureDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_choosepicture));
        String[] items = {getString(R.string.text_gallary), getString(R.string.text_camera)};

        dialog.setItems(items, (dialog1, which) -> {
            switch (which) {
                case 0:
                    choosePhotoFromGallery();
                    break;

                case 1:
                    takePhotoFromCamera();
                    break;
            }
        });

        dialog.show();
    }

    private void takePhotoFromCamera() {
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

    private void choosePhotoFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.CHOOSE_IMAGE_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.CHOOSE_PDF && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    pdfUri = data.getData();
                    String selectedVideoFilePath = GetFilePathFromDevice.getPath(this, pdfUri);
                    if (checkSizeExceed(selectedVideoFilePath)) {
                        pdfUri = null;
                        Utills.showToast(getString(R.string.text_size_exceed), this);
                    }
                    binding.addImage.setImageResource(R.drawable.pdfattachment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == Constants.CHOOSE_IMAGE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture_crop.jpg";
            File imageFile = new File(imageFilePath);
            finalUri = Uri.fromFile(imageFile);
            Crop.of(outputUri, finalUri).start(this);
        } else if (requestCode == Constants.CHOOSE_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                outputUri = data.getData();
                String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture_crop.jpg";
                File imageFile = new File(imageFilePath);
                finalUri = Uri.fromFile(imageFile);
                Crop.of(outputUri, finalUri).start(this);
            }
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (checkSizeExceed(outputUri.getPath())) {
                finalUri = null;
                Utills.showToast(getString(R.string.text_size_exceed), this);
            } else {
                Glide.with(this)
                        .load(finalUri)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.addImage);
            }
        } else if (requestCode == Constants.CHOOSE_VIDEO_FROM_CAMERA && resultCode == RESULT_OK) {
            String selectedImagePath = outputUri.getPath();
            if (checkSizeExceed(selectedImagePath)) {
                outputUri = null;
                Utills.showToast(getString(R.string.text_size_exceed), this);
            } else {
                Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(outputUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                binding.addImage.setImageBitmap(bmThumbnail);
            }
        } else if (requestCode == Constants.CHOOSE_VIDEO_FROM_GALLERY && resultCode == RESULT_OK) {
            outputUri = data.getData();
            String selectedVideoFilePath = GetFilePathFromDevice.getPath(this, outputUri);

            if (checkSizeExceed(selectedVideoFilePath)) {
                outputUri = null;
                Utills.showToast(getString(R.string.text_size_exceed), this);
            } else {
                if (selectedVideoFilePath != null) {
                    binding.addImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(
                            selectedVideoFilePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND));
                }
            }
        } else if (requestCode == Constants.SELECT_AUDIO && resultCode == RESULT_OK) {
            audioUri = data.getData();
            if (checkSizeExceed(getPath(audioUri))) {
                audioUri = null;
                Utills.showToast(getString(R.string.text_size_exceed), this);
            } else {
                auxFileAudio = new File(getPath(audioUri));
                binding.addImage.setImageResource(R.drawable.mic);
            }
        }
    }

    private String getVideoString(Uri selectedImageUri) {
        int len;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            if (inputStream != null) {
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Converting bytes into base64
        String videoData = Base64.encodeToString(byteBuffer.toByteArray(), Base64.DEFAULT);
        String sinSaltoFinal2 = videoData.trim();
        return sinSaltoFinal2.replaceAll("\n", "");
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
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
                        Utills.showToast("No Internet Connectivity.", this);
                    }
                }

                List<String> list = AppDatabase.getAppDatabase(this).userDao().getTaluka(
                        User.getCurrentUser(this).getMvUser().getState(),
                        User.getCurrentUser(this).getMvUser().getDistrict());

                mListTaluka.clear();
                mListTaluka.add("Select");
                mListTaluka.addAll(list);
                talukaAdapter.notifyDataSetChanged();
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