package com.mv.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.Community;
import com.mv.Model.Content;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityCommunityDetailsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityCommunityDetailsBinding binding;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private Content mContent;
    private PreferenceHelper preferenceHelper;
    private List<Community> communityList = new ArrayList<>();
    LinearLayout layout_forward, layout_download_file, layout_share;
    private boolean[] mSelection = null;
    String value;
    private JSONArray jsonArrayAttchment = new JSONArray();
    private static final Pattern urlPattern = Pattern.compile("(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private Bitmap theBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_community_details);
        binding.setActivity(this);
        initViews();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.layout_comment:
                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra(Constants.ID, mContent.getId());
                startActivity(intent);
                break;
            case R.id.layout_download_file:
                if (mContent.getIsAttachmentPresent() != null) {

                    if (mContent.getIsAttachmentPresent().equalsIgnoreCase("true")) {

                        if (mContent.getAttachmentId() == null) {

                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected void onPreExecute() {
                                    Utills.showProgressDialog(CommunityDetailsActivity.this, "Downloading", getString(R.string.progress_please_wait));

                                    theBitmap = null;
                                }


                                @Override

                                protected Void doInBackground(Void... params) {
                                    try {
                                        theBitmap = Glide.
                                                with(getApplicationContext()).
                                                load("http://mobileapp.mulyavardhan.org/images/" + mContent.getId() + ".png").

                                                asBitmap().
                                                into(200, 200).
                                                get();
                                    } catch (final ExecutionException e) {

                                    } catch (final InterruptedException e) {

                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void dummy) {
                                    if (theBitmap != null) {
                                        Utills.hideProgressDialog();


                                        try {
                                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mContent.getId() + ".png");
                                            FileOutputStream out;
                                            out = new FileOutputStream(file);
                                            theBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                                            out.close();


                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                   /* Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("image*//**//**//**//*");
                                    i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());
                                    i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(theBitmap, getAdapterPosition()));
                                    Utills.hideProgressDialog();
                                    mContext.startActivity(Intent.createChooser(i, "Share Post"));*/
                                    } else {
                                        Utills.hideProgressDialog();
                                    }
                                }
                            }.execute();
                        } else {
                            downloadImage();
                        }
                    }
                } else {
                    if (mContent.getAttachmentId() != null) {
                        downloadImage();
                    }
                }

                break;
            case R.id.layout_share:
                if (mContent.getIsAttachmentPresent() != null) {

                    if (mContent.getIsAttachmentPresent().equalsIgnoreCase("true")) {
                        if (mContent.getAttachmentId() == null) {
                            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mContent.getId() + ".png";
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "Title : " + mContent.getTitle() + "\n\nDescription : " + mContent.getDescription());

                            startActivity(Intent.createChooser(shareIntent, "Share Content"));
                        } else {
                            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mContent.getAttachmentId() + ".png";

                            //String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mDataList.get(getAdapterPosition()).getAttachmentId()+".png";

                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/*");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "Title : " + mContent.getTitle() + "\n\nDescription : " + mContent.getDescription());

                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                            startActivity(Intent.createChooser(shareIntent, "Share Content"));
                        }
                    } else {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image*//**//*");
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mContent.getTitle() + "\n\nDescription : " + mContent.getDescription());
                        startActivity(Intent.createChooser(i, "Share Post"));

                    }
                } else {
                    if (mContent.getAttachmentId() != null) {
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mContent.getAttachmentId() + ".png";

                        //String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mDataList.get(getAdapterPosition()).getAttachmentId()+".png";

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("application/*");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Title : " + mContent.getTitle() + "\n\nDescription : " + mContent.getDescription());

                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                        startActivity(Intent.createChooser(shareIntent, "Share Content"));
                    } else {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image*//**//*");
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mContent.getTitle() + "\n\nDescription : " + mContent.getDescription());
                        startActivity(Intent.createChooser(i, "Share Post"));

                    }
                }
                break;
            case R.id.layout_like:
                if (!mContent.getIsLike()) {
                    sendLikeAPI(mContent.getId(), !(mContent.getIsLike()));
                    mContent.setIsLike(!mContent.getIsLike());
                    mContent.setLikeCount((mContent.getLikeCount() + 1));
                    binding.likeCount.setText(mContent.getLikeCount() + " " + getString(R.string.likes));
                    binding.heart.setImageResource(R.drawable.like);
                } else {
                    sendDisLikeAPI(mContent.getId(), !mContent.getIsLike());
                    mContent.setIsLike(!mContent.getIsLike());
                    mContent.setLikeCount((mContent.getLikeCount() - 1));
                    binding.likeCount.setText(mContent.getLikeCount() + " " + getString(R.string.likes));
                    binding.heart.setImageResource(R.drawable.dislike);
                }
                break;
            case R.id.layout_forward:
                if (!TextUtils.isEmpty(mContent.getSynchStatus()) && mContent.getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {
                    Utills.showToast(getString(R.string.error_offline_share_post), CommunityDetailsActivity.this);
                } else {
                    if (Utills.isConnected(CommunityDetailsActivity.this)) {
                        showDialog();
                        // showGroupDialog(getAdapterPosition());
                    } else {
                        Utills.showToast(getString(R.string.error_no_internet), CommunityDetailsActivity.this);
                    }
                }


        }
    }

    /*
        private void sendShareRecord(String contentId, String communityId) {
            if (Utills.isConnected(this)) {
                try {
                    Utills.showProgressDialog(this, getString(R.string.share_post), getString(R.string.progress_please_wait));
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("userId", User.getCurrentUser(this).getId());
                    jsonObject1.put("contentId", contentId);
                    JSONArray jsonArrayAttchment = new JSONArray();
                    jsonArrayAttchment.put(communityId);
                    // jsonObject1.put("MV_User", User.getCurrentUser(mContext).getId());
                    jsonObject1.put("grId", jsonArrayAttchment);
                    ServiceRequest apiService =
                            ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                    JsonParser jsonParser = new JsonParser();
                    JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                    apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/sharedRecords", gsonObject).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Utills.hideProgressDialog();
                            try {
                                Utills.showToast(getString(R.string.post_share_successfully), CommunityDetailsActivity.this);
                            } catch (Exception e) {
                                Utills.hideProgressDialog();
                                Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Utills.hideProgressDialog();
                    Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                }
            } else {
                Utills.showToast(getString(R.string.error_no_internet), CommunityDetailsActivity.this);
            }
        }
    */
    private void sendShareRecord(String contentId) {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this, "Sharing Post...", "Please wait");
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("userId", User.getCurrentUser(getApplicationContext()).getMvUser().getId());
                jsonObject1.put("contentId", contentId);

                //  jsonArrayAttchment.put(communityId);
                // jsonObject1.put("MV_User", User.getCurrentUser(mContext).getId());
                jsonObject1.put("grId", jsonArrayAttchment);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/sharedRecords", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            Utills.showToast("Post Share Successfully...", CommunityDetailsActivity.this);
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CommunityDetailsActivity.this);
        }
    }

    public void showGroupDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.logomulya);
        builderSingle.setTitle(getString(R.string.select_one));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < this.communityList.size(); i++) {
            arrayAdapter.add(this.communityList.get(i).getName());
        }

        builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendShareRecord(mContent.getId());
            }
        });
        builderSingle.show();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    GlideUrl getUrlWithHeaders(String url) {
//
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
    }

    public void onPostImageClick() {
        if (mContent.getIsAttachmentPresent() == null || mContent.getIsAttachmentPresent().equalsIgnoreCase("false")) {
            if (mContent.getAttachmentId() != null) {
                Utills.showImagewithheaderZoomDialog(CommunityDetailsActivity.this, getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mContent.getAttachmentId() + "/Body"));
            }
        } else if (mContent.getId() != null) {
            Utills.showImageZoomInDialog(CommunityDetailsActivity.this, mContent.getId());
        }
    }

    public void onProfileImageClick() {


    }

    private void sendDisLikeAPI(String cotentId, boolean isLike) {
        if (Utills.isConnected(this)) {
            try {


                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("Is_Like", isLike);
                jsonObject1.put("MV_Content", cotentId);
                jsonObject1.put("MV_User", User.getCurrentUser(this).getMvUser().getId());

                jsonArray.put(jsonObject1);
                jsonObject.put("listVisitsData", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/removeLike", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {

                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CommunityDetailsActivity.this);
        }
    }

    private void sendLikeAPI(String cotentId, Boolean isLike) {
        if (Utills.isConnected(this)) {
            try {


                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("Is_Like__c", isLike);
                jsonObject1.put("MV_Content__c", cotentId);
                jsonObject1.put("MV_User__c", User.getCurrentUser(this).getMvUser().getId());

                jsonArray.put(jsonObject1);
                jsonObject.put("contentlikeList", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertLike", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {

                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), CommunityDetailsActivity.this);

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CommunityDetailsActivity.this);
        }
    }

    private void initViews() {
//        if(getIntent().getExtras().getString(Constants.TITLE)!=null && getIntent().getExtras().getString(Constants.TITLE).length()>0){
//            setActionbar(getIntent().getExtras().getString(Constants.TITLE));
//        }else{
            setActionbar(getString(R.string.comunity_detail));
      //  }
        layout_forward = (LinearLayout) findViewById(R.id.layout_forward);
        layout_download_file = (LinearLayout) findViewById(R.id.layout_download_file);
        layout_share = (LinearLayout) findViewById(R.id.layout_share);
        if (getIntent().getExtras()!=null && "forward_flag".equalsIgnoreCase(getIntent().getExtras().getString("flag"))) {
            layout_forward.setVisibility(View.VISIBLE);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = getIntent().getExtras().getString(Constants.LIST);
            communityList = Arrays.asList(gson.fromJson(json, Community[].class));
        } else {
            layout_forward.setVisibility(View.GONE);
        }
        layout_forward.setOnClickListener(this);
        String json="";
        if(getIntent().getExtras()!=null)
            json = getIntent().getExtras().getString(Constants.LIST);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if (!TextUtils.isEmpty(json))
            communityList = Arrays.asList(gson.fromJson(json, Community[].class));
        preferenceHelper = new PreferenceHelper(this);
        mContent = (Content) getIntent().getExtras().getSerializable(Constants.CONTENT);
        binding.layoutComment.setOnClickListener(this);
        binding.layoutShare.setOnClickListener(this);
        binding.layoutLike.setOnClickListener(this);
        layout_download_file.setOnClickListener(this);
        layout_share.setOnClickListener(this);

//        if (TextUtils.isEmpty(mContent.getUserAttachmentId())) {
//        } else if (mContent.getUserAttachmentId().equalsIgnoreCase("null")) {
//        }
        if(!TextUtils.isEmpty(mContent.getUserAttachmentId()) && !mContent.getUserAttachmentId().equalsIgnoreCase("null")) {
            // holder.picture.setImageDrawable(mPlacePictures[position % mPlacePictures.length]);
            Glide.with(this)
                    .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mContent.getUserAttachmentId() + "/Body"))
                    .placeholder(getResources().getDrawable(R.drawable.logomulya))
                    .into(binding.userImage);
        }
        if (mContent.getIsAttachmentPresent() == null || TextUtils.isEmpty(mContent.getIsAttachmentPresent()) || mContent.getIsAttachmentPresent().equalsIgnoreCase("false")) {

            if (TextUtils.isEmpty(mContent.getAttachmentId())) {
                binding.cardImagedetails.setVisibility(View.GONE);
            } else if (mContent.getAttachmentId().equalsIgnoreCase("null")) {
                binding.cardImagedetails.setVisibility(View.GONE);
            } else {
                binding.cardImagedetails.setVisibility(View.VISIBLE);
                if (mContent.getSynchStatus() != null
                        && mContent.getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image" + "/" + mContent.getAttachmentId() + ".png");
                    if (file.exists()) {
                        Glide.with(this)
                                .load(Uri.fromFile(file))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(binding.cardImagedetails);
                    }

                } else {
                    Glide.with(this)
                            .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mContent.getAttachmentId() + "/Body"))
                            .placeholder(getResources().getDrawable(R.drawable.mulya_bg))
                            .into(binding.cardImagedetails);
                }
                // holder.picture.setImageDrawable(mPlacePictures[position % mPlacePictures.length]);

            }
        } else {

            Glide.with(this)
                    .load("http://mobileapp.mulyavardhan.org/images/" + mContent.getId() + ".png")
                    .placeholder(getResources().getDrawable(R.drawable.mulya_bg))
                    .into(binding.cardImagedetails);
        }
        if (mContent.getIsLike())
            binding.heart.setImageResource(R.drawable.like);
        else
            binding.heart.setImageResource(R.drawable.dislike);
        if (mContent.getCommentCount() == 0) {
            binding.imgComment.setImageResource(R.drawable.no_comment);
        } else {
            binding.imgComment.setImageResource(R.drawable.comment);
        }
        binding.likeCount.setText(mContent.getLikeCount() + " " + getString(R.string.likes));
        binding.txtCommentCnt.setText(mContent.getCommentCount() + " " + getString(R.string.comments));
        binding.Title.setText(getString(R.string.title) + " : " + mContent.getTitle());

       /* if (mContent.getSynchStatus() != null
                && mContent.getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL))
            binding.type.setText(getString(R.string.template_type)+" : " + mContent.getTemplateName());
        else
            binding.type.setText(getString(R.string.template_type)+" : " + mContent.getTemplate());*/
        if (mContent.getUserName() == null)
            binding.type.setText("Admin");
        else
            binding.type.setText("" + mContent.getUserName());
        binding.Description.setText(getString(R.string.description) + " : " + mContent.getDescription());
        Linkify.addLinks(binding.Description, urlPattern, mContent.getDescription());
        binding.postDate.setText(mContent.getTime());

        // binding.userName.setText(mContent.g);
        TypedArray a = getResources().obtainTypedArray(R.array.places_picture);
        Drawable[] mPlacePictures = new Drawable[a.length()];
        for (int i = 0; i < mPlacePictures.length; i++) {
            mPlacePictures[i] = a.getDrawable(i);
        }
        a.recycle();
        //  binding.cardImagedetails.setImageDrawable(mPlacePictures[getIntent().getExtras().getInt("Position") % mPlacePictures.length]);

        if (mContent.getIsAttachmentPresent() == null
                || TextUtils.isEmpty(mContent.getIsAttachmentPresent())
                || mContent.getIsAttachmentPresent().equalsIgnoreCase("false") || isFileAvalible()) {
            layout_download_file.setVisibility(View.GONE);
            layout_share.setVisibility(View.VISIBLE);
        } else {
            layout_download_file.setVisibility(View.VISIBLE);
            layout_share.setVisibility(View.GONE);
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

    private void downloadImage() {
       /**/

        if (Utills.isConnected(getApplicationContext())) {

            Utills.showProgressDialog(CommunityDetailsActivity.this, "Please wait", "Loading Image");

            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(getApplicationContext()).create(ServiceRequest.class);
            String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getAttachmentBody/" + mContent.getAttachmentId();
            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Utills.hideProgressDialog();
                    try {
                        String str = response.body().string();
                        byte[] decodedString = Base64.decode(str, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        Utills.showToast("Image Downloaded Successfully...", getApplicationContext());
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mContent.getAttachmentId() + ".png");
                        FileOutputStream out = new FileOutputStream(file);
                        decodedByte.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.close();
                       /* Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                        shareIntent.setType("text/html");
                        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Title : " + mContent.getTitle() + "\n\nDescription : " + mContent.getDescription());
                        shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(decodedByte));
                        startActivity(Intent.createChooser(shareIntent, "Share Content"));*/
                    } catch (Exception e) {
                        Utills.hideProgressDialog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    Utills.showToast(getApplicationContext().getString(R.string.error_something_went_wrong), getApplicationContext());
                }
            });
        } else {
            Utills.showInternetPopUp(getApplicationContext());
        }

    }

    public Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/downloaded_share_image.png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void showDialog() {
        final String[] items = new String[communityList.size()];
        for (int i = 0; i < communityList.size(); i++) {
            items[i] = communityList.get(i).getName();
        }
        mSelection = new boolean[items.length];
        Arrays.fill(mSelection, false);

        final ArrayList seletedItems = new ArrayList();
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(CommunityDetailsActivity.this)
                .setTitle("Select Communities")
                .setMultiChoiceItems(items, mSelection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (mSelection != null && which < mSelection.length) {
                            mSelection[which] = isChecked;
                            value = buildSelectedItemString(items);

                        } else {
                            throw new IllegalArgumentException(
                                    "Argument 'which' is out of bounds.");
                        }
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        sendShareRecord(mContent.getId());
                        Log.i("value", "value");
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }

    private String buildSelectedItemString(String[] items) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;
        jsonArrayAttchment = new JSONArray();
        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                jsonArrayAttchment.put(communityList.get(i).getId());
                sb.append(i);
            }
        }
        return sb.toString();
    }

    private boolean isFileAvalible() {
        if (mContent.getIsAttachmentPresent().equalsIgnoreCase("true")) {
            if (mContent.getAttachmentId() == null) {
                String filepath = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mContent.getId() + ".png");
                if (new File(filepath).exists())
                    return true;
                return false;
            } else {
                String filepath = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mContent.getAttachmentId() + ".png");
                if (new File(filepath).exists())
                    return true;
                return false;
            }


        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
    }

}