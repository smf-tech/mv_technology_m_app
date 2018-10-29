package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.mv.Activity.CommentActivity;
import com.mv.Model.Comment;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;


/**
 * Created by acer on 6/7/2016.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private static final int LENGTH = 7;

    private final Context mContext;
    private CommentActivity mActivity;
    private ArrayList<Comment> mDataList;
    private PreferenceHelper preferenceHelper;
    private PopupMenu popup;
    private int mPosition;

    public CommentAdapter(Context context, ArrayList<Comment> chatList) {
        Resources resources = context.getResources();
        mActivity = (CommentActivity) context;

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
            holder.userImage.setImageResource(R.drawable.logomulya);
        } else {
            Glide.with(mContext)
                    .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mDataList.get(position).getUserUrl() + "/Body"))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.logomulya))
                    .into(holder.userImage);
        }
        holder.txt_username.setText(mDataList.get(position).getUserName());
        holder.txt_comment.setText(mDataList.get(position).getComment());
        holder.txt_time.setText(mDataList.get(position).getTime());

        //added for comment edit,delete option
        String id=mDataList.get(position).getUserId();
        if(mDataList.get(position).getUserId()!=null && mDataList.get(position).getUserId().equals(User.getCurrentUser(mActivity).getMvUser().getId()))
            holder.imgMore.setVisibility(View.VISIBLE);
        else
            holder.imgMore.setVisibility(View.GONE);
        holder.imgMore.setOnClickListener(view -> {
            popup = new PopupMenu(mContext, holder.imgMore);
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());
            //   popup.getMenu().getItem(R.id.spam).setVisible(true);
            MenuItem spam = popup.getMenu().findItem(R.id.spam);
            MenuItem edit = popup.getMenu().findItem(R.id.edit);
            MenuItem delete = popup.getMenu().findItem(R.id.delete);
            MenuItem status = popup.getMenu().findItem(R.id.status);
            spam.setVisible(false);
            status.setVisible(false);

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equalsIgnoreCase("Delete")) {
                    showDeleteDialog(position);

                } else if (item.getTitle().toString().equalsIgnoreCase("Edit")) {
                    mActivity.editComment(mDataList.get(position).getId(),mDataList.get(position).getComment());
                }
                return true;
            });
            popup.show();
        });

    }
    //delete comment dialog box
    private void showDeleteDialog(int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(R.string.cancel), (dialog, which) -> alertDialog.dismiss());
        // Setting OK Button
        alertDialog.setButton(mContext.getString(R.string.ok), (dialog, which) -> mActivity.deleteComment(mDataList.get(position).getId()));

        // Showing Alert Message
        alertDialog.show();
    }


    private GlideUrl getUrlWithHeaders(String url) {
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
        public ImageView userImage,imgMore;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txt_username = itemLayoutView.findViewById(R.id.txt_username);
            txt_comment = itemLayoutView.findViewById(R.id.txt_comment);
            txt_time = itemLayoutView.findViewById(R.id.txt_time);
            userImage = itemLayoutView.findViewById(R.id.userImage);
            imgMore = itemLayoutView.findViewById(R.id.imgMore);
        }


    }


}

