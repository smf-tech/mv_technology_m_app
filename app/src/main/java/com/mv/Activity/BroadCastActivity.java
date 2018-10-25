package com.mv.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityBroadcastBinding;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mv.R.layout.activity_broadcast;

/**
 * Created by Rohit Gujar on 30-11-2017.
 */

public class BroadCastActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityBroadcastBinding binding;
    private PreferenceHelper preferenceHelper;
    private Spinner spinner_taluka, spinner_state, spinner_district;
    private int mSelectRole = 0, mSelectState = 0, mSelectDistrict = 0, mSelectTaluka = 0;
    private ArrayList<String> mListRoleName, mListState, mListDistrict, mListTaluka;
    private ArrayAdapter<String> state_adapter, district_adapter, taluka_adapter;
    private boolean[] mSelection = null;
    private String[] items = null;
    private String value;
    private StringBuilder selectedRoles = new StringBuilder();
    private Uri FinalUri = null;
    private Uri outputUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, activity_broadcast);
        binding.setActivity(this);
        initViews();
        if (Utills.isConnected(this)) {
            getState();
            getRole();
        } else {
            showPopUp();
        }
    }

    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), (dialog, which) -> {
            alertDialog.dismiss();
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        alertDialog.show();
    }

    private void getState() {

        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        Utills.showProgressDialog(this);
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getState().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListState.clear();
                            mListState.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListState.add(jsonArray.getString(i));
                            }
                            state_adapter.notifyDataSetChanged();
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

    private void getRole() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Roles), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.MV_Role__c_URL;
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    JSONArray jsonArray = obj.getJSONArray("records");
                    mListRoleName.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        mListRoleName.add(jsonObject.getString("Name"));
                    }
                    // role_adapter.notifyDataSetChanged();

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

    private void showDialog() {
        items = new String[mListRoleName.size()];
        for (int i = 0; i < mListRoleName.size(); i++) {
            items[i] = mListRoleName.get(i);
        }
        mSelection = new boolean[items.length];
        Arrays.fill(mSelection, false);

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Roles")
                .setMultiChoiceItems(items, mSelection, (dialog13, which, isChecked) -> {
                    if (mSelection != null && which < mSelection.length) {
                        mSelection[which] = isChecked;
                        value = buildSelectedItemString(items);
                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(getString(R.string.ok), (dialog12, id) -> {
                    Log.i("value", "value");
                    binding.txtRole.setText(selectedRoles);
                }).setNegativeButton(getString(R.string.cancel), (dialog1, id) -> {
                    //  Your code when user clicked on Cancel
                }).create();
        dialog.show();
    }

    private String buildSelectedItemString(String[] items) {
        StringBuilder sb = new StringBuilder();
        selectedRoles = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                    selectedRoles.append(";");
                }
                foundOne = true;
                sb.append(i);
                selectedRoles.append(items[i]);
            }
        }
        return sb.toString();
    }

    private void initViews() {
        setActionbar("Add Broadcast");
        preferenceHelper = new PreferenceHelper(this);

        binding.txtRole.setOnClickListener(this);

        spinner_taluka = (Spinner) findViewById(R.id.spinner_taluka);
        spinner_state = (Spinner) findViewById(R.id.spinner_state);
        spinner_district = (Spinner) findViewById(R.id.spinner_district);

        spinner_taluka.setOnItemSelectedListener(this);
        spinner_state.setOnItemSelectedListener(this);
        spinner_district.setOnItemSelectedListener(this);

        mListState = new ArrayList<>();
        mListRoleName = new ArrayList<>();
        mListDistrict = new ArrayList<>();
        mListTaluka = new ArrayList<>();

        mListState.add("Select");
        mListDistrict.add("Select");


      /*  role_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mListRoleName);
        role_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_role.setAdapter(role_adapter);*/

        state_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mListState);
        state_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_state.setAdapter(state_adapter);

        district_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mListDistrict);
        district_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_district.setAdapter(district_adapter);

        taluka_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mListTaluka);
        taluka_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_taluka.setAdapter(taluka_adapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.txt_role:
                showDialog();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_state:
                mSelectState = i;
                if (mSelectState != 0) {
                    getDistrict();
                }
                mListDistrict.clear();
                mListTaluka.clear();
                mListTaluka.add("Select");
                mListDistrict.add("Select");
                district_adapter.notifyDataSetChanged();
                taluka_adapter.notifyDataSetChanged();

                break;
            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    getTaluka();
                }
                mListTaluka.clear();
                mListTaluka.add("Select");
                taluka_adapter.notifyDataSetChanged();

                break;
            case R.id.spinner_taluka:
                mSelectTaluka = i;
                break;
        }
    }

    private void getTaluka() {

        Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mListState.get(mSelectState), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
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

    private void getDistrict() {

        Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));

        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(mListState.get(mSelectState)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(response.body().string());
                            mListDistrict.clear();
                            mListDistrict.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListDistrict.add(jsonArray.getString(i));
                            }
                            district_adapter.notifyDataSetChanged();
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

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void onAddImageClick() {
        showPictureDialog();
    }

    private void showPictureDialog() {
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.text_choosepicture));
        String[] items = {getString(R.string.text_gallary),
                getString(R.string.text_camera)};

        dialog.setItems(items, (dialog1, which) -> {
            // TODO Auto-generated method stub
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
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, Constants.CHOOSE_IMAGE_FROM_GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CHOOSE_IMAGE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/picture_crop.jpg";
                File imageFile = new File(imageFilePath);
                FinalUri = Uri.fromFile(imageFile);
                Crop.of(outputUri, FinalUri).asSquare().start(this);
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
                    Crop.of(outputUri, FinalUri).asSquare().start(this);
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


    public void onBtnSubmitClick() {
        if (Utills.isConnected(this)) {
            if (isValidate()) {
                try {
                    Utills.showProgressDialog(this);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("state", mListState.get(mSelectState));
                    jsonObject.put("district", mListDistrict.get(mSelectDistrict));
                    jsonObject.put("taluka", mListTaluka.get(mSelectTaluka));
                    jsonObject.put("title", binding.editTextContent.getText().toString().trim());
                    jsonObject.put("description", binding.editTextContent.getText().toString().trim());
                    jsonObject.put("role", selectedRoles);
                    JSONObject jsonObjectAttachment = new JSONObject();
                    JSONArray jsonArrayAttchment = new JSONArray();
                    if (FinalUri != null) {

                        try {
                            InputStream iStream;
                            iStream = getContentResolver().openInputStream(FinalUri);
                            String img_str = Base64.encodeToString(Utills.getBytes(iStream), 0);
                            jsonObjectAttachment.put("Body", img_str);
                            jsonObjectAttachment.put("Name", binding.editTextContent.getText().toString().trim());
                            jsonObjectAttachment.put("ContentType", "image/png");
                            jsonArrayAttchment.put(jsonObjectAttachment);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    jsonObject.put("attachments", jsonArrayAttchment);

                    ServiceRequest apiService =
                            ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                    JsonParser jsonParser = new JsonParser();
                    JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                    apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.InsertBroadcastPostUrl, gsonObject).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Utills.hideProgressDialog();
                            try {
                                Utills.showToast(getString(R.string.broadcast_submit), getApplicationContext());
                                finish();
                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
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


                    Log.i("JSON", jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            showPopUp();
        }
    }

    private boolean isValidate() {
        String str = "";
        if (mSelectState == 0) {
            str = "Please Select State";
        } else if (TextUtils.isEmpty(selectedRoles.toString().trim())) {
            str = "Please Select Roles";
        } else if (TextUtils.isEmpty(binding.editTextContent.getText().toString().trim())) {
            str = "Please Enter Title";
        } else if (TextUtils.isEmpty(binding.editTextDescription.getText().toString().trim())) {
            str = "Please Enter Description";
        }
        if (TextUtils.isEmpty(str))
            return true;
        Utills.showToast(str, this);
        return false;
    }
}
