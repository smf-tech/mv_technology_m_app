package com.mv.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.mv.Utils.GetFilePathFromDevice;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityAddThetSavadBinding;
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
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class AddThetSavadActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {


    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityAddThetSavadBinding binding;
    private Uri FinalUri = null;
    private Uri outputUri = null;
    private Uri audioUri = null;
    private String imageFilePath;
    private int mSelectDistrict = 0, mSelectTaluka = 0, mSelectReportingType = 0;
    private List<String> mListDistrict;
    private List<String> mListTaluka;
    private List<String> mListReportingType;
    private ArrayAdapter<String> district_adapter, taluka_adapter;
    private PreferenceHelper preferenceHelper;
    private Content content;
    private String stringId = "";
    private Dialog dialogrecord;
    private TextView rectext;
    private static File auxFile, auxFileAudio, imgGallaery;
    private boolean isplaying = false, isFirstTime = false;
    private MediaPlayer mp;
    private static MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String audioFilePath =
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/coach/random.mp3";
    private static MediaPlayer mediaPlayer;
    private String img_str;
    private boolean isEdit;
    private Content mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_thet_savad);
        binding.setActivity(this);
        initViews();


    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
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
                    binding.spinnerDistrict.setSelection(mListDistrict.indexOf(User.getCurrentUser(AddThetSavadActivity.this).getProject_Name__c()));

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
        apiService.getTaluka(User.getCurrentUser(AddThetSavadActivity.this).getState(), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
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
        setActionbar(getString(R.string.thet_savnd));
        isEdit = getIntent().getExtras().getBoolean("EDIT");

        preferenceHelper = new PreferenceHelper(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerIssue.setOnItemSelectedListener(this);

        mListDistrict = new ArrayList<String>();
        mListTaluka = new ArrayList<String>();
        mListReportingType = new ArrayList<String>();

        mListReportingType = Arrays.asList(getResources().getStringArray(R.array.array_of_thet_savad));


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
            // getDistrict();
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
        if (isEdit) {
            mContent = (Content) getIntent().getExtras().getSerializable(Constants.CONTENT);
            binding.editTextContent.setText(mContent.getTitle());
            binding.editTextDescription.setText(mContent.getDescription());
            List<String> mList = new ArrayList<String>();
            Collections.addAll(mList, getResources().getStringArray(R.array.array_of_thet_savad));
            binding.spinnerIssue.setSelection(mList.indexOf(mContent.getReporting_type()));
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
        showMediaDialog();
    }

    private void showMediaDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_mediatype));
        String[] items = {getString(R.string.text_image),
                getString(R.string.text_audio), getString(R.string.text_video)};

        dialog.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
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
                }
            }
        });
        dialog.show();
    }

    private void showVideoDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_choosevideo));
        String[] items = {getString(R.string.text_gallary),
                getString(R.string.text_camera)};

        dialog.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                switch (which) {
                    case 0:
                        chooseVideoFromGallery();
                        break;
                    case 1:
                        takeVideoFromCamera();
                        break;

                }
            }
        });
        dialog.show();
    }

    private void chooseVideoFromGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), Constants.CHOOSE_VIDEO_FROM_GALLERY);
    }

    private void takeVideoFromCamera() {

        try {
           /* Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Video/video.mp4";
            File imageFile = new File(imageFilePath);
            outputUri = Uri.fromFile(imageFile); // convert path to Uri
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath);
            startActivityForResult(takeVideoIntent, Constants.CHOOSE_VIDEO_FROM_CAMERA);*/
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            // create a file to save the video
            outputUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
            // set the video duration
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
            // set the image file name
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            // set the video image quality to high
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

            // start the Video Capture Intent
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

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) {

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Video");


        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Toast.makeText(AddThetSavadActivity.this, "Failed to create directory MyCameraVideo.",
                        Toast.LENGTH_LONG).show();

                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }


        // Create a media file name

        // For unique file name appending current timeStamp with file name
        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;

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
            content = new Content();
            if (isEdit)
                content.setId(mContent.getId());
            content.setDescription(binding.editTextDescription.getText().toString().trim());
            content.setTitle(binding.editTextContent.getText().toString().trim());
            content.setDistrict(mListDistrict.get(mSelectDistrict));
            content.setTaluka(mListTaluka.get(mSelectTaluka));
            content.setReporting_type(mListReportingType.get(mSelectReportingType));
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
                jsonObject1.put("isTheatMessage", "true");
                if (FinalUri != null) {
                    try {
                       /* if (checkSizeExceed(FinalUri)) {
                            Utills.showToast("File Size Cannot Be Greater than 5 MB", this);
                            return;
                        }*/
                        jsonObject1.put("contentType", "Image");
                        jsonObject1.put("isAttachmentPresent", "true");
                        InputStream iStream = null;
                        iStream = getContentResolver().openInputStream(FinalUri);
                        img_str = Base64.encodeToString(Utills.getBytes(iStream), 0);
                      /*  JSONObject jsonObjectAttachment = new JSONObject();
                        jsonObjectAttachment.put("Body", img_str);
                        jsonObjectAttachment.put("Name", content.getTitle());
                        jsonObjectAttachment.put("ContentType", "image/png");
                        jsonArrayAttchment.put(jsonObjectAttachment);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (outputUri != null) {
                    try {
                       /* if (checkSizeExceed(outputUri)) {
                            Utills.showToast("File Size Cannot Be Greater than 5 MB", this);
                            return;
                        }*/
                        jsonObject1.put("contentType", "Video");
                        jsonObject1.put("isAttachmentPresent", "true");
                        img_str = getVideoString(outputUri);
                      /*  JSONObject jsonObjectAttachment = new JSONObject();
                        jsonObjectAttachment.put("Body", img_str);
                        jsonObjectAttachment.put("Name", content.getTitle());
                        jsonObjectAttachment.put("ContentType", "image/png");
                        jsonArrayAttchment.put(jsonObjectAttachment);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (audioUri != null) {
                    try {
                       /* if (checkSizeExceed(audioUri)) {
                            Utills.showToast("File Size Cannot Be Greater than 5 MB", this);
                            return;
                        }*/
                        jsonObject1.put("contentType", "Audio");
                        jsonObject1.put("isAttachmentPresent", "true");
                        img_str = getVideoString(audioUri);
                      /*  JSONObject jsonObjectAttachment = new JSONObject();
                        jsonObjectAttachment.put("Body", img_str);
                        jsonObjectAttachment.put("Name", content.getTitle());
                        jsonObjectAttachment.put("ContentType", "image/png");
                        jsonArrayAttchment.put(jsonObjectAttachment);*/
                    } catch (Exception e) {
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

                            String str = response.body().string();
                            JSONObject object = new JSONObject(str);
                            JSONArray array = object.getJSONArray("Records");
                            if (array.length() > 0) {
                                JSONObject object1 = array.getJSONObject(0);
                                if (object1.has("Id") && (FinalUri != null || outputUri != null || audioUri != null)) {
                                    JSONObject object2 = new JSONObject();
                                    object2.put("id", object1.getString("Id"));
                                    stringId = object1.getString("Id");
                                    if (FinalUri != null)
                                        object2.put("type", "png");
                                    else if (outputUri != null)
                                        object2.put("type", "mp4");
                                    else if (audioUri != null)
                                        object2.put("type", "mp3");
                                    object2.put("img", img_str);
                                    JSONArray array1 = new JSONArray();
                                    array1.put(object2);
                                    sendImageToServer(array1);
                                   /* Utills.showToast("Report submitted successfully...", getApplicationContext());
                                    finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.right_out);*/
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
        if (fileSizeInMB > 5)
            return true;
        return false;
    }

    private void showRecorDialog() {

        dialogrecord = new Dialog(AddThetSavadActivity.this);
        dialogrecord.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogrecord.setCancelable(true);
        dialogrecord.setContentView(R.layout.activity_recordaudio);

        final LinearLayout record = (LinearLayout) dialogrecord.findViewById(R.id.record);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    record.setBackgroundResource(R.drawable.blue_box_mic_radius);

                    stopClicked(v);


                } else {

                    record.setBackgroundResource(R.drawable.red_box_mic_radius);
                    try {
                        if (hasMicrophone())
                            recordAudio(v);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        final ImageView play = (ImageView) dialogrecord.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (auxFileAudio != null) {
                    if (mp == null)
                        mp = new MediaPlayer();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            isplaying = false;
                            isFirstTime = false;
                            mp.stop();
                            play.setImageResource(R.drawable.play_song);
                        }
                    });
                    try {
                        if (isplaying) {
                            isplaying = false;
                            mp.pause();
                            play.setImageResource(R.drawable.play_song);
                        } else {
                            isplaying = true;
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
                    Toast.makeText(AddThetSavadActivity.this, "Please record Audio", Toast.LENGTH_LONG).show();
                }
            }
        });

        rectext = (TextView) dialogrecord.findViewById(R.id.rectext);
        TextView done = (TextView) dialogrecord.findViewById(R.id.done);
        TextView cancel = (TextView) dialogrecord.findViewById(R.id.cancel);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {
                    mp.pause();
                }
                stopClicked(v);
                if (audioUri != null)
                    binding.addImage.setImageResource(R.drawable.mic_audio);
                dialogrecord.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioUri = null;
                binding.addImage.setImageResource(R.drawable.add);
                dialogrecord.dismiss();
            }
        });

        dialogrecord.show();

    }

    public void recordAudio(View view) throws IOException {
        isRecording = true;
        rectext.setText("Done");

        try {

            File folder = new File(Environment.getExternalStorageDirectory() + "/coach");
            if (!folder.exists()) {
                folder.mkdir();
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
        PackageManager pmanager = getPackageManager();
        return pmanager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
    }

    public void stopClicked(View view) {

        try {
            if (isRecording) {
                rectext.setText("Start");
                if (mediaRecorder != null)
                    mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                audioUri = Uri.fromFile(new File(audioFilePath));

// dialogrecord.dismiss();
            } else {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    audioUri = Uri.fromFile(new File(audioFilePath));
                }

            }

        } catch (Exception e) {

        }
    }


    private void sendImageToServer(JSONArray jsonArray) {
        Utills.showProgressDialog(this);
        JsonParser jsonParser = new JsonParser();
        JsonArray gsonObject = (JsonArray) jsonParser.parse(jsonArray.toString());
        ServiceRequest apiService =
                ApiClient.getImageClient().create(ServiceRequest.class);
        apiService.sendImageToSalesforce("http://13.58.218.106/new_upload.php", gsonObject).enqueue(new Callback<ResponseBody>() {
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
                    deleteSalesforceData();
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                deleteSalesforceData();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
            }
        });
    }

    private void deleteSalesforceData() {
        Utills.showProgressDialog(this);

        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        apiService.getSalesForceData(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/DeletePost/" + stringId).enqueue(new Callback<ResponseBody>() {
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

    private boolean isValidate() {
        String str = "";

        if (mSelectReportingType == 0 && !User.getCurrentUser(getApplicationContext()).getRoll().equals("President")) {
            str = "Please select Category ";
        } else if (binding.editTextContent.getText().toString().trim().length() == 0) {
            str = "Please enter Content";
        } else if (binding.editTextDescription.getText().toString().trim().length() == 0) {
            str = "Please enter Description";
        }
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        Utills.showToast(str, AddThetSavadActivity.this);
        return false;
    }

    private void showAudioDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_chooseaudio));
        String[] items = {getString(R.string.text_record),
                getString(R.string.text_select_audio)};

        dialog.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                switch (which) {
                    case 0:
                        showRecorDialog();
                        break;
                    case 1:
                        showSelectRecorDialog();
                        break;

                }
            }
        });
        dialog.show();
    }

    private void showSelectRecorDialog() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Constants.SELECT_AUDIO);
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
        Bitmap bmThumbnail;
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
            if (checkSizeExceed(FinalUri.getPath())) {
                FinalUri = null;
                Utills.showToast(getString(R.string.text_size_exceed), this);
            } else {
                Glide.with(this)
                        .load(FinalUri)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.addImage);
            }

        } else if (requestCode == Constants.CHOOSE_VIDEO_FROM_CAMERA && resultCode == RESULT_OK) {
            String selectedImagePath = getPath(outputUri);
            if (checkSizeExceed(selectedImagePath)) {
                outputUri = null;
                Utills.showToast(getString(R.string.text_size_exceed), this);
            } else {
                bmThumbnail = ThumbnailUtils.createVideoThumbnail(outputUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
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
                    binding.addImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(selectedVideoFilePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND));
                }
            }
        } else if (requestCode == Constants.SELECT_AUDIO && resultCode == RESULT_OK) {
            audioUri = data.getData();
            String dddd = getPath(audioUri);
            Log.e("dddd", dddd);

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
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(selectedImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int len = 0;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("converted!");
        String videoData = "";
        //Converting bytes into base64
        videoData = Base64.encodeToString(byteBuffer.toByteArray(), Base64.DEFAULT);
        Log.d("VideoData**>  ", videoData);
        String sinSaltoFinal2 = videoData.trim();
        String sinsinSalto2 = sinSaltoFinal2.replaceAll("\n", "");
        Log.d("VideoData**>  ", sinsinSalto2);
        String baseVideo = sinsinSalto2;
        return baseVideo;
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
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
