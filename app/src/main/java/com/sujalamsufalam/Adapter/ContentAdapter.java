package com.sujalamsufalam.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sujalamsufalam.Activity.CommentActivity;
import com.sujalamsufalam.Activity.CommunityDetailsActivity;
import com.sujalamsufalam.Activity.CommunityHomeActivity;
import com.sujalamsufalam.Activity.IssueTemplateActivity;
import com.sujalamsufalam.Activity.ReportingTemplateActivity;
import com.sujalamsufalam.Activity.VideoViewActivity;
import com.sujalamsufalam.BuildConfig;
import com.sujalamsufalam.Model.Content;
import com.sujalamsufalam.Model.User;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.ApiClient;
import com.sujalamsufalam.Retrofit.AppDatabase;
import com.sujalamsufalam.Retrofit.ServiceRequest;
import com.sujalamsufalam.Service.DownloadService;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by acer on 6/7/2016.
 */
public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    private final Context mContext;
    private CommunityHomeActivity mActivity;
    private PreferenceHelper preferenceHelper;

    private String postId;
    private int mPosition;
    private int temp = 555500;
    private int deletePosition;

    public PopupMenu popup;
    private List<Content> mDataList;
    private boolean[] mSelection = null;

    private JSONArray jsonArrayAttachment = new JSONArray();
    private MediaPlayer mPlayer = new MediaPlayer();

    public ContentAdapter(Context context, List<Content> chatList) {
        mContext = context;
        mActivity = (CommunityHomeActivity) context;

        Resources resources = context.getResources();
        TypedArray pic = resources.obtainTypedArray(R.array.places_picture);

        Drawable[] placePictures = new Drawable[pic.length()];
        for (int i = 0; i < placePictures.length; i++) {
            placePictures[i] = pic.getDrawable(i);
        }
        pic.recycle();

        preferenceHelper = new PreferenceHelper(mContext);
        this.mDataList = chatList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_content, parent, false);

        // create ViewHolder
        return new ViewHolder(itemLayoutView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if (mDataList.get(position).getMediaPlay()) {
            holder.txt_audio_txt.setText("Stop Audio");
            holder.play.setImageResource(R.drawable.pause_song);
        } else {
            holder.txt_audio_txt.setText("Play Audio");
            holder.play.setImageResource(R.drawable.play_song);
        }

        if (TextUtils.isEmpty(mDataList.get(position).getUserAttachmentId())) {
            holder.userImage.setImageResource(R.drawable.logomulya);
        } else if (mDataList.get(position).getUserAttachmentId().equalsIgnoreCase("null")) {
            holder.userImage.setImageResource(R.drawable.logomulya);
        } else {
            Glide.with(mContext)
                    .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                            + "/services/data/v36.0/sobjects/Attachment/"
                            + mDataList.get(position).getUserAttachmentId() + "/Body"))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.logomulya))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.userImage);
        }

        if (mDataList.get(position).getIsAttachmentPresent() == null ||
                TextUtils.isEmpty(mDataList.get(position).getIsAttachmentPresent()) ||
                mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("false")) {

            if (TextUtils.isEmpty(mDataList.get(position).getAttachmentId())) {
                holder.mediaLayout.setVisibility(View.GONE);
                holder.layout_download.setVisibility(View.GONE);
            } else if (mDataList.get(position).getAttachmentId().equalsIgnoreCase("null")) {
                holder.mediaLayout.setVisibility(View.GONE);
                holder.layout_download.setVisibility(View.GONE);
            } else {
                holder.mediaLayout.setVisibility(View.VISIBLE);
                holder.layout_download.setVisibility(View.GONE);

                if (mDataList.get(position).getSynchStatus() != null
                        && mDataList.get(position).getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {

                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MV/Image" + "/" + mDataList.get(position).getAttachmentId() + ".png");

                    if (file.exists()) {
                        Glide.with(mContext)
                                .load(Uri.fromFile(file))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.picture);
                    }
                } else {
                    Glide.with(mContext)
                            .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                                    + "/services/data/v36.0/sobjects/Attachment/"
                                    + mDataList.get(position).getAttachmentId() + "/Body"))
                            .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.picture);
                }
            }
            holder.txt_detail.setVisibility(View.VISIBLE);
        } else {

            holder.mediaLayout.setVisibility(View.VISIBLE);
            holder.layout_download.setVisibility(View.GONE);

            if (mDataList.get(position).getContentType() != null
                    && mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {
                holder.picture.setVisibility(View.VISIBLE);
                holder.layout_Video.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.GONE);
                holder.txt_detail.setVisibility(View.VISIBLE);

                String imgUrl = Constants.IMAGEURL + mDataList.get(position).getId().trim() + ".png";
                Glide.with(mContext)
                        .load(imgUrl)
                        .dontTransform()
                        .dontAnimate()
                        .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.picture);

            } else if (mDataList.get(position).getContentType() != null
                    && mDataList.get(position).getContentType().equalsIgnoreCase("Video")) {

                holder.picture.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.GONE);
                holder.layout_Video.setVisibility(View.VISIBLE);
                holder.txt_detail.setVisibility(View.GONE);

            } else if (mDataList.get(position).getContentType() != null
                    && mDataList.get(position).getContentType().equalsIgnoreCase("Pdf")) {

                holder.picture.setVisibility(View.VISIBLE);
                holder.picture.setImageResource(R.drawable.pdfattachment);
                holder.audioLayout.setVisibility(View.GONE);
                holder.layout_Video.setVisibility(View.GONE);
                holder.txt_detail.setVisibility(View.GONE);

            } else if (mDataList.get(position).getContentType() != null
                    && mDataList.get(position).getContentType().equalsIgnoreCase("Audio")) {

                holder.picture.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.VISIBLE);
                holder.layout_Video.setVisibility(View.GONE);
                holder.txt_detail.setVisibility(View.GONE);

                holder.play.setOnClickListener(view -> {

                    if (temp == 555500) {
                        temp = position;

                        startAudio(Constants.IMAGEURL + mDataList.get(position).getId() + ".mp3");
                        holder.play.setImageResource(R.drawable.pause_song);
                        holder.txt_audio_txt.setText("Stop Audio");

                    } else if (temp == position) {

                        if (mPlayer.isPlaying()) {
                            mPlayer.pause();
                            holder.play.setImageResource(R.drawable.play_song);
                            holder.txt_audio_txt.setText("Play Audio");
                        } else {
                            holder.play.setImageResource(R.drawable.pause_song);
                            holder.txt_audio_txt.setText("Stop Audio");
                            mPlayer.start();
                        }
                    } else {
                        startAudio(Constants.IMAGEURL + mDataList.get(position).getId() + ".mp3");
                        mDataList.get(position).setMediaPlay(true);
                        mDataList.get(temp).setMediaPlay(false);
                        notifyItemChanged(position);
                        notifyItemChanged(temp);
                        temp = position;
                    }

                    mPlayer.setOnCompletionListener(mp -> {
                        mDataList.get(temp).setMediaPlay(false);
                        notifyItemChanged(temp);
                    });
                });
            } else if (mDataList.get(position).getId() != null) {
                holder.txt_detail.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .load(Constants.IMAGEURL + mDataList.get(position).getId() + ".png")
                        .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.picture);
            }
        }

        holder.txt_title.setText(String.format("%s", mDataList.get(position).getUserName()));
        holder.txt_template_type.setText(String.format("Title : %s", mDataList.get(position).getTitle()));
        holder.txt_desc.setText(String.format("Description : %s", mDataList.get(position).getDescription()));
        Linkify.addLinks(holder.txt_desc, Constants.urlPattern, mDataList.get(position).getDescription());
        holder.txt_time.setText(mDataList.get(position).getTime());
        holder.txtLikeCount.setText(String.format("%d Likes", mDataList.get(position).getLikeCount()));
        holder.txtCommentCount.setText(String.format("%d Comments", mDataList.get(position).getCommentCount()));
        holder.txt_tag.setText("Tag : ");
        holder.txt_type.setText(mDataList.get(position).getIssue_priority());

        if (mDataList.get(position).getStatus() != null && !(TextUtils.isEmpty(mDataList.get(position).getStatus()))) {
            holder.txt_status.setVisibility(View.VISIBLE);
            holder.txt_status.setText(String.format("Status : %s", mDataList.get(position).getStatus()));
        } else {
            holder.txt_status.setVisibility(View.GONE);
        }

        if (mDataList.get(position).getIsLike()) {
            holder.imgLike.setImageResource(R.drawable.like);
        } else {
            holder.imgLike.setImageResource(R.drawable.dislike);
        }

        if (mDataList.get(position).getCommentCount() == 0) {
            holder.img_comment.setImageResource(R.drawable.no_comment);
        } else {
            holder.img_comment.setImageResource(R.drawable.comment);
        }

        if (isFileAvalible(position) || (mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("false"))) {
            holder.layout_download_file.setVisibility(View.GONE);
            holder.layout_download.setVisibility(View.VISIBLE);
        } else {
            holder.layout_download_file.setVisibility(View.VISIBLE);
            holder.layout_download.setVisibility(View.GONE);
        }

        holder.imgMore.setVisibility(View.VISIBLE);
        holder.imgMore.setOnClickListener(view -> {
            //Inflating the Popup using xml file
            popup = new PopupMenu(mContext, holder.imgMore);
            popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());

            MenuItem spam = popup.getMenu().findItem(R.id.spam);
            MenuItem edit = popup.getMenu().findItem(R.id.edit);
            MenuItem delete = popup.getMenu().findItem(R.id.delete);
            MenuItem status = popup.getMenu().findItem(R.id.status);
            spam.setVisible(true);

            if (mActivity.hoSupportCommunity.equalsIgnoreCase("Ho Support")) {
                status.setVisible(true);
            }

            if (mDataList.get(position).getUser_id() != null &&
                    mDataList.get(position).getUser_id().equals(User.getCurrentUser(mContext).getMvUser().getId())) {
                delete.setVisible(true);
                edit.setVisible(true);
                spam.setVisible(false);
            } else {
                delete.setVisible(false);
                edit.setVisible(false);
                spam.setVisible(true);
            }

            if (mDataList.get(position).getPostUserDidSpam().equals(false)) {
                spam.setTitle("Mark As Spam");
            } else {
                spam.setTitle("Mark As Unspam");
            }

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equalsIgnoreCase("Delete")) {
                    postId = mDataList.get(position).getId();
                    deletePosition = position;
                    showDeletePopUp();
                } else if (item.getTitle().toString().equalsIgnoreCase("Change Status")) {
                    showStatusDialog(position);
                } else if (item.getTitle().toString().equalsIgnoreCase("Edit")) {
                    if (mActivity.hoSupportCommunity.equalsIgnoreCase("Ho Support")) {
                        Intent intent;
                        intent = new Intent(mContext, IssueTemplateActivity.class);
                        intent.putExtra("EDIT", true);
                        intent.putExtra(Constants.CONTENT, mDataList.get(position));
                        mContext.startActivity(intent);

                    } else {
                        Intent intent;
                        intent = new Intent(mContext, ReportingTemplateActivity.class);
                        intent.putExtra("EDIT", true);
                        intent.putExtra(Constants.CONTENT, mDataList.get(position));
                        mContext.startActivity(intent);
                    }
                } else if (mDataList.get(position).getPostUserDidSpam().equals(false)) {
                    Utills.spamContent(mContext, preferenceHelper, mDataList.get(position).getId(),
                            User.getCurrentUser(mContext).getMvUser().getId(), true);
                    mDataList.get(position).setPostUserDidSpam(!mDataList.get(position).getPostUserDidSpam());
                    notifyDataSetChanged();
                } else {
                    Utills.spamContent(mContext, preferenceHelper, mDataList.get(position).getId(),
                            User.getCurrentUser(mContext).getMvUser().getId(), false);
                    mDataList.get(position).setPostUserDidSpam(!mDataList.get(position).getPostUserDidSpam());
                    notifyDataSetChanged();
                }
                return true;
            });
            popup.show();
        });
    }

    private void showStatusDialog(final int Position) {

        final String[] items = {"Resolved", "Pending", "Reject"};
        int checkId = 1;
        int i = 0;

        for (String str : items) {
            if (mDataList.get(Position).getStatus() != null && !(TextUtils.isEmpty(mDataList.get(Position).getStatus()))) {
                if (str.equalsIgnoreCase(mDataList.get(Position).getStatus())) {
                    checkId = i;
                    break;
                }
            } else {
                break;
            }
            i++;
        }

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.select_status))
                .setSingleChoiceItems(items, checkId, (dialogInterface, i1) -> {

                })
                .setPositiveButton(R.string.ok, (dialog1, id) -> {

                    if (Utills.isConnected(mContext)) {
                        ListView lw = ((android.app.AlertDialog) dialog1).getListView();
                        String str = items[lw.getCheckedItemPosition()];
                        try {
                            mDataList.get(Position).setStatus(str);
                            mDataList.get(Position).setTemplate(BuildConfig.ISSUEID);
                            Utills.showProgressDialog(mContext);

                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            String json = gson.toJson(mDataList.get(Position));

                            JSONObject jsonObject1 = new JSONObject(json);
                            JSONArray jsonArrayAttachment = new JSONArray();
                            jsonObject1.put("attachments", jsonArrayAttachment);

                            JSONArray jsonArray = new JSONArray();
                            jsonArray.put(jsonObject1);

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("listVisitsData", jsonArray);

                            ServiceRequest apiService =
                                    ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
                            JsonParser jsonParser = new JsonParser();
                            JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                            apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                                    + Constants.InsertContentUrl, gsonObject).enqueue(new Callback<ResponseBody>() {

                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    Utills.hideProgressDialog();
                                    try {
                                        notifyItemChanged(Position);
                                        AppDatabase.getAppDatabase(mContext).userDao().updateContent(mDataList.get(Position));
                                    } catch (Exception e) {
                                        Utills.hideProgressDialog();
                                        e.printStackTrace();
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
                    dialog1.dismiss();

                }).create();

        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private GlideUrl getUrlWithHeaders(String url) {
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
    }

    public void showGroupDialog(final int position) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select Communities");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < mActivity.communityList.size(); i++) {
            arrayAdapter.add(mActivity.communityList.get(i).getName());
        }

        builderSingle.setNegativeButton(mContext.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {});
        builderSingle.show();
    }

    /*It shows communities dialog for forward posts*/
    private void showDialog(final int position) {
        final String[] items = new String[mActivity.communityList.size()];
        for (int i = 0; i < mActivity.communityList.size(); i++) {
            items[i] = mActivity.communityList.get(i).getName();
        }

        mSelection = new boolean[items.length];
        Arrays.fill(mSelection, false);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(mContext)
                .setTitle("Select Communities")
                .setMultiChoiceItems(items, mSelection, (dialog1, which, isChecked) -> {
                    if (mSelection != null && which < mSelection.length) {
                        mSelection[which] = isChecked;

                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(mContext.getString(R.string.ok), (dialog12, id) -> {
                    sendShareRecord(mDataList.get(position).getId());
                    Log.i("value", "value");
                })
                .setNegativeButton(mContext.getString(R.string.cancel), (dialog13, id) -> {
                    //  Your code when user clicked on Cancel
                })
                .create();
        dialog.show();
    }

    private String buildSelectedItemString(String[] items) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;
        jsonArrayAttachment = new JSONArray();
        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                jsonArrayAttachment.put(mActivity.communityList.get(i).getId());
                sb.append(i);
            }
        }
        return sb.toString();
    }

    /*It calls sharedRecords api. ContentId is id of particular posts. */
    private void sendShareRecord(String contentId) {
        if (Utills.isConnected(mContext)) {
            try {
                Utills.showProgressDialog(mContext, "Sharing Post...", "Please wait");

                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("userId", User.getCurrentUser(mContext).getMvUser().getId());
                jsonObject1.put("contentId", contentId);
                jsonObject1.put("grId", jsonArrayAttachment);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject1.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + Constants.SharedRecordsUrl, gsonObject).enqueue(new Callback<ResponseBody>() {

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
        public ImageView picture, userImage, imgLike, img_comment, play, imgMore;
        public CardView card_view;
        public RelativeLayout audioLayout, layout_Video;
        TextView txt_status, txt_id, txt_audio_txt, txt_title, txt_template_type, txt_desc, txt_time,
                textViewLike, txtLikeCount, txtCommentCount, txt_type, txt_tag, txt_detail;
        public LinearLayout layout_like, mediaLayout, layout_comment, layout_share, layout_download,
                layout_download_file;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txt_audio_txt = itemLayoutView.findViewById(R.id.audio_text);
            txt_title = itemLayoutView.findViewById(R.id.txt_title);
            txt_status = itemLayoutView.findViewById(R.id.txt_status);
            txt_template_type = itemLayoutView.findViewById(R.id.txt_template_type);
            txt_desc = itemLayoutView.findViewById(R.id.txt_desc);
            txt_time = itemLayoutView.findViewById(R.id.txt_time);
            txtLikeCount = itemLayoutView.findViewById(R.id.txtLikeCount);
            txtCommentCount = itemLayoutView.findViewById(R.id.txtCommentCount);
            userImage = itemLayoutView.findViewById(R.id.userImage);
            picture = itemLayoutView.findViewById(R.id.card_image);
            card_view = itemLayoutView.findViewById(R.id.card_view);
            imgLike = itemLayoutView.findViewById(R.id.imgLike);
            textViewLike = itemLayoutView.findViewById(R.id.textViewLike);
            img_comment = itemLayoutView.findViewById(R.id.img_comment);
            layout_comment = itemLayoutView.findViewById(R.id.layout_comment);
            mediaLayout = itemLayoutView.findViewById(R.id.mediaLayout);
            txt_type = itemLayoutView.findViewById(R.id.txt_type);
            audioLayout = itemLayoutView.findViewById(R.id.audioLayout);
            layout_download_file = itemLayoutView.findViewById(R.id.layout_download_file);
            play = itemLayoutView.findViewById(R.id.play);
            txt_tag = itemLayoutView.findViewById(R.id.txt_tag);
            txt_detail = itemLayoutView.findViewById(R.id.txt_detail);
            imgMore = itemLayoutView.findViewById(R.id.imgMore);
            txt_id = itemLayoutView.findViewById(R.id.txt_id);

            /*Add the comment to particular posts by calling api. */
            layout_comment.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                mContext.startActivity(intent);
            });

            txt_detail.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                intent.putExtra("flag", "forward_flag");
                intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                intent.putExtra(Constants.LIST, mActivity.json);
                mContext.startActivity(intent);
            });

            /*Forward posts to different communities*/
            layout_share = itemLayoutView.findViewById(R.id.layout_share);
            layout_share.setOnClickListener(view -> {
                if (!TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getSynchStatus()) &&
                        mDataList.get(getAdapterPosition()).getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {
                    Utills.showToast(mContext.getString(R.string.error_offline_share_post), mContext);
                } else {
                    if (Utills.isConnected(mContext)) {
                        showDialog(getAdapterPosition());
                    } else {
                        Utills.showToast(mContext.getString(R.string.error_no_internet), mContext);
                    }
                }
            });

            /*Play the videoin videoview activity. Pass the url of video to videoview activity*/
            layout_Video = itemLayoutView.findViewById(R.id.layout_Video);
            layout_Video.setOnClickListener(view -> {
                Intent myIntent = new Intent(mContext, VideoViewActivity.class);
                myIntent.putExtra("URL", Constants.IMAGEURL + mDataList.get(getAdapterPosition()).getId() + ".mp4");
                mContext.startActivity(myIntent);
            });

            /*Share the different types of media files */
            layout_download = itemLayoutView.findViewById(R.id.layout_download);
            layout_download.setOnClickListener(view -> {
                if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("true")) {
                    String filePath = "";
                    if (mDataList.get(getAdapterPosition()).getContentType() != null) {
                        if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("audio")) {
                            filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".mp3";
                        } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("video")) {
                            filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".mp4";
                        } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("pdf")) {
                            filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".pdf";
                        } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("zip")) {
                            filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".zip";
                        } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("Image")) {
                            filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".png";
                        }

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("application/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Intent.EXTRA_TEXT, "Title : "
                                + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : "
                                + mDataList.get(getAdapterPosition()).getDescription());
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                        mContext.startActivity(Intent.createChooser(intent, "Share Content"));
                    } else {
                        filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".png";

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("application/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Intent.EXTRA_TEXT, "Title : "
                                + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : "
                                + mDataList.get(getAdapterPosition()).getDescription());
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                        mContext.startActivity(Intent.createChooser(intent, "Share Content"));
                    }
                } else if (mDataList.get(getAdapterPosition()).getAttachmentId() != null) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MV/Download/" + mDataList.get(getAdapterPosition()).getAttachmentId() + ".png";

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("application/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Intent.EXTRA_TEXT, "Title : "
                            + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : "
                            + mDataList.get(getAdapterPosition()).getDescription());
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                    mContext.startActivity(Intent.createChooser(intent, "Share Content"));
                } else {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("image*//**//*");
                    i.putExtra(Intent.EXTRA_TEXT, "Title : "
                            + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : "
                            + mDataList.get(getAdapterPosition()).getDescription());
                    Utills.hideProgressDialog();
                    mContext.startActivity(Intent.createChooser(i, "Share Post"));
                }
            });

            /*It Start the downloading file*/
            layout_download_file.setOnClickListener(v -> {
                if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("true")) {
                    startDownload(getAdapterPosition());
                } else {
                    if (mDataList.get(getAdapterPosition()).getAttachmentId() != null) {
                        downloadImage(getAdapterPosition());
                    }
                }
            });

            /*SendLike and SendDislike function is called here.*/
            layout_like = itemLayoutView.findViewById(R.id.layout_like);
            layout_like.setOnClickListener(view -> {
                if (!TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getSynchStatus()) &&
                        mDataList.get(getAdapterPosition()).getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {
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
            });

            picture.setOnClickListener(v -> {
                if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("false")) {
                    if (mDataList.get(getAdapterPosition()).getAttachmentId() != null) {
                        Utills.showImagewithheaderZoomDialog(v.getContext(),
                                getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                                        + "/services/data/v36.0/sobjects/Attachment/"
                                        + mDataList.get(getAdapterPosition()).getAttachmentId() + "/Body"));
                    }
                } else if (mDataList.get(getAdapterPosition()).getId() != null &&
                        mDataList.get(getAdapterPosition()).getContentType() != null) {

                    if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("image")) {
                        Utills.showImageZoomInDialog(v.getContext(), mDataList.get(getAdapterPosition()).getId());
                    } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("pdf")) {
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".pdf";
                        if (!(new File(filePath).exists())) {
                            Utills.showToast("Unable to open PDF file. Please download it.", mContext);
                            return;
                        }

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        PackageManager packageManager = mContext.getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            mContext.startActivity(intent);
                        } else {
                            Utills.showToast("No Application available to open PDF file", mContext);
                        }
                    }
                }
            });
        }
    }

    private void downloadImage(final int adapterPosition) {
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

                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/MV/Download/" + mDataList.get(adapterPosition).getAttachmentId() + ".png");
                        FileOutputStream out = new FileOutputStream(file);
                        decodedByte.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.close();
                        notifyDataSetChanged();

                        Utills.hideProgressDialog();
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
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/MV/Download/" + mDataList.get(mPosition).getId() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    /*It calls the removeLike api for dislike the particular post. Here content Id is Id of posts.
    It is called on layout_like click.*/
    private void sendDisLikeAPI(String cotentId, boolean isLike) {
        if (Utills.isConnected(mContext)) {
            try {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("Is_Like", isLike);
                jsonObject1.put("MV_Content", cotentId);
                jsonObject1.put("MV_User", User.getCurrentUser(mContext).getMvUser().getId());

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("listVisitsData", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + Constants.RemoveLikeUrl, gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
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

    /*It calls the InsertLike api for like the particular post.Here content Id is Id of posts. It is called on layout_like click.*/
    private void sendLikeAPI(String cotentId, Boolean isLike) {
        if (Utills.isConnected(mContext)) {
            try {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("Is_Like__c", isLike);
                jsonObject1.put("MV_Content__c", cotentId);
                jsonObject1.put("MV_User__c", User.getCurrentUser(mContext).getMvUser().getId());

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("contentlikeList", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());

                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + Constants.InsertLikeUrl, gsonObject).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
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

    /*It is used for starting mediaplayer by passing audio url.*/
    private void startAudio(String url) {
        if (mPlayer == null)
            mPlayer = new MediaPlayer();

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }

        mPlayer.reset();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mPlayer.setDataSource(url);
        } catch (IllegalArgumentException e) {
            Toast.makeText(mContext, "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            Toast.makeText(mContext, "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e) {
            Toast.makeText(mContext, "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mPlayer.prepare();
        } catch (IllegalStateException e) {
            Toast.makeText(mContext, "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(mContext, "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        }

        mPlayer.start();
    }

    /*Send urls, filetypes and filenames to Downloadservice for downloading file */
    private void startDownload(int position) {
        Utills.showToast("Downloading Started...", mContext);
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra("fragment_flag", "My_Community");

        if ((mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("true"))) {
            if ((mDataList.get(position).getContentType() != null)) {
                if (mDataList.get(position).getContentType().equalsIgnoreCase("zip")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".zip");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "zip");
                    intent.putExtra("URL", Constants.IMAGEURL + mDataList.get(position).getId() + ".zip");
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("pdf")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".pdf");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "pdf");
                    intent.putExtra("URL", Constants.IMAGEURL + mDataList.get(position).getId() + ".pdf");
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("audio")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".mp3");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "audio");
                    intent.putExtra("URL", Constants.IMAGEURL + mDataList.get(position).getId() + ".mp3");
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("video")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".mp4");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "video");
                    intent.putExtra("URL", Constants.IMAGEURL + mDataList.get(position).getId() + ".mp4");
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".png");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "Image");
                    intent.putExtra("URL", Constants.IMAGEURL + mDataList.get(position).getId() + ".png");
                }
            } else {
                intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".png");
                intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "Image");
                intent.putExtra("URL", Constants.IMAGEURL + mDataList.get(position).getId() + ".png");
            }
        }
        mContext.startService(intent);
    }

    /*Check if file is available or not in respective folder.*/
    private boolean isFileAvalible(int position) {
        if (mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("true")) {
            if (mDataList.get(position).getContentType() != null) {
                if (mDataList.get(position).getContentType().equalsIgnoreCase("zip")) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MV/UnZip/" + mDataList.get(position).getTitle();
                    return new File(filePath).exists();
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("pdf")) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MV/Zip/" + mDataList.get(position).getTitle() + ".pdf";
                    return new File(filePath).exists();
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("video")) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MV/Zip/" + mDataList.get(position).getTitle() + ".mp4";
                    return new File(filePath).exists();
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("audio")) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MV/Zip/" + mDataList.get(position).getTitle() + ".mp3";
                    return new File(filePath).exists();
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MV/Zip/" + mDataList.get(position).getTitle() + ".png";
                    return new File(filePath).exists();
                }
            } else {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/MV/Zip/" + mDataList.get(position).getTitle() + ".png";
                return new File(filePath).exists();
            }
        } else if (mDataList.get(position).getAttachmentId() != null) {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/MV/Download/" + mDataList.get(position).getAttachmentId() + ".png";
            return new File(filePath).exists();
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private void showDeletePopUp() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.text_are_you_sure));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.text_delete));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_launcher);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(android.R.string.cancel), (dialog, which) -> alertDialog.dismiss());

        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), (dialog, which) -> DeletePost());

        // Showing Alert Message
        alertDialog.show();
    }

    private void DeletePost() {
        Utills.showProgressDialog(mContext);

        ServiceRequest apiService =
                ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);

        apiService.getSalesForceData(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.DeletePostUrl + postId).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mDataList.get(deletePosition).setDelete(true);
                    AppDatabase.getAppDatabase(mContext).userDao().updateContent(mDataList.get(deletePosition));

                    Utills.showToast("Post Deleted Successfully...", mContext);
                    mDataList.remove(deletePosition);
                    notifyItemRemoved(deletePosition);
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
    }

    public static void spamContent(Context mContext, PreferenceHelper preferenceHelper, String ID,
                                   String UserId, Boolean isSpam) {
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.SpamContentUrl + "?Id=" + ID + "&userId=" + UserId + "&isSpam=" + isSpam;

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String data;
                try {
                    data = response.body().string();
                    if (data.length() > 0) {
                        JSONObject jsonObject = new JSONObject(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }
}