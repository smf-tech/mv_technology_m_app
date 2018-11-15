package com.mv.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Expense;
import com.mv.Model.User;
import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityExpenseNewBinding;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */
public class ExpenseNewActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private ActivityExpenseNewBinding binding;
    private PreferenceHelper preferenceHelper;

    private Uri finalUri = null;
    private Uri outputUri = null;

    private int mParticularSelect = 0;
    private String stringId = "";
    private String img_str;
    private boolean isAdd;

    private List<String> particularList = new ArrayList<>();
    private Expense mExpense;
    private Voucher voucher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_expense_new);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

        initViews();
    }

    public void onAddImageClick() {
        if (Constants.AccountTeamCode.equals("TeamManagement")) {
            if (mExpense.getAttachmentPresent() != null || mExpense.getAttachmentPresent().equalsIgnoreCase("true")) {
                Utills.showImageZoomInDialog(ExpenseNewActivity.this, mExpense.getId());
            }
        } else {
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

    /*
     * Show dialog to select image from camera or gallary
     * */
    private void showPictureDialog() {
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
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

    /*
     * Intent to open camera
     * */
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
        } catch (SecurityException se) {
            String errorMessage = "App do not have permission to take a photo, please allow it.";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Intent to open gallery
     * */
    private void choosePhotoFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, Constants.CHOOSE_IMAGE_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.CHOOSE_IMAGE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
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
            Glide.with(this)
                    .load(finalUri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.addImage);
        }
    }

    private void initViews() {
        setActionbar(getString(R.string.expense_new));

        voucher = (Voucher) getIntent().getSerializableExtra(Constants.VOUCHER);
        particularList = Arrays.asList(getResources().getStringArray(R.array.array_of_particulars));
        preferenceHelper = new PreferenceHelper(this);
        binding.txtDate.setOnClickListener(this);
        binding.spinnerParticular.setOnItemSelectedListener(this);

        if (getIntent().getExtras() != null &&
                Constants.ACTION_ADD.equalsIgnoreCase(getIntent().getExtras().getString(Constants.ACTION))) {
            isAdd = true;
        } else {
            isAdd = false;
            mExpense = (Expense) getIntent().getSerializableExtra(Constants.EXPENSE);
            binding.txtDate.setText(mExpense.getDate());
            binding.editTextAmount.setText(mExpense.getAmount());
            binding.editTextDescription.setText(mExpense.getDecription());
            binding.editApproveAmt.setText(mExpense.getApproved_Amount__c());
            binding.editApproveRemarks.setText(mExpense.getRemark__c());
            mParticularSelect = particularList.indexOf(mExpense.getPartuculars());
            binding.spinnerParticular.setSelection(mParticularSelect);

            binding.txtDate.setEnabled(false);
            binding.editTextAmount.setEnabled(false);
            binding.editTextDescription.setEnabled(false);
            binding.spinnerParticular.setEnabled(false);
            binding.btnSubmit.setVisibility(View.GONE);
            binding.btnApprove.setOnClickListener(this);
            binding.btnReject.setOnClickListener(this);

            if (mExpense.getApproved_Amount__c() != null) {
                binding.approveAmt.setVisibility(View.VISIBLE);
                binding.editApproveAmt.setText(mExpense.getApproved_Amount__c());
            }

            if (mExpense.getRemark__c() != null) {
                binding.approveRemarks.setVisibility(View.VISIBLE);
                binding.editApproveRemarks.setText(mExpense.getRemark__c());
            }

            if (mExpense.getStatus().equalsIgnoreCase("Pending")) {
                binding.editApproveAmt.setText(mExpense.getAmount());
                binding.approveRemarks.setVisibility(View.VISIBLE);
                binding.linearly.setVisibility(View.VISIBLE);
                binding.editApproveRemarks.setEnabled(true);
                binding.editApproveAmt.setEnabled(true);
            } else {
                binding.linearly.setVisibility(View.GONE);
                binding.editApproveAmt.setEnabled(false);
                binding.editApproveRemarks.setEnabled(false);
            }

            if (mExpense.getAttachmentPresent() != null &&
                    mExpense.getAttachmentPresent().equalsIgnoreCase("true")) {
                Glide.with(ExpenseNewActivity.this)
                        .load(Constants.IMAGEURL + mExpense.getId() + ".png")
                        .placeholder(getResources().getDrawable(R.drawable.mulya_bg))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.addImage);

                binding.tvAddAttchment.setVisibility(View.GONE);
                if (!mExpense.getStatus().equalsIgnoreCase("Pending")) {
                    binding.addImage.setEnabled(false);
                }
            } else {
                if (mExpense.getStatus().equalsIgnoreCase("Pending")) {
                    binding.addImage.setVisibility(View.VISIBLE);
                    binding.tvAddAttchment.setText(R.string.addAttachment);
                } else {
                    binding.addImage.setVisibility(View.GONE);
                    binding.tvAddAttchment.setText(R.string.no_attachment_text);
                }
            }

            if (!Constants.AccountTeamCode.equals("TeamManagement")) {
                binding.approveRemarks.setVisibility(View.GONE);
                binding.linearly.setVisibility(View.GONE);
                binding.btnSubmit.setVisibility(View.GONE);

                if (mExpense.getStatus().equalsIgnoreCase("Pending")) {
                    binding.approveAmt.setVisibility(View.GONE);
                    binding.editTextAmount.setEnabled(true);
                    binding.editTextDescription.setEnabled(true);
                    binding.btnSubmit.setVisibility(View.VISIBLE);
                } else {
                    binding.editTextAmount.setEnabled(false);
                    binding.editTextDescription.setEnabled(false);

                    if (mExpense.getApproved_Amount__c() != null) {
                        binding.approveAmt.setVisibility(View.VISIBLE);
                        binding.editApproveAmt.setText(mExpense.getApproved_Amount__c());
                    }

                    if (mExpense.getRemark__c() != null) {
                        binding.approveRemarks.setVisibility(View.VISIBLE);
                        binding.editApproveRemarks.setText(mExpense.getRemark__c());
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case R.id.txtDate:
                showDateDialog();
                break;

            case R.id.btn_reject:
                if (!binding.editApproveAmt.getText().toString().equals("") &&
                        Double.parseDouble(binding.editApproveAmt.getText().toString()) > 0) {
                    Toast.makeText(this, "Please clear approve amount to reject.", Toast.LENGTH_LONG).show();
                } else {
                    changeExpense("Rejected");
                }
                break;

            case R.id.btn_approve:
                //check if approve amount is greater than requested amount
                if (binding.editApproveAmt.getText().toString().trim().length() > 0) {
                    if (Integer.parseInt(binding.editApproveAmt.getText().toString().trim()) >
                            Integer.parseInt(binding.editTextAmount.getText().toString().trim())) {
                        Utills.showToast(getString(R.string.valid_expense), this);
                    } else {
                        changeExpense("Approved");
                    }
                } else {
                    Utills.showToast("Please enter proper Expense to be approve.", this);
                }
                break;
        }
    }

    private void changeExpense(String status) {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this);

                Expense expense = mExpense;
                expense.setStatus(status);
                expense.setApproved_Amount__c(binding.editApproveAmt.getText().toString().trim());
                expense.setRemark__c(binding.editApproveRemarks.getText().toString().trim());

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(expense);
                JSONObject jsonObject1 = new JSONObject(json);

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + "/services/apexrest/InsertExpense", gsonObject).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response != null && response.isSuccess()) {
                                if (response.body() != null) {
                                    String data = response.body().string();
                                    if (data.length() > 0) {
                                        Utills.showToast("Status of expense changed successfully",
                                                ExpenseNewActivity.this);
                                        finish();
                                        overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                    }
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
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setActionbar(String title) {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        String str = title;
        if (title != null && title.contains("\n")) {
            str = title.replace("\n", " ");
        }
        toolbar_title.setText(str);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.GONE);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }

    public void onSubmitClick() {
        if (isValid()) {
            Expense expense = new Expense();
            if (!isAdd) {
                expense = mExpense;
            }

            expense.setPartuculars(particularList.get(mParticularSelect));
            expense.setDate(binding.txtDate.getText().toString().trim());
            expense.setDecription(binding.editTextDescription.getText().toString().trim());
            expense.setAmount(binding.editTextAmount.getText().toString().trim());
            expense.setVoucherId("" + voucher.getId());
            expense.setUser(User.getCurrentUser(this).getMvUser().getId());
            addExpense(expense);
        }
    }

    private void addExpense(final Expense expense) {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(this);
                try {
                    if (finalUri != null) {
                        InputStream iStream = getContentResolver().openInputStream(finalUri);
                        if (iStream != null) {
                            img_str = Base64.encodeToString(Utills.getBytes(iStream), 0);
                        }
                        expense.setAttachmentPresent("true");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(expense);
                JSONObject jsonObject1 = new JSONObject(json);

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + "/services/apexrest/InsertExpense", gsonObject).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response != null && response.isSuccess()) {
                                if (response.body() != null) {
                                    String data = response.body().string();
                                    if (data.length() > 0) {
                                        JSONObject object = new JSONObject(data);
                                        JSONArray array = object.getJSONArray("Records");

                                        if (array.length() != 0) {
                                            expense.setId(array.getJSONObject(0).getString("Id"));
                                            expense.setStatus("Pending");

                                            AppDatabase.getAppDatabase(ExpenseNewActivity.this).userDao().insertExpense(expense);
                                            if (finalUri != null) {
                                                stringId = array.getJSONObject(0).getString("Id");

                                                JSONObject object2 = new JSONObject();
                                                object2.put("id", stringId);
                                                object2.put("type", "png");
                                                object2.put("img", img_str);

                                                JSONArray array1 = new JSONArray();
                                                array1.put(object2);
                                                sendImageToServer(array1);
                                            } else {
                                                Utills.showToast("Expense Added successfully", ExpenseNewActivity.this);
                                                finish();
                                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                            }
                                        }
                                    }
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
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }
    }

    private void deleteRecord(String Id) {
        Utills.showProgressDialog(this, "Deleting Expense", getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.DeleteAccountData + "?Id=" + Id + "&Object=Expense";

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
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
                                Utills.showToast("Expense Added successfully", getApplicationContext());
                                finish();
                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                            }
                        }
                    }
                } catch (Exception e) {
                    deleteRecord(stringId);
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                deleteRecord(stringId);
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), ExpenseNewActivity.this);
            }
        });
    }

    private boolean isValid() {
        String str = "";
        if (mParticularSelect == 0) {
            str = "Please select Particulars";
        } else if (binding.txtDate.getText().toString().trim().length() == 4) {
            str = "Please select Date";
        } else if (binding.editTextAmount.getText().toString().trim().length() == 0) {
            str = "Please enter Amount";
        } else if (binding.editTextDescription.getText().toString().trim().length() == 0) {
            str = "Please enter Description";
        }

        if (str.length() != 0) {
            Utills.showToast(str, this);
            return false;
        }
        return true;
    }

    private static String getTwoDigit(int i) {
        if (i < 10) {
            return "0" + i;
        }

        return "" + i;
    }

    @SuppressLint("SetTextI18n")
    private void showDateDialog() {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) ->
                        binding.txtDate.setText(year + "-" + getTwoDigit(monthOfYear + 1)
                                + "-" + getTwoDigit(dayOfMonth)), mYear, mMonth, mDay);
        dpd.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinnerParticular:
                mParticularSelect = i;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
