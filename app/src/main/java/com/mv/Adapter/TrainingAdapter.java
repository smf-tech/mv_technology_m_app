

package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.ActivityWebView;
import com.mv.Fragment.TrainingFragment;
import com.mv.Model.DownloadContent;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.Utills;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by acer on 9/8/2016.
 */


public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.ViewHolder> {

    private Context mContext;
    private Resources resources;
    private ArrayList<DownloadContent> mDataList;
    private TrainingFragment trainingFragment;

    public TrainingAdapter(Context context, TrainingFragment fragment, ArrayList<DownloadContent> list) {
        mContext = context;
        resources = context.getResources();
        mDataList = list;
        trainingFragment = fragment;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_trainging, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDownload;
        TextView txtCount, txtName;
        RelativeLayout layoutMain;

        public ViewHolder(View itemLayoutView) {

            super(itemLayoutView);
            layoutMain = (RelativeLayout) itemLayoutView.findViewById(R.id.layoutMain);
            txtCount = (TextView) itemLayoutView.findViewById(R.id.txtCount);
            layoutMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///" + Environment.getExternalStorageDirectory().getPath() + "/MV_e-learning_Mar/Modules/1/story_html5.html"));
                    mContext.startActivity(browserIntent);*/
                    if (isFileAvalible(getAdapterPosition())) {
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/UnZip/" + mDataList.get(getAdapterPosition()).getName();
                        if (new File(filePath + "/story_html5.html").exists()) {
                            Intent intent = new Intent(mContext, ActivityWebView.class);
                            intent.putExtra(Constants.URL, "file:///" + filePath + "/story_html5.html");
                            intent.putExtra(Constants.TITLE, mDataList.get(getAdapterPosition()).getName());
                            mContext.startActivity(intent);
                        } else {
                            deleteRecursive(new File(filePath));
                            notifyDataSetChanged();
                            Utills.showToast("File is corrupted. Please download again.", mContext);
                        }
                    } else {
                        showNoFilePresentPopUp();
                    }


                }
            });
            imgDownload = (ImageView) itemLayoutView.findViewById(R.id.imgDownload);
            imgDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    trainingFragment.startDownload(getAdapterPosition());
                }
            });

            txtName = (TextView) itemLayoutView.findViewById(R.id.txtName);
        }
    }

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    private void showNoFilePresentPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.fileNotPresent));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);
        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtCount.setVisibility(View.GONE);
        holder.txtName.setText(mDataList.get(position).getName());
        if (isFileAvalible(position)) {
            holder.imgDownload.setVisibility(View.GONE);
        } else {
            holder.imgDownload.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFileAvalible(int position) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/UnZip/" + mDataList.get(position).getName();
        if (new File(filePath).exists())
            return true;
        return false;
    }


}

