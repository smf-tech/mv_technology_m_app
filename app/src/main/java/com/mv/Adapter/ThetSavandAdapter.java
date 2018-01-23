package com.mv.Adapter;

import android.app.AlertDialog;
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
import android.os.Environment;
import android.os.Handler;
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
import android.widget.ProgressBar;
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
import com.mv.Fragment.ThetSavandFragment;
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
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Nanostuffs on 27-12-2017.
 */

public class ThetSavandAdapter extends RecyclerView.Adapter<ThetSavandAdapter.ViewHolder> {
    private static final int LENGTH = 7;
    private final String[] mPlaces;
    private final String[] mPlaceDesc;
    private final Drawable[] mPlacePictures;
    private final Context mContext;
    private List<Content> mDataList;
    private PreferenceHelper preferenceHelper;
    private int mPosition;
    private boolean[] mSelection = null;
    private String value;
    private Handler mHandler = new Handler();
    private JSONArray jsonArrayAttchment = new JSONArray();
    private Bitmap theBitmap;
    private String postId;
    private ThetSavandFragment fragment;
    int temp = 555500, deletePosition;
    MediaPlayer mPlayer = new MediaPlayer();
    private static final Pattern urlPattern = Pattern.compile("(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public ThetSavandAdapter(Context context, ThetSavandFragment fragment, List<Content> chatList) {
        Resources resources = context.getResources();
        mPlaces = resources.getStringArray(R.array.places);
        this.fragment = fragment;
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
    public ThetSavandAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_content, parent, false);

        // create ViewHolder
        ThetSavandAdapter.ViewHolder viewHolder = new ThetSavandAdapter.ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ThetSavandAdapter.ViewHolder holder, final int position) {

       /* Glide.with(mContext)
                .load(getUrlWithHeaders(new PreferenceHelper(mContext).getString(PreferenceHelper.InstanceUrl)+"services/data/v20.0/sobjects/Attachment/"+mDataList.get(position).getId()))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.picture);
*/
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
                if (mDataList.get(position).getContentType() != null
                        && mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {
                    holder.picture.setVisibility(View.VISIBLE);
                    holder.layout_Video.setVisibility(View.GONE);
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
                        .load("http://13.58.218.106/images/" + mDataList.get(position).getId() + ".png")
                        .placeholder(mContext.getResources().getDrawable(R.drawable.mulya_bg))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.picture);
            } else if (mDataList.get(position).getContentType() != null
                    && mDataList.get(position).getContentType().equalsIgnoreCase("Video")) {
                holder.picture.setVisibility(View.GONE);
                holder.audioLayout.setVisibility(View.GONE);
                holder.layout_Video.setVisibility(View.VISIBLE);
              /*  holder.card_video.setVideoPath("http://13.58.218.106/images/" + mDataList.get(position).getId() + ".mp4");
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
                      /*  holder.songProgressBar.setProgress(0);
                        holder.songProgressBar.setMax(100);

                      Runnable mUpdateTimeTask = new Runnable() {
                            public void run() {
                                long totalDuration = mPlayer.getDuration();
                                long currentDuration = mPlayer.getCurrentPosition();

                                // Displaying Total Duration time
                               *//* songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
                                // Displaying time completed playing
                                songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
*//*
                                // Updating progress bar
                                int progress = (int)(Utills.getProgressPercentage(currentDuration, totalDuration));
                                //Log.d("Progress", ""+progress);
                                holder.songProgressBar.setProgress(progress);

                                // Running this thread after 100 milliseconds
                                mHandler.postDelayed(this, 100);
                            }
                        };
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        mHandler.postDelayed(mUpdateTimeTask, 100);*/
                        if (temp == 555500) {
                            temp = position;

                            startAudio("http://13.58.218.106/images/" + mDataList.get(position).getId() + ".mp3");
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

                            startAudio("http://13.58.218.106/images/" + mDataList.get(position).getId() + ".mp3");
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
            }
        }

        holder.layout_share.setVisibility(View.GONE);
        holder.txt_title.setText("" + mDataList.get(position).getUserName());
       /* if (mDataList.get(position).getSynchStatus() != null
                && mDataList.get(position).getSynchStatus().equalsIgnoreCase(Constants.STATUS_LOCAL))
            holder.txt_template_type.setText("Template Type : " + mDataList.get(position).getTemplateName());
        else
            holder.txt_template_type.setText("Template Type : " + mDataList.get(position).getTemplate());*/
        holder.txt_template_type.setText("Title : " + mDataList.get(position).getTitle());
        holder.txt_desc.setText("Description : " + mDataList.get(position).getDescription());
        // Linkify.addLinks(holder.txt_desc, urlPattern, mDataList.get(position).getDescription());
        //  Linkify.addLinks(holder.txt_desc,Linkify.WEB_URLS);
        //  android.util.Patterns.WEB_URL.matcher( mDataList.get(position).getDescription()).matches();

        holder.txt_time.setText(mDataList.get(position).getTime().toString());
        holder.txtLikeCount.setText(mDataList.get(position).getLikeCount() + " Likes");
        holder.txtCommentCount.setText(mDataList.get(position).getCommentCount() + " Comments");


        holder.txt_type.setText(mDataList.get(position).getIssue_priority());
        if ((mDataList.get(position).getIsLike()) && (mDataList.get(position).getUser_id().equalsIgnoreCase(User.getCurrentUser(mContext).getId())))
            holder.imgLike.setImageResource(R.drawable.like);
        else
            holder.imgLike.setImageResource(R.drawable.dislike);

        if (mDataList.get(position).getCommentCount() == 0) {
            holder.img_comment.setImageResource(R.drawable.no_comment);
        } else {
            holder.img_comment.setImageResource(R.drawable.comment);
        }
      /* if (mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("false")){
            holder.layout_download_file.setVisibility(View.GONE);
            holder.layout_download.setVisibility(View.VISIBLE);
       }else {
           holder.layout_download_file.setVisibility(View.VISIBLE);
       }*/

        Log.i("Value", "Position " + position + " : " + isFileAvalible(position));
        if (isFileAvalible(position) || mDataList.get(position).getIsAttachmentPresent().equalsIgnoreCase("false")) {
            holder.layout_download_file.setVisibility(View.GONE);
            holder.layout_download.setVisibility(View.VISIBLE);

        } else {
            holder.layout_download_file.setVisibility(View.VISIBLE);
            holder.layout_download.setVisibility(View.GONE);

        }

        if (mDataList.get(position).getUser_id().equals(User.getCurrentUser(mContext).getId())) {
            holder.imgMore.setVisibility(View.VISIBLE);
            holder.imgMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(mContext, holder.imgMore);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
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
                            }
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                }
            });
        } else {
            holder.imgMore.setVisibility(View.GONE);
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
        public ImageView picture, userImage, imgLike, img_comment, imageThumbnail;
        public CardView card_view;
        public TextView txt_audio_txt, txt_title, txt_template_type, txt_desc, txt_time, textViewLike, txtLikeCount, txtCommentCount, txt_type;
        public LinearLayout mediaLayout, layout_like, layout_comment, layout_share, layout_download, layout_download_file;
        public RelativeLayout audioLayout, layout_Video;
        public ImageView play, imgMore;
        public ProgressBar songProgressBar;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txt_title = (TextView) itemLayoutView.findViewById(R.id.txt_title);
            txt_template_type = (TextView) itemLayoutView.findViewById(R.id.txt_template_type);
            txt_audio_txt = (TextView) itemLayoutView.findViewById(R.id.audio_text);
            txt_desc = (TextView) itemLayoutView.findViewById(R.id.txt_desc);
            txt_time = (TextView) itemLayoutView.findViewById(R.id.txt_time);
            txtLikeCount = (TextView) itemLayoutView.findViewById(R.id.txtLikeCount);
            txtCommentCount = (TextView) itemLayoutView.findViewById(R.id.txtCommentCount);
            userImage = (ImageView) itemLayoutView.findViewById(R.id.userImage);
            picture = (ImageView) itemLayoutView.findViewById(R.id.card_image);
            card_view = (CardView) itemLayoutView.findViewById(R.id.card_view);
            // songProgressBar = (ProgressBar) itemLayoutView.findViewById(R.id.songProgressBar);
            imgLike = (ImageView) itemLayoutView.findViewById(R.id.imgLike);
            textViewLike = (TextView) itemLayoutView.findViewById(R.id.textViewLike);
            img_comment = (ImageView) itemLayoutView.findViewById(R.id.img_comment);
            layout_comment = (LinearLayout) itemLayoutView.findViewById(R.id.layout_comment);
            imageThumbnail = (ImageView) itemLayoutView.findViewById(R.id.card_Thumbnail);
            txt_type = (TextView) itemLayoutView.findViewById(R.id.txt_type);
            layout_download_file = (LinearLayout) itemLayoutView.findViewById(R.id.layout_download_file);
            layout_download_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                //    Utills.showToast("download",mContext);
                    startDownload(getAdapterPosition());

                }
            });
            imgMore = (ImageView) itemLayoutView.findViewById(R.id.imgMore);

            layout_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, CommentActivity.class);
                    intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                    mContext.startActivity(intent);
                }
            });


            audioLayout = (RelativeLayout) itemLayoutView.findViewById(R.id.audioLayout);
            mediaLayout = (LinearLayout) itemLayoutView.findViewById(R.id.mediaLayout);
            layout_share = (LinearLayout) itemLayoutView.findViewById(R.id.layout_share);
            layout_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startDownload(getAdapterPosition());

                }
            });
            play = (ImageView) itemLayoutView.findViewById(R.id.play);
            layout_Video = (RelativeLayout) itemLayoutView.findViewById(R.id.layout_Video);
            layout_Video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myIntent = new Intent(mContext,
                            VideoViewActivity.class);
                    myIntent.putExtra("URL", "http://13.58.218.106/images/" + mDataList.get(getAdapterPosition()).getId() + ".mp4");
                    mContext.startActivity(myIntent);
                }
            });
            layout_download = (LinearLayout) itemLayoutView.findViewById(R.id.layout_download);
            layout_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent().equalsIgnoreCase("true")) {
                        String filePath = "";

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
                    } else {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image*//**//*");
                        i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());
                        Utills.hideProgressDialog();
                        mContext.startActivity(Intent.createChooser(i, "Share Post"));
                    }
                   /* if (mDataList.get(getAdapterPosition()).getIsAttachmentPresent() == null
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
                                    i.setType("image*//**//**//**//*");
                                    i.putExtra(Intent.EXTRA_TEXT, "Title : " + mDataList.get(getAdapterPosition()).getTitle() + "\n\nDescription : " + mDataList.get(getAdapterPosition()).getDescription());
                                    i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(theBitmap, getAdapterPosition()));
                                    Utills.hideProgressDialog();
                                    mContext.startActivity(Intent.createChooser(i, "Share Post"));
                                }
                            }
                        }.execute();
                    }*/
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
                   /* Intent intent = new Intent(mContext, CommunityDetailsActivity.class);
                    intent.putExtra(Constants.CONTENT, mDataList.get(getAdapterPosition()));
                    mContext.startActivity(intent);*/
                }
            });

            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utills.showImageZoomInDialog(v.getContext(), mDataList.get(getAdapterPosition()).getId());


                }
            });
        }


    }

    private void showDeletePopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.text_are_you_sure));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.text_delete));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                // Write your code here to execute after dialog closed
              /*  listOfWrongQuestions.add(mPosition);
                prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
            }
        });
        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DeletePost();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void DeletePost() {
        Utills.showProgressDialog(mContext);

        ServiceRequest apiService =
                ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
        apiService.getSalesForceData(preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/DeletePost/" + postId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    AppDatabase.getAppDatabase(mContext).userDao().deletePost(postId);
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

    public void startDownload(int position) {
        Utills.showToast("Downloading Started...", mContext);
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra("fragment_flag", "ThetSanvad_Fragment");
        if (mDataList.get(position).getContentType().equalsIgnoreCase("zip")) {
            intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".zip");
            intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "zip");
            intent.putExtra("URL", "http://13.58.218.106/images/" + mDataList.get(position).getId() + ".zip");
        } else if (mDataList.get(position).getContentType().equalsIgnoreCase("pdf")) {
            intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".pdf");
            intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "pdf");
            intent.putExtra("URL", "http://13.58.218.106/images/" + mDataList.get(position).getId() + ".pdf");
        } else if (mDataList.get(position).getContentType().equalsIgnoreCase("audio")) {
            intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".mp3");
            intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "audio");
            intent.putExtra("URL", "http://13.58.218.106/images/" + mDataList.get(position).getId() + ".mp3");
        } else if (mDataList.get(position).getContentType().equalsIgnoreCase("video")) {
            intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".mp4");
            intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "video");
            intent.putExtra("URL", "http://13.58.218.106/images/" + mDataList.get(position).getId() + ".mp4");
        } else if (mDataList.get(position).getContentType().equalsIgnoreCase("Image")) {
            intent.putExtra("FILENAME", mDataList.get(position).getTitle() + ".png");
            intent.putExtra("FILETYPE", mDataList.get(position).getContentType() + "Image");
            intent.putExtra("URL", "http://13.58.218.106/images/" + mDataList.get(position).getId() + ".png");
        }
        mContext.startService(intent);
    }

    private boolean isFileAvalible(int position) {
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
        }
        return false;
    }

}

