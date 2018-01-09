package com.mv.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Activity.CommentActivity;
import com.mv.Activity.CommunityDetailsActivity;
import com.mv.Activity.CommunityHomeActivity;
import com.mv.Model.Content;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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


/**
 * Created by acer on 6/7/2016.
 */

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {
    private static final int LENGTH = 7;
    private final String[] mPlaces;
    private final String[] mPlaceDesc;
    private final Drawable[] mPlacePictures;
    private final Context mContext;
    private CommunityHomeActivity mActivity;
    private List<Content> mDataList;
    private PreferenceHelper preferenceHelper;
    private int mPosition;
    private boolean[] mSelection = null;
    private String value;
    private JSONArray jsonArrayAttchment = new JSONArray();
    private Bitmap theBitmap;
    private static final Pattern urlPattern = Pattern.compile( "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public ContentAdapter(Context context, List<Content> chatList) {
        Resources resources = context.getResources();
        mPlaces = resources.getStringArray(R.array.places);
        mActivity = (CommunityHomeActivity) context;
        mPlaceDesc = resources.getStringArray(R.array.place_desc);
        TypedArray a = resources.obtainTypedArray(R.array.places_picture);
        mContext = context;
        mPlacePictures = new Drawable[a.length()];
        for (int i = 0; i < mPlacePictures.length; i++) {
            mPlacePictures[i] = a.getDrawable(i);
        }
        a.recycle();
        preferenceHelper = new PreferenceHelper(mContext);
        this.mDataList = chatList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_content, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

       /* Glide.with(mContext)
                .load(getUrlWithHeaders(new PreferenceHelper(mContext).getString(PreferenceHelper.InstanceUrl)+"services/data/v20.0/sobjects/Attachment/"+mDataList.get(position).getId()))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.picture);
*/

        if (TextUtils.isEmpty(mDataList.get(position).getUserAttachmentId())) {
            holder.userImage.setImageResource(R.drawable.logomulya);
        } else if (mDataList.get(position).getAttachmentId().equalsIgnoreCase("null")) {
            holder.userImage.setImageResource(R.drawable.logomulya);
        } else {
            Glide.with(mContext)
                    .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mDataList.get(position).getUserAttachmentId() + "/Body"))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.logomulya))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.userImage);
            // holder.picture.setImageDrawable(mPlacePictures[position % mPlacePictures.length]);
        }
        if (mDataList.get(position).getIsAttachmentPresent() == null || TextUtils.isEmpty(mDataList.get(position).getIsAttachmentPresent()) || mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("false")) {
            if (TextUtils.isEmpty(mDataList.get(position).getAttachmentId())) {
                holder.mediaLayout.setVisibility(View.GONE);
                holder.layout_download.setVisibility(View.VISIBLE);
            } else if (mDataList.get(position).getAttachmentId().equalsIgnoreCase("null")) {
                holder.mediaLayout.setVisibility(View.GONE);
                holder.layout_download.setVisibility(View.VISIBLE);
            } else {
                holder.mediaLayout.setVisibility(View.VISIBLE);
                holder.layout_download.setVisibility(View.VISIBLE);
                // holder.picture.setImageDrawable(mPlacePictures[position % mPlacePictures.length]);
                if (mDataList.get(position).getSynchStatus() != null
                        && mDataList.get(position).getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image" + "/" + mDataList.get(position).getAttachmentId() + ".png");
                    if (file.exists()) {
                        Glide.with(mContext)
                                .load(Uri.fromFile(file))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.picture);
                    }

                } else {
                    Glide.with(mContext)
                            .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mDataList.get(position).getAttachmentId() + "/Body"))
                            .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.picture);
                }
            }
        } else {
            holder.mediaLayout.setVisibility(View.VISIBLE);
            holder.layout_download.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load("http://13.58.218.106/images/" + mDataList.get(position).getId() + ".png")
                    .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.picture);
        }
        holder.txt_title.setText("" + mDataList.get(position).getUserName());
       /* if (mDataList.get(position).getSynchStatus() != null
                && mDataList.get(position).getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL))
            holder.txt_template_type.setText("Template Type : " + mDataList.get(position).getTemplateName());
        else
            holder.txt_template_type.setText("Template Type : " + mDataList.get(position).getTemplate());*/
        holder.txt_template_type.setText("Title : " + mDataList.get(position).getTitle());
        holder.txt_desc.setText("Description : " + mDataList.get(position).getDescription());
        Linkify.addLinks(holder.txt_desc,urlPattern,mDataList.get(position).getDescription());
        holder.txt_time.setText(mDataList.get(position).getTime().toString());
        holder.txtLikeCount.setText(mDataList.get(position).getLikeCount() + " Likes");
        holder.txtCommentCount.setText(mDataList.get(position).getCommentCount() + " Comments");

        holder.txt_type.setText(mDataList.get(position).getIssue_priority());
        if (mDataList.get(position).getIsLike())
            holder.imgLike.setImageResource(R.drawable.like);
        else
            holder.imgLike.setImageResource(R.drawable.dislike);

        if (mDataList.get(position).getCommentCount() == 0) {
            holder.img_comment.setImageResource(R.drawable.no_comment);
        } else {
            holder.img_comment.setImageResource(R.drawable.comment);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    GlideUrl getUrlWithHeaders(String url) {
//
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
    }

    public void showGroupDialog(final int position) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setIcon(R.drawable.logomulya);
        builderSingle.setTitle("Select Communities");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < mActivity.communityList.size(); i++) {
            arrayAdapter.add(mActivity.communityList.get(i).getName());
        }

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builderSingle.show();
    }

    private void showDialog(final int position) {
        final String[] items = new String[mActivity.communityList.size()];
        for (int i = 0; i < mActivity.communityList.size(); i++) {
            items[i] = mActivity.communityList.get(i).getName();
        }
        mSelection = new boolean[items.length];
        Arrays.fill(mSelection, false);

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(mContext)
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
                        sendShareRecord(mDataList.get(position).getId());
                        Log.i("value", "value");
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                jsonArrayAttchment.put(mActivity.communityList.get(i).getId());
                sb.append(i);
            }
        }
        return sb.toString();
    }

    private void sendShareRecord(String contentId) {
        if (Utills.isConnected(mContext)) {
            try {


                Utills.showProgressDialog(mContext, "Sharing Post...", "Please wait");
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("userId", User.getCurrentUser(mContext).getId());
                jsonObject1.put("contentId", contentId);


                //  jsonArrayAttchment.put(communityId);
                // jsonObject1.put("MV_User", User.getCurrentUser(mContext).getId());
                jsonObject1.put("grId", jsonArrayAttchment);


                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/sharedRecords", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            Utills.showToast("Post Share Successfully...", mContext);
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);

            }
        } else {
            Utills.showToast(mContext.getString(R.string.error_no_internet), mContext);
        }
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView picture, userImage, imgLike, img_comment;
        public CardView card_view;
        public TextView txt_title, txt_template_type, txt_desc, txt_time, textViewLike, txtLikeCount, txtCommentCount, txt_type;
        public LinearLayout layout_like, mediaLayout, layout_comment, layout_share, layout_download;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txt_title = (TextView) itemLayoutView.findViewById(R.id.txt_title);
            txt_template_type = (TextView) itemLayoutView.findViewById(R.id.txt_template_type);
            txt_desc = (TextView) itemLayoutView.findViewById(R.id.txt_desc);
            txt_time = (TextView) itemLayoutView.findViewById(R.id.txt_time);
            txtLikeCount = (TextView) itemLayoutView.findViewById(R.id.txtLikeCount);
            txtCommentCount = (TextView) itemLayoutView.findViewById(R.id.txtCommentCount);
            userImage = (ImageView) itemLayoutView.findViewById(R.id.userImage);
            picture = (ImageView) itemLayoutView.findViewById(R.id.card_image);
            card_view = (CardView) itemLayoutView.findViewById(R.id.card_view);
            imgLike = (ImageView) itemLayoutView.findViewById(R.id.imgLike);
            textViewLike = (TextView) itemLayoutView.findViewById(R.id.textViewLike);
            img_comment = (ImageView) itemLayoutView.findViewById(R.id.img_comment);
            layout_comment = (LinearLayout) itemLayoutView.findViewById(R.id.layout_comment);
            mediaLayout = (LinearLayout) itemLayoutView.findViewById(R.id.mediaLayout);
            txt_type = (TextView) itemLayoutView.findViewById(R.id.txt_type);
            layout_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, CommentActivity.class);
                    intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                    mContext.startActivity(intent);
                }
            });

            layout_share = (LinearLayout) itemLayoutView.findViewById(R.id.layout_share);
            layout_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getSynchStatus()) && mDataList.get(getAdapterPosition()).getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {
                        Utills.showToast(mContext.getString(R.string.error_offline_share_post), mContext);
                    } else {
                        if (Utills.isConnected(mContext)) {
                            showDialog(getAdapterPosition());
                            // showGroupDialog(getAdapterPosition());
                        } else {
                            Utills.showToast(mContext.getString(R.string.error_no_internet), mContext);
                        }
                    }

                }
            });
            layout_download = (LinearLayout) itemLayoutView.findViewById(R.id.layout_download);
            layout_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getAttachmentId())) {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image*//**//*");
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());

                        mContext.startActivity(Intent.createChooser(i, "Share Post"));

                    } else if (mDataList.get(getAdapterPosition()).getAttachmentId().equalsIgnoreCase("null")) {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image*//**//*");
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());

                        mContext.startActivity(Intent.createChooser(i, "Share Post"));

                    } else if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent() == null
                            || TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getIsAttachmentPresent())
                            || mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("false")) {
                        downloadImage(getAdapterPosition());
                    } else {

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected void onPreExecute() {
                                theBitmap = null;
                            }


                            @Override

                            protected Void doInBackground(Void... params) {
                                try {
                                    theBitmap = Glide.
                                            with(mContext).
                                            load("http://13.58.218.106/images/" + mDataList.get(getAdapterPosition()).getId() + ".png").

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
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("image*//**//*");
                                    i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());
                                    i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(theBitmap, getAdapterPosition()));
                                    Utills.hideProgressDialog();
                                    mContext.startActivity(Intent.createChooser(i, "Share Post"));
                                }
                            }
                        }.execute();
                    }
                }
            });
            layout_like = (LinearLayout) itemLayoutView.findViewById(R.id.layout_like);
            layout_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getSynchStatus()) && mDataList.get(getAdapterPosition()).getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {
                        Utills.showToast(mContext.getString(R.string.error_offline_like_post), mContext);
                    } else {
                        mPosition = getAdapterPosition();
                        if (Utills.isConnected(mContext)) {


                            if (!mDataList.get(getAdapterPosition()).getIsLike()) {
                                sendLikeAPI(mDataList.get(getAdapterPosition()).getId(), !(mDataList.get(getAdapterPosition()).getIsLike()));
                                mDataList.get(mPosition).setIsLike(!mDataList.get(mPosition).getIsLike());
                                mDataList.get(mPosition).setLikeCount((mDataList.get(mPosition).getLikeCount() + 1));
                                notifyDataSetChanged();
                            } else {
                                sendDisLikeAPI(mDataList.get(getAdapterPosition()).getId(), !(mDataList.get(getAdapterPosition()).getIsLike()));
                                mDataList.get(mPosition).setIsLike(!mDataList.get(mPosition).getIsLike());
                                mDataList.get(mPosition).setLikeCount((mDataList.get(mPosition).getLikeCount() - 1));
                                notifyDataSetChanged();
                            }
                        } else {
                            Utills.showToast(mContext.getString(R.string.error_no_internet), mContext);
                        }

                    }

                }
            });
            card_view.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                    intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                    intent.putExtra("flag", "forward_flag");
                    intent.putExtra(Constants.LIST, mActivity.json);
                    mContext.startActivity(intent);
                }
            });

            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utills.showImageZoomInDialog(v.getContext(),mDataList.get(getAdapterPosition()).getId());
                }
            });
        }


    }

    private void downloadImage(final int adapterPosition) {
       /**/

        if (Utills.isConnected(mContext)) {

            Utills.showProgressDialog(mContext, "Please wait", "Loading Image");

            ServiceRequest apiService =
                    ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
            String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getAttachmentBody/" + mDataList.get(adapterPosition).getAttachmentId();
            apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    try {
                        String str = response.body().string();
                        byte[] decodedString = Base64.decode(str, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image*//**//*");
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(adapterPosition).getTitle() + "\n\nDescription : " + mDataList.get(adapterPosition).getDescription());
                        i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(decodedByte, adapterPosition));
                        Utills.hideProgressDialog();
                        mContext.startActivity(Intent.createChooser(i, "Share Post"));
                    } catch (Exception e) {
                        Utills.hideProgressDialog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Utills.hideProgressDialog();
                    Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);
                }
            });
        } else {
            Utills.showInternetPopUp(mContext);
        }

    }

    public Uri getLocalBitmapUri(Bitmap bmp, int mPosition) {
        Uri bmpUri = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mDataList.get(mPosition).getId() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void sendDisLikeAPI(String cotentId, boolean isLike) {
        if (Utills.isConnected(mContext)) {
            try {


                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("Is_Like", isLike);
                jsonObject1.put("MV_Content", cotentId);
                jsonObject1.put("MV_User", User.getCurrentUser(mContext).getId());

                jsonArray.put(jsonObject1);
                jsonObject.put("listVisitsData", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/removeLike", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {

                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);

            }
        } else {
            Utills.showToast(mContext.getString(R.string.error_no_internet), mContext);
        }
    }

    private void sendLikeAPI(String cotentId, Boolean isLike) {
        if (Utills.isConnected(mContext)) {
            try {


                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("Is_Like__c", isLike);
                jsonObject1.put("MV_Content__c", cotentId);
                jsonObject1.put("MV_User__c", User.getCurrentUser(mContext).getId());

                jsonArray.put(jsonObject1);
                jsonObject.put("contentlikeList", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertLike", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {

                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(mContext.getString(R.string.error_something_went_wrong), mContext);

            }
        } else {
            Utills.showToast(mContext.getString(R.string.error_no_internet), mContext);
        }
    }
}

