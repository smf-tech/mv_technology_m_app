package com.mv.Adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Activity.CommentActivity;
import com.mv.Activity.CommunityDetailsActivity;
import com.mv.Activity.CommunityHomeActivity;
import com.mv.Activity.VideoViewActivity;
import com.mv.Model.Content;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.DownloadService;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

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
    int temp = 555500;
    MediaPlayer mPlayer = new MediaPlayer();
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
                    .load(getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mDataList.get(position).getUserAttachmentId() + "/Body"))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.logomulya))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.userImage);
            // holder.picture.setImageDrawable(mPlacePictures[position % mPlacePictures.length]);
        }
        if (mDataList.get(position).getIsAttachmentPresent() == null || TextUtils.isEmpty(mDataList.get(position).getIsAttachmentPresent()) || mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("false")) {
            if (TextUtils.isEmpty(mDataList.get(position).getAttachmentId())) {
                holder.mediaLayout.setVisibility(View.GONE);
                holder.layout_download.setVisibility(View.GONE);
            } else if (mDataList.get(position).getAttachmentId().equalsIgnoreCase("null")) {
                holder.mediaLayout.setVisibility(View.GONE);
                holder.layout_download.setVisibility(View.GONE);
            } else {
                holder.mediaLayout.setVisibility(View.VISIBLE);
                holder.layout_download.setVisibility(View.GONE);
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
            holder.layout_download.setVisibility(View.GONE);


            if (mDataList.get(position).getContentType() != null
                    && mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {
                holder.picture.setVisibility(View.VISIBLE);
                holder.layout_Video.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.GONE);


                Glide.with(mContext)
                        .load("http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".png")
                        .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.picture);
            } else if (mDataList.get(position).getContentType() != null
                    && mDataList.get(position).getContentType().equalsIgnoreCase("Video")) {
                holder.picture.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.GONE);
                holder.layout_Video.setVisibility(View.VISIBLE);

              /*  holder.card_video.setVideoPath("http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".mp4");
                holder.card_video.start();*/

            } else if (mDataList.get(position).getContentType() != null
                    && mDataList.get(position).getContentType().equalsIgnoreCase("Audio")) {
                holder.picture.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.VISIBLE);
                holder.layout_Video.setVisibility(View.GONE);

                holder.play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("aaaatemp", temp + "");
                        Log.i("aaaaposition", position + "");
                        if (temp == 555500) {
                            temp = position;

                            startAudio("http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".mp3");
                            holder.play.setImageResource(R.drawable.pause_song);
                            holder.txt_audio_txt.setText("Stop Audio");
                            // notifyItemChanged(position);
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
                            //  notifyItemChanged(position);
                        } else {

                            startAudio("http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".mp3");
                            mDataList.get(position).setMediaPlay(true);
                            mDataList.get(temp).setMediaPlay(false);
                            notifyItemChanged(position);
                            notifyItemChanged(temp);
                            temp = position;
                        }
                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mDataList.get(temp).setMediaPlay(false);
                                notifyItemChanged(temp);
                            }
                        });


                    }
                });
            } else  if (mDataList.get(position).getId()!=null)
            {
                Glide.with(mContext)
                        .load("http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".png")
                        .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.picture);
            }
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
        if (mDataList.get(position).getIsLike() && (mDataList.get(position).getUser_id().equalsIgnoreCase(User.getCurrentUser(mContext).getId())))
            holder.imgLike.setImageResource(R.drawable.like);
        else
            holder.imgLike.setImageResource(R.drawable.dislike);

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

    /*It shows communities dialog for forward posts*/
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
    /*It calls sharedRecords api. ContentId is id of particular posts. */
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
        public ImageView picture, userImage, imgLike, img_comment,play;
        public CardView card_view;
        public RelativeLayout audioLayout, layout_Video;
        public TextView txt_audio_txt,txt_title, txt_template_type, txt_desc,txt_detail, txt_time, textViewLike, txtLikeCount, txtCommentCount, txt_type;
        public LinearLayout layout_like, mediaLayout, layout_comment, layout_share, layout_download,layout_download_file;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txt_audio_txt = (TextView) itemLayoutView.findViewById(R.id.audio_text);
            txt_title = (TextView) itemLayoutView.findViewById(R.id.txt_title);
            txt_template_type = (TextView) itemLayoutView.findViewById(R.id.txt_template_type);
            txt_desc = (TextView) itemLayoutView.findViewById(R.id.txt_desc);
            txt_detail = (TextView) itemLayoutView.findViewById(R.id.txt_detail);
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
            audioLayout = (RelativeLayout) itemLayoutView.findViewById(R.id.audioLayout);
            layout_download_file = (LinearLayout) itemLayoutView.findViewById(R.id.layout_download_file);
            play = (ImageView) itemLayoutView.findViewById(R.id.play);


            /*Add the comment to particular posts by calling api. */
            layout_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, CommentActivity.class);
                    intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                    mContext.startActivity(intent);
                }
            });


            txt_detail.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   if (TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getIsAttachmentPresent())) {
                       Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                       intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                       intent.putExtra("flag", "forward_flag");
                       intent.putExtra(Constants.LIST, mActivity.json);
                       mContext.startActivity(intent);
                   } else if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("false")) {
                       Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                       intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                       intent.putExtra("flag", "forward_flag");
                       intent.putExtra(Constants.LIST, mActivity.json);
                       mContext.startActivity(intent);
                   } else if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("true")){
                       if (mDataList.get(getAdapterPosition()).getContentType()!=null){
                           if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("Image")) {
                               Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                               intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                               intent.putExtra("flag", "forward_flag");
                               intent.putExtra(Constants.LIST, mActivity.json);
                               mContext.startActivity(intent);
                           }
                       }else {
                           Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                           intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                           intent.putExtra("flag", "forward_flag");
                           intent.putExtra(Constants.LIST, mActivity.json);
                           mContext.startActivity(intent);
                       }
                   }
               }
           });

            layout_share = (LinearLayout) itemLayoutView.findViewById(R.id.layout_share);
            /*Forward posts to different communities*/
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

            layout_Video = (RelativeLayout) itemLayoutView.findViewById(R.id.layout_Video);
            /*Play the videoin videoview activity. Pass the url of video to videoview activity*/
            layout_Video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(mContext,
                            VideoViewActivity.class);
                    myIntent.putExtra("URL", "http://mobileapp.mulyavardhan.org/images/" + mDataList.get(getAdapterPosition()).getId() + ".mp4");
                    mContext.startActivity(myIntent);
                }
            });


            layout_download = (LinearLayout) itemLayoutView.findViewById(R.id.layout_download);
            /*Share the different types of media files */
            layout_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("true")) {
                        String filePath = "";
                        if (mDataList.get(getAdapterPosition()).getContentType()!=null) {

                            if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("audio")) {
                                filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".mp3";

                            } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("video")) {
                                filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".mp4";

                            } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("pdf")) {
                                filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".pdf";

                            } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("zip")) {
                                filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".zip";
                            } else if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("Image")) {
                                filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".png";
                            }

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.setType("application/*");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            intent.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());

                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                            mContext.startActivity(Intent.createChooser(intent, "Share Content"));
                        }else {
                            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getTitle() + ".png";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.setType("application/*");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            intent.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());

                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                            mContext.startActivity(Intent.createChooser(intent, "Share Content"));
                        }
                    }else if (mDataList.get(getAdapterPosition()).getAttachmentId() != null) {
                        // if (mDataList.get(position).getFileType().equalsIgnoreCase("zip")) {
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mDataList.get(getAdapterPosition()).getAttachmentId()+".png";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("application/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        intent.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());

                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                        mContext.startActivity(Intent.createChooser(intent, "Share Content"));
                    } else {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image*//**//*");
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());
                        Utills.hideProgressDialog();
                        mContext.startActivity(Intent.createChooser(i, "Share Post"));
                    }




                }
            });


           /*It Start the downloading file*/
            layout_download_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("true")){
                        startDownload(getAdapterPosition());
                    }else {
                        if (mDataList.get(getAdapterPosition()).getAttachmentId()!=null){
                            downloadImage(getAdapterPosition());
                        }

                    }


/*
                    if (mDataList.get(getAdapterPosition()).getAttachmentId()!=null) {
                        downloadImage(getAdapterPosition());
                    }else {

                        startDownload(getAdapterPosition());
                    }*/



                }

            });
            layout_like = (LinearLayout) itemLayoutView.findViewById(R.id.layout_like);

            /*SendLike and SendDislike function is called here.*/
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
           /* card_view.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick(View view) {

                    if (TextUtils.isEmpty(mDataList.get(getAdapterPosition()).getIsAttachmentPresent())) {
                        Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                        intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                        intent.putExtra("flag", "forward_flag");
                        intent.putExtra(Constants.LIST, mActivity.json);
                        mContext.startActivity(intent);
                    } else if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("false")) {
                        Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                        intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                        intent.putExtra("flag", "forward_flag");
                        intent.putExtra(Constants.LIST, mActivity.json);
                        mContext.startActivity(intent);
                    } else if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("true")){
                        if (mDataList.get(getAdapterPosition()).getContentType()!=null){
                            if (mDataList.get(getAdapterPosition()).getContentType().equalsIgnoreCase("Image")) {
                                Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                                intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                                intent.putExtra("flag", "forward_flag");
                                intent.putExtra(Constants.LIST, mActivity.json);
                                mContext.startActivity(intent);
                            }
                        }else {
                            Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                            intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                            intent.putExtra("flag", "forward_flag");
                            intent.putExtra(Constants.LIST, mActivity.json);
                            mContext.startActivity(intent);
                        }
                    }
                }
            });*/

            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("false")){
                    if (mDataList.get(getAdapterPosition()).getAttachmentId()!=null) {
                        Utills.showImagewithheaderZoomDialog(v.getContext(), getUrlWithHeaders(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/data/v36.0/sobjects/Attachment/" + mDataList.get(getAdapterPosition()).getAttachmentId() + "/Body"));
                    }
                    }else if ( mDataList.get(getAdapterPosition()).getId()!=null){
                        Utills.showImageZoomInDialog(v.getContext(),mDataList.get(getAdapterPosition()).getId());

                    }

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

                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" +mDataList.get(adapterPosition).getAttachmentId()+".png");
                        FileOutputStream out = new FileOutputStream(file);
                        decodedByte.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.close();
                        notifyDataSetChanged();
                      /*  Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image*//**//**//**//*");
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(adapterPosition).getTitle() + "\n\nDescription : " + mDataList.get(adapterPosition).getDescription());
                        i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(decodedByte, adapterPosition));*/
                        Utills.hideProgressDialog();
                        /*mContext.startActivity(Intent.createChooser(i, "Share Post"));*/
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
    /*It calls the removeLike api for dislike the particular post. Here content Id is Id of posts. It is called on layout_like click.*/

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
    /*It calls the InsertLike api for like the particular post.Here content Id is Id of posts. It is called on layout_like click.*/
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
   /*It is used for starting mediaplayer by passing audio url.*/
    public void startAudio(String url) {
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

    public void startDownload(int position) {
        Utills.showToast("Downloading Started...", mContext);
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra("fragment_flag", "My_Community");
        if((mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("true")) ) {
            if ( (mDataList.get(position).getContentType()!=null)) {
                if (mDataList.get(position).getContentType().equalsIgnoreCase("zip")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".zip");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "zip");
                    intent.putExtra("URL", "http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".zip");
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("pdf")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".pdf");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "pdf");
                    intent.putExtra("URL", "http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".pdf");
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("audio")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".mp3");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "audio");
                    intent.putExtra("URL", "http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".mp3");
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("video")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".mp4");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "video");
                    intent.putExtra("URL", "http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".mp4");
                } else if (mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {
                    intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".png");
                    intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "Image");
                    intent.putExtra("URL", "http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".png");
                }
            }else {
                intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".png");
                intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "Image");
                intent.putExtra("URL", "http://mobileapp.mulyavardhan.org/images/" + mDataList.get(position).getId() + ".png");
            }
        }
        mContext.startService(intent);
    }

    /*Check if file is available or not in respective folder.*/
    private boolean isFileAvalible(int position) {
        if (mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("true")){
        if (mDataList.get(position).getContentType() != null) {
            if (mDataList.get(position).getContentType().equalsIgnoreCase("zip")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/UnZip/" + mDataList.get(position).getTitle();
                if (new File(filePath).exists())
                    return true;
                return false;
            } else if (mDataList.get(position).getContentType().equalsIgnoreCase("pdf")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(position).getTitle() + ".pdf";
                if (new File(filePath).exists())
                    return true;
                return false;
            } else if (mDataList.get(position).getContentType().equalsIgnoreCase("video")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(position).getTitle() + ".mp4";
                if (new File(filePath).exists())
                    return true;
                return false;
            } else if (mDataList.get(position).getContentType().equalsIgnoreCase("audio")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(position).getTitle() + ".mp3";
                if (new File(filePath).exists())
                    return true;
                return false;
            } else if (mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(position).getTitle() + ".png";
                //   Log.e("Image path-->" +m)
                if (new File(filePath).exists())
                    return true;
                return false;
            }
        }else {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(position).getTitle() + ".png";
            //   Log.e("Image path-->" +m)
            if (new File(filePath).exists())
                return true;
        }

        }  else if (mDataList.get(position).getAttachmentId() != null) {
            // if (mDataList.get(position).getFileType().equalsIgnoreCase("zip")) {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Download/" + mDataList.get(position).getAttachmentId()+".png";
            if (new File(filePath).exists())
                return true;
            return false;

        }
        return false;
    }

}

