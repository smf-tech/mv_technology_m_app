package com.mv.Activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class ExpenseNewActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityExpenseNewBinding binding;
    private int mParticularSelect = 0;
    private List<String> particularList = new ArrayList<>();
    private Expense mExpense;
    private boolean isAdd;
    private Voucher voucher;
    private PreferenceHelper preferenceHelper;
    private Uri FinalUri = null;
    private Uri outputUri = null;
    private String stringId = "";
    private String img_str;

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
            showPictureDialog();
        }
    }


    /*
       * Show dialog to select image from camera or gallary
       * */
    private void showPictureDialog() {
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
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

    /*
    * Intent to open camera
    * */
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

    /*
    * Intent to open gallery
    * */
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

    private void initViews() {
        voucher = (Voucher) getIntent().getSerializableExtra(Constants.VOUCHER);
        particularList = Arrays.asList(getResources().getStringArray(R.array.array_of_particulars));
        setActionbar(getString(R.string.expense_new));
        preferenceHelper = new PreferenceHelper(this);
        binding.txtDate.setOnClickListener(this);
        binding.spinnerParticular.setOnItemSelectedListener(this);
        if (getIntent().getExtras().getString(Constants.ACTION).equalsIgnoreCase(Constants.ACTION_ADD)) {
            isAdd = true;
        } else {
            isAdd = false;
            mExpense = (Expense) getIntent().getSerializableExtra(Constants.EXPENSE);
            binding.txtDate.setText(mExpense.getDate());
            binding.editTextAmount.setText(mExpense.getAmount());
            binding.editTextDescription.setText(mExpense.getDecription());
            mParticularSelect = particularList.indexOf(mExpense.getPartuculars());
            binding.spinnerParticular.setSelection(mParticularSelect);
            if (mExpense.getAttachmentPresent().equalsIgnoreCase("true")) {
                Glide.with(ExpenseNewActivity.this)
                        .load(Constants.IMAGEURL + mExpense.getId() + ".png")
                        .placeholder(getResources().getDrawable(R.drawable.mulya_bg))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.addImage);
            }
        }
        // hiding views for team mgmt section
        if (Constants.AccountTeamCode.equals("TeamManagement")) {

        }
        // to deseble and hide views for team mgmt section
        if (Constants.AccountTeamCode.equals("TeamManagement")) {
            binding.linearly.setVisibility(View.VISIBLE);
            binding.btnSubmit.setVisibility(View.GONE);
            binding.btnApprove.setOnClickListener(this);
            binding.btnReject.setOnClickListener(this);

            binding.txtDate.setEnabled(false);
            binding.spinnerParticular.setEnabled(false);
            binding.editTextAmount.setEnabled(false);
            binding.editTextDescription.setEnabled(false);

            binding.tvAddAttchment.setVisibility(View.GONE);
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
                changeExpense("Rejected");
                break;
            case R.id.btn_approve:
                changeExpense(Constants.USERACTION);
                break;
        }
    }

    private void changeExpense(String status) {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                Expense expense = mExpense;
                expense.setStatus(status);
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(expense);
                JSONObject jsonObject1 = new JSONObject(json);
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertExpense", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response != null && response.isSuccess()) {
                                if (response.body() != null) {
                                    String data = response.body().string();
                                    if (data != null && data.length() > 0) {
                                        Utills.showToast("Status of expense changed successfully", ExpenseNewActivity.this);
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

    private void setActionbar(String Title) {

        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        String str = Title;
        if (Title != null && Title.contains("\n"))
            str = Title.replace("\n", " ");
        toolbar_title.setText(str);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.GONE);
        img_list.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
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
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                try {
                    if (FinalUri != null) {
                        InputStream iStream = null;
                        iStream = getContentResolver().openInputStream(FinalUri);
                        img_str = Base64.encodeToString(Utills.getBytes(iStream), 0);
                        expense.setAttachmentPresent("true");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(expense);
                JSONObject jsonObject1 = new JSONObject(json);
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertExpense", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response != null && response.isSuccess()) {
                                if (response.body() != null) {
                                    String data = response.body().string();
                                    if (data != null && data.length() > 0) {
                                        JSONObject object = new JSONObject(data);
                                        JSONArray array = object.getJSONArray("Records");
                                        if (array.length() != 0) {
                                            expense.setId(array.getJSONObject(0).getString("Id"));
                                            expense.setStatus("Pending");
                                            AppDatabase.getAppDatabase(ExpenseNewActivity.this).userDao().insertExpense(expense);
                                            if (FinalUri != null) {
                                                JSONObject object2 = new JSONObject();
                                                stringId = array.getJSONObject(0).getString("Id");
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
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.DeleteAccountData + "?Id=" + Id + "&Object=Expense";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.contains("deleted")) {

                        }
                    }

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

    private void sendImageToServer(JSONArray jsonArray) {
        Utills.showProgressDialog(this);
        JsonParser jsonParser = new JsonParser();
        JsonArray gsonObject = (JsonArray) jsonParser.parse(jsonArray.toString());
        ServiceRequest apiService =
                ApiClient.getImageClient().create(ServiceRequest.class);
        apiService.sendImageToSalesforce(Constants.New_upload_phpUrl, gsonObject).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    String str = response.body().string();
                    JSONObject object = new JSONObject(str);
                    if (object.has("status")) {
                        if (object.getString("status").equalsIgnoreCase("1")) {
                            Utills.showToast("Expense Added successfully", getApplicationContext());
                            finish();
                            overridePendingTransition(R.anim.left_in, R.anim.right_out);
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
        if (i < 10)
            return "0" + i;
        return "" + i;
    }

    private void showDateDialog() {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        binding.txtDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                    }
                }, mYear, mMonth, mDay);
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
