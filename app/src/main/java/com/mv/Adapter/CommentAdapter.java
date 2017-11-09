package com.mv.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.mv.Activity.CommunityDetailsActivity;
import com.mv.Model.Comment;
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

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by acer on 6/7/2016.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private static final int LENGTH = 7;

    private final Context mContext;
    private ArrayList<Comment> mDataList;
    private PreferenceHelper preferenceHelper;
    private int mPosition;

    public CommentAdapter(Context context, ArrayList<Comment> chatList) {
        Resources resources = context.getResources();

        TypedArray a = resources.obtainTypedArray(R.array.places_picture);
        mContext = context;

        preferenceHelper = new PreferenceHelper(mContext);
        this.mDataList = chatList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_comment, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (TextUtils.isEmpty(mDataList.get(position).getUserUrl())
                || mDataList.get(position).getUserUrl().equalsIgnoreCase("null")) {
        } else {
            Glide.with(mContext)
                    .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mDataList.get(position).getUserUrl() + "/Body"))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.logomulya))
                    .into(holder.userImage);
        }

        holder.txt_username.setText(mDataList.get(position).getUserName());
        holder.txt_comment.setText(mDataList.get(position).getComment());
        holder.txt_time.setText(mDataList.get(position).getTime());

    }


    GlideUrl getUrlWithHeaders(String url) {
//
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txt_username, txt_comment, txt_time;
        public ImageView userImage;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txt_username = (TextView) itemLayoutView.findViewById(R.id.txt_username);
            txt_comment = (TextView) itemLayoutView.findViewById(R.id.txt_comment);
            txt_time = (TextView) itemLayoutView.findViewById(R.id.txt_time);
            userImage = (ImageView) itemLayoutView.findViewById(R.id.userImage);
        }


    }


}

