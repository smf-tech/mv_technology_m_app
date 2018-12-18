package com.mv.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Adapter.CommentAdapter;
import com.mv.Model.Comment;
import com.mv.Model.TaskContainerModel;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityCommentBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityCommentBinding binding;
    private ArrayList<Comment> commentList = new ArrayList<>();
    private CommentAdapter adapter;
    private String conetentId;
    private String intentActivity = "";
    private PreferenceHelper preferenceHelper;
    private TextView textNoData;
    public String HoSupportCommunity = "";
    private String commentId;
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initUI();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
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
        binding.imgClear.setOnClickListener(this);

    }

    private void initUI() {
        setActionbar("Comments");

        Utills.setupUI(findViewById(R.id.layout_main), this);
        textNoData = (TextView) findViewById(R.id.textNoData);

        preferenceHelper = new PreferenceHelper(this);

        adapter = new CommentAdapter(binding.recyclerView.getContext(), commentList);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.imgSend.setOnClickListener(this);
        if(getIntent().getExtras()!=null) {
            conetentId = getIntent().getExtras().getString(Constants.ID);
            intentActivity = getIntent().getExtras().getString("intentFrom");
        }

        if (Utills.isConnected(this)) {
            getComments(true);
        } else {
            showPopUp();
        }
    }

    private void getComments(boolean showDialog) {
        if (showDialog)
            Utills.showProgressDialog(this, getString(R.string.loading_comment), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url;
        if(intentActivity.equals("ProcessDeatailActivity")){
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getProcessAnswerComments?ProcessAnswerId=" + conetentId;
        }else{
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getComments?contentId=" + conetentId + "&userId=" + User.getCurrentUser(this).getMvUser().getId();
        }
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                //binding.swipeRefreshLayout.setRefreshing(false);
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    commentList.clear();
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    List<Comment> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Comment[].class));
                    if (temp.size() != 0) {
                        commentList.addAll(temp);
                        textNoData.setVisibility(View.GONE);
                    } else {
                        textNoData.setVisibility(View.VISIBLE);
                    }
                    //save count of read comments in form db
                   // TaskContainerModel taskContainerModel = new TaskContainerModel();
                   // taskContainerModel.setUnique_Id(conetentId);
                   // taskContainerModel.setUnique_Id(preferenceHelper.getString(Constants.UNIQUE));
                   // taskContainerModel.setFormReadCommentCount(String.valueOf(temp.size()));
                   // AppDatabase.getAppDatabase(context).userDao().updateFormReadCommentCount(preferenceHelper.getString(Constants.UNIQUE), String.valueOf(temp.size()));
                    // List<TaskContainerModel> resultList = new ArrayList<>();
                    if(preferenceHelper.getString(Constants.PROCESS_TYPE).equals(Constants.MANGEMENT_PROCESS) && temp.size() != 0) {
                        TaskContainerModel taskContainerModel;
                        taskContainerModel = AppDatabase.getAppDatabase(CommentActivity.this).userDao().getTaskByUniqueId(preferenceHelper.getString(Constants.UNIQUE));
                        //        TaskContainerModel taskContainerModel = resultList.get(0);
                            taskContainerModel.setFormReadCommentCount(String.valueOf(temp.size()));
                        AppDatabase.getAppDatabase(context).userDao().updateTask(taskContainerModel);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                //  binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.imgSend:
                if (binding.edtComment.getText().toString().trim().length() != 0) {
                    sendComment();
                } else {
                    Utills.showToast(getString(R.string.please_comment), CommentActivity.this);
                }
                break;
            case R.id.imgClear:
                binding.edtComment.setText("");
                binding.imgClear.setVisibility(View.GONE);
                commentId=null;
                break;
        }
    }

    public  void editComment(String commentId, String commentData){
        this.commentId = commentId;
        binding.edtComment.setText(commentData);
        binding.imgClear.setVisibility(View.VISIBLE);
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(binding.edtComment, InputMethodManager.SHOW_IMPLICIT);
        // Request focus and show soft keyboard automatically
     //   getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
    //to delete the comment

    private void sendComment() {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();

                if(commentId!=null)
                    jsonObject1.put("Id", commentId);

                jsonObject1.put("Comment__c", binding.edtComment.getText().toString().trim());
                jsonObject1.put("MV_User__c", User.getCurrentUser(this).getMvUser().getId());

                String url;
                if(intentActivity.equals("ProcessDeatailActivity")){
                    url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertProcessAnswerComments";
                    jsonObject1.put("ProcessAnswer__c", conetentId);
                }else{
                    url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertComments";
                    jsonObject1.put("MV_Content__c", conetentId);
                }
                jsonArray.put(jsonObject1);
                jsonObject.put("listOfComments", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(url, gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                           // JSONArray jsonArray = new JSONArray(response.body().string());
                            if (Utills.isConnected(CommentActivity.this)) {
                                getComments(true);
                            }
//                            Comment comment = new Comment();
//                            comment.setComment(binding.edtComment.getText().toString().trim());
//                            comment.setUserName(User.getCurrentUser(CommentActivity.this).getMvUser().getName());
//                            comment.setUserUrl(User.getCurrentUser(CommentActivity.this).getMvUser().getImageId());
//                            Calendar c = Calendar.getInstance();
//                            System.out.println("Current time => " + c.getTime());
//                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                            String formattedDate = df.format(c.getTime());
//                            comment.setTime(formattedDate);
//                            commentList.add(0, comment);
//                            adapter.notifyDataSetChanged();
                            binding.imgClear.setVisibility(View.GONE);
                            commentId=null;
                            textNoData.setVisibility(View.GONE);
                            Utills.showToast(getString(R.string.comment_add), getApplicationContext());
                            binding.edtComment.setText("");
                            Utills.hideSoftKeyboard(CommentActivity.this);
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
    public void deleteComment(String id) {
        if (Utills.isConnected(this)) {

            Utills.showProgressDialog(this);
//                JSONObject jsonObject = new JSONObject();
//                JSONArray jsonArray = new JSONArray();
//                JSONObject jsonObject1 = new JSONObject();
//
//                    jsonObject1.put("commentId", id);

            String url;
            if(intentActivity.equals("ProcessDeatailActivity")){
                url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) +"/services/apexrest/DeleteProcessAnswerComment/"+id;
            }else{
                url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/WS_DeleteComments?commentId="+id;
            }
            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
            JsonParser jsonParser = new JsonParser();
//                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        if (Utills.isConnected(CommentActivity.this)) {
                            getComments(true);
                        }

                        Utills.showToast(getString(R.string.comment_delete), getApplicationContext());
                        binding.edtComment.setText("");
                        Utills.hideSoftKeyboard(CommentActivity.this);
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
        } else {
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }

    }
}
