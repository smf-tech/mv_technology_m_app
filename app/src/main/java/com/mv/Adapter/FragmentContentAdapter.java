package com.mv.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Activity.CommentActivity;
import com.mv.Activity.CommunityDetailsActivity;
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
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by acer on 6/7/2016.
 */

public class FragmentContentAdapter extends RecyclerView.Adapter<FragmentContentAdapter.ViewHolder> {
    private static final int LENGTH = 7;
    private final String[] mPlaces;
    private final String[] mPlaceDesc;
    private final Drawable[] mPlacePictures;
    private final Context mContext;

    private ArrayList<Content> mDataList;
    private PreferenceHelper preferenceHelper;
    private int mPosition;
    private static final Pattern urlPattern = Pattern.compile( "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public FragmentContentAdapter(Context context, ArrayList<Content> chatList) {
        Resources resources = context.getResources();
        mPlaces = resources.getStringArray(R.array.places);
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
        holder.layout_share.setVisibility(View.GONE);
       /* Glide.with(mContext)
                .load(getUrlWithHeaders(new PreferenceHelper(mContext).getString(PreferenceHelper.InstanceUrl)+"services/data/v20.0/sobjects/Attachment/"+mDataList.get(position).getId()))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.picture);
*/
        if (TextUtils.isEmpty(mDataList.get(position).getAttachmentId())
                || mDataList.get(position).getAttachmentId().equalsIgnoreCase("null")) {
            holder.mediaLayout.setVisibility(View.GONE);


        } else {
            holder.mediaLayout.setVisibility(View.VISIBLE);

            // holder.picture.setImageDrawable(mPlacePictures[position % mPlacePictures.length]);
            Glide.with(mContext)
                    .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mDataList.get(position).getAttachmentId() + "/Body"))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                    .into(holder.picture);
        }
        if (TextUtils.isEmpty(mDataList.get(position).getUserAttachmentId())
                || mDataList.get(position).getAttachmentId().equalsIgnoreCase("null")) {
            holder.userImage.setImageResource(R.drawable.logomulya);
        } else {

            // holder.picture.setImageDrawable(mPlacePictures[position % mPlacePictures.length]);
            Glide.with(mContext)
                    .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mDataList.get(position).getUserAttachmentId() + "/Body"))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.logomulya))
                    .into(holder.userImage);
        }
        Log.i("URL for position : " + position, "" + mDataList.get(position).getUserAttachmentId());
        holder.txt_title.setVisibility(View.GONE);
        // holder.txt_title.setText("Title : " + mDataList.get(position).getTitle());
        holder.txt_template_type.setText("Title : " + mDataList.get(position).getTitle());
        holder.txt_desc.setText("Description : " + mDataList.get(position).getDescription());
        Linkify.addLinks(holder.txt_desc,urlPattern,mDataList.get(position).getDescription());
        holder.txt_time.setText(mDataList.get(position).getTime().toString());
        holder.txtLikeCount.setText(mDataList.get(position).getLikeCount() + " Likes");
        holder.txtCommentCount.setText(mDataList.get(position).getCommentCount() + " Comments");
        holder.img_share.setImageResource(R.drawable.download);
        holder.txt_forward.setText(mContext.getString(R.string.Share));
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


    private void sendShareRecord(String contentId, String communityId) {
        if (Utills.isConnected(mContext)) {
            try {


                Utills.showProgressDialog(mContext, "Sharing Post...", "Please wait");
                JSONObject jsonObject1 = new JSONObject();

                jsonObject1.put("userId", User.getCurrentUser(mContext).getId());
                jsonObject1.put("contentId", contentId);

                JSONArray jsonArrayAttchment = new JSONArray();

                jsonArrayAttchment.put(communityId);
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
        public ImageView picture, userImage, imgLike, img_share, img_comment;
        public CardView card_view;
        public TextView txt_title, txt_template_type, txt_desc, txt_time, textViewLike, txtLikeCount, txtCommentCount, txt_forward;
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
            img_share = (ImageView) itemLayoutView.findViewById(R.id.img_share);
            img_comment = (ImageView) itemLayoutView.findViewById(R.id.img_comment);
            txt_forward = (TextView) itemLayoutView.findViewById(R.id.txt_forward);
            layout_comment = (LinearLayout) itemLayoutView.findViewById(R.id.layout_comment);
            layout_download = (LinearLayout) itemLayoutView.findViewById(R.id.layout_download);
            layout_like = (LinearLayout) itemLayoutView.findViewById(R.id.layout_like);
            layout_share = (LinearLayout) itemLayoutView.findViewById(R.id.layout_share);
            mediaLayout = (LinearLayout) itemLayoutView.findViewById(R.id.mediaLayout);
            layout_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, CommentActivity.class);
                    intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                    mContext.startActivity(intent);
                }
            });

            layout_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //showGroupDialog(getAdapterPosition());
                    if (TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getAttachmentId())
                            || mDataList.get(getAdapterPosition()).getAttachmentId().equalsIgnoreCase("null")) {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image*//**//*");
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());
                        Utills.hideProgressDialog();
                        mContext.startActivity(Intent.createChooser(i, "Share Post"));
                    } else {
                        downloadImage(getAdapterPosition());
                    }
                }
            });

            layout_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utills.isConnected(mContext)) {
                        mPosition = getAdapterPosition();
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
            });
            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                    intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                    intent.putExtra("flag", "not_forward_flag");

                    mContext.startActivity(intent);
                }
            });

            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utills.showImagewithheaderZoomDialog(v.getContext(),getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mDataList.get(getAdapterPosition()).getAttachmentId() + "/Body"));
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
                    Utills.hideProgressDialog();
                    try {
                        String str = response.body().string();
                        byte[] decodedString = Base64.decode(str, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        Utills.showToast("Image Downloaded Successfully...", mContext);

                        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                        shareIntent.setType("text/html");
                        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Title : " + mDataList.get(adapterPosition).getTitle() + "\n\nDescription : " + mDataList.get(adapterPosition).getDescription());
                        shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(decodedByte));
                        mContext.startActivity(Intent.createChooser(shareIntent, "Share Content"));
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

