package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Activity.AddThetSavadActivity;
import com.mv.Activity.CommentActivity;
import com.mv.Activity.CommunityDetailsActivity;
import com.mv.Activity.VideoViewActivity;
import com.mv.ActivityMenu.ThetSavandFragment;
import com.mv.Model.Content;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.DownloadService;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Nanostuffs on 27-12-2017.
 */
public class ThetSavandAdapter extends RecyclerView.Adapter<ThetSavandAdapter.ViewHolder> {

    private final Context mContext;

    private List<Content> mDataList;
    private PreferenceHelper preferenceHelper;
    private JSONArray jsonArrayAttchment = new JSONArray();
    private MediaPlayer mPlayer = new MediaPlayer();

    private String postId;
    private int mPosition;
    private int temp = 555500, deletePosition;

    public ThetSavandAdapter(Context context, ThetSavandFragment fragment, List<Content> chatList) {
        TypedArray a = context.getResources().obtainTypedArray(R.array.places_picture);
        mContext = context;

        Drawable[] mPlacePictures = new Drawable[a.length()];
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
        return new ViewHolder(itemLayoutView);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (mDataList.get(position).getMediaPlay()) {
            holder.txt_audio_txt.setText("Stop Audio");
            holder.play.setImageResource(R.drawable.pause_song);
        } else {
            holder.txt_audio_txt.setText("Play Audio");
            holder.play.setImageResource(R.drawable.play_song);
        }

        if (TextUtils.isEmpty(mDataList.get(position).getUserAttachmentId())) {
            holder.userImage.setImageResource(R.drawable.logomulya);
        } else if (mDataList.get(position).getAttachmentId().equalsIgnoreCase("null")) {
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

                if (mDataList.get(position).getContentType() != null &&
                        mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {
                    holder.picture.setVisibility(View.VISIBLE);
                    holder.layout_Video.setVisibility(View.GONE);

                    if (mDataList.get(position).getSynchStatus() != null &&
                            mDataList.get(position).getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL)) {
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
            }
        } else {
            holder.mediaLayout.setVisibility(View.VISIBLE);
            holder.layout_download.setVisibility(View.GONE);

            if (mDataList.get(position).getContentType() != null &&
                    mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {

                holder.picture.setVisibility(View.VISIBLE);
                holder.layout_Video.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.GONE);

                Glide.with(mContext)
                        .load(Constants.IMAGEURL + mDataList.get(position).getId() + ".png")
                        .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.picture);

            } else if (mDataList.get(position).getContentType() != null &&
                    mDataList.get(position).getContentType().equalsIgnoreCase("Video")) {
                holder.picture.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.GONE);
                holder.layout_Video.setVisibility(View.VISIBLE);

            } else if (mDataList.get(position).getContentType() != null &&
                    mDataList.get(position).getContentType().equalsIgnoreCase("Audio")) {
                holder.picture.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.VISIBLE);
                holder.layout_Video.setVisibility(View.GONE);

                holder.play.setOnClickListener(view -> {
                    Log.i("aaaatemp", temp + "");
                    Log.i("aaaaposition", position + "");
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
            }
        }

        holder.layout_share.setVisibility(View.GONE);
        holder.txt_title.setText(String.format("%s", mDataList.get(position).getUserName()));
        holder.txt_template_type.setText(String.format("Title : %s", mDataList.get(position).getTitle()));
        holder.txt_desc.setText(String.format("Description : %s", mDataList.get(position).getDescription()));

        holder.txt_time.setText(mDataList.get(position).getTime());
        holder.txtLikeCount.setText(mDataList.get(position).getLikeCount() + " Likes");
        holder.txtCommentCount.setText(mDataList.get(position).getCommentCount() + " Comments");

        holder.txt_type.setText(mDataList.get(position).getIssue_priority());
        if ((mDataList.get(position).getIsLike())) {
            holder.imgLike.setImageResource(R.drawable.like);
        } else {
            holder.imgLike.setImageResource(R.drawable.dislike);
        }

        if (mDataList.get(position).getCommentCount() == 0) {
            holder.img_comment.setImageResource(R.drawable.no_comment);
        } else {
            holder.img_comment.setImageResource(R.drawable.comment);
        }

        Log.i("Value", "Position " + position + " : " + isFileAvalible(position));
        if (isFileAvalible(position) || mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("false")) {
            holder.layout_download_file.setVisibility(View.GONE);
            holder.layout_download.setVisibility(View.VISIBLE);
        } else {
            holder.layout_download_file.setVisibility(View.VISIBLE);
            holder.layout_download.setVisibility(View.GONE);
        }

        holder.imgMore.setVisibility(View.VISIBLE);
        holder.imgMore.setOnClickListener(view -> {
            //Inflating the Popup using xml file
            PopupMenu popup = new PopupMenu(mContext, holder.imgMore);
            popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());

            MenuItem edit = popup.getMenu().findItem(R.id.edit);
            MenuItem delete = popup.getMenu().findItem(R.id.delete);
            MenuItem spam = popup.getMenu().findItem(R.id.spam);
            spam.setVisible(true);

            if (mDataList.get(position).getUser_id().equals(User.getCurrentUser(mContext).getMvUser().getId())) {
                spam.setVisible(false);
                edit.setVisible(true);
                delete.setVisible(true);
            } else {
                edit.setVisible(false);
                spam.setVisible(true);
                delete.setVisible(false);
            }

            if (mDataList.get(position).getPostUserDidSpam().equals(false)) {
                spam.setTitle("Mark As Spam");
            } else {
                spam.setTitle("Mark As Unspam");
            }

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equalsIgnoreCase("Edit")) {
                    Intent intent;
                    intent = new Intent(mContext, AddThetSavadActivity.class);
                    intent.putExtra("EDIT", true);
                    intent.putExtra(Constants.CONTENT, mDataList.get(position));
                    mContext.startActivity(intent);
                } else if (item.getTitle().toString().equalsIgnoreCase("Delete")) {
                    postId = mDataList.get(position).getId();
                    deletePosition = position;
                    showDeletePopUp();
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

            //showing popup menu
            popup.show();
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**/
    private GlideUrl getUrlWithHeaders(String url) {
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "OAuth " + preferenceHelper.getString(PreferenceHelper.AccessToken))
                .addHeader("Content-Type", "image/png")
                .build());
    }

    private void sendShareRecord(String contentId) {
        if (Utills.isConnected(mContext)) {
            try {
                Utills.showProgressDialog(mContext, "Sharing Post...", "Please wait");
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("userId", User.getCurrentUser(mContext).getMvUser().getId());
                jsonObject1.put("contentId", contentId);
                jsonObject1.put("grId", jsonArrayAttchment);

                ServiceRequest apiService = ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
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
        ImageView picture, userImage, imgLike, img_comment, imageThumbnail;
        public CardView card_view;
        TextView txt_audio_txt, txt_title, txt_template_type, txt_desc, txt_time, textViewLike, txtLikeCount, txtCommentCount, txt_type, txt_detail;
        public LinearLayout mediaLayout, layout_like, layout_comment, layout_share, layout_download, layout_download_file;
        public RelativeLayout audioLayout, layout_Video;
        public ImageView play, imgMore;
        LinearLayout lnr_content;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txt_title = itemLayoutView.findViewById(R.id.txt_title);
            txt_template_type = itemLayoutView.findViewById(R.id.txt_template_type);
            txt_audio_txt = itemLayoutView.findViewById(R.id.audio_text);
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
            imageThumbnail = itemLayoutView.findViewById(R.id.card_Thumbnail);
            txt_type = itemLayoutView.findViewById(R.id.txt_type);
            layout_download_file = itemLayoutView.findViewById(R.id.layout_download_file);
            lnr_content = itemLayoutView.findViewById(R.id.lnr_content);
            txt_detail = itemLayoutView.findViewById(R.id.txt_detail);

            txt_detail.setOnClickListener(view -> {
                if (TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getIsAttachmentPresent())) {
                    Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                    intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                    intent.putExtra("flag", "not_forward_flag");
                    mContext.startActivity(intent);
                } else if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("false")) {
                    Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                    intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                    intent.putExtra("flag", "not_forward_flag");
                    mContext.startActivity(intent);
                } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("Image")) {
                    Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                    intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                    intent.putExtra("flag", "not_forward_flag");
                    mContext.startActivity(intent);
                }
            });

            layout_download_file.setOnClickListener(v -> {
                startDownload(getAdapterPosition());
            });

            imgMore = itemLayoutView.findViewById(R.id.imgMore);

            layout_comment.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                mContext.startActivity(intent);
            });

            audioLayout = itemLayoutView.findViewById(R.id.audioLayout);
            mediaLayout = itemLayoutView.findViewById(R.id.mediaLayout);
            play = itemLayoutView.findViewById(R.id.play);

            layout_share = itemLayoutView.findViewById(R.id.layout_share);
            layout_share.setOnClickListener(view -> startDownload(getAdapterPosition()));

            layout_Video = itemLayoutView.findViewById(R.id.layout_Video);
            layout_Video.setOnClickListener(view -> {
                Intent myIntent = new Intent(mContext, VideoViewActivity.class);
                myIntent.putExtra("URL", Constants.IMAGEURL
                        + mDataList.get(getAdapterPosition()).getId() + ".mp4");
                mContext.startActivity(myIntent);
            });

            layout_download = itemLayoutView.findViewById(R.id.layout_download);
            layout_download.setOnClickListener(view -> {
                if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("true")) {
                    String filePath = "";
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
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("image*//**//*");
                    i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle()
                            + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());
                    Utills.hideProgressDialog();
                    mContext.startActivity(Intent.createChooser(i, "Share Post"));
                }
            });

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

            picture.setOnClickListener(v -> Utills.showImageZoomInDialog(v.getContext(),
                    mDataList.get(getAdapterPosition()).getId()));
        }
    }

    @SuppressWarnings("deprecation")
    private void showDeletePopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.text_are_you_sure));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.text_delete));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), (dialog, which) -> DeletePost());

        // Showing Alert Message
        alertDialog.show();
    }

    private void DeletePost() {
        Utills.showProgressDialog(mContext);

        ServiceRequest apiService = ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);

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

    private void downloadImage(final int adapterPosition) {
        if (Utills.isConnected(mContext)) {
            Utills.showProgressDialog(mContext, "Please wait", "Loading Image");

            ServiceRequest apiService = ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
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
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(adapterPosition).getTitle()
                                + "\n\nDescription : " + mDataList.get(adapterPosition).getDescription());
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

    private Uri getLocalBitmapUri(Bitmap bmp, int mPosition) {
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

    private void sendDisLikeAPI(String cotentId, boolean isLike) {
        if (Utills.isConnected(mContext)) {
            try {
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("Is_Like", isLike);
                jsonObject1.put("MV_Content", cotentId);
                jsonObject1.put("MV_User", User.getCurrentUser(mContext).getMvUser().getId());

                jsonArray.put(jsonObject1);
                jsonObject.put("listVisitsData", jsonArray);

                ServiceRequest apiService = ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
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

    private void sendLikeAPI(String cotentId, Boolean isLike) {
        if (Utills.isConnected(mContext)) {
            try {
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("Is_Like__c", isLike);
                jsonObject1.put("MV_Content__c", cotentId);
                jsonObject1.put("MV_User__c", User.getCurrentUser(mContext).getMvUser().getId());

                jsonArray.put(jsonObject1);
                jsonObject.put("contentlikeList", jsonArray);

                ServiceRequest apiService = ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
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

    private void startAudio(String url) {
        if (mPlayer == null) {
           mPlayer = new MediaPlayer();
        }

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

    private void startDownload(int position) {
        Utills.showToast("Downloading Started...", mContext);
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra("fragment_flag", "ThetSanvad_Fragment");

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

        mContext.startService(intent);
    }

    private boolean isFileAvalible(int position) {
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
        }
        return false;
    }
}

