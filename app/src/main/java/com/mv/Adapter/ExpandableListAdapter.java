package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.ActivityWebView;
import com.mv.Activity.ExpandableListActivity;
import com.mv.Model.DownloadContent;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.Utills;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    // header titles
    private List<String> mListDataHeader;
    // child data in format of header title, child title
    private HashMap<String, List<DownloadContent>> mListDataChild;
    private ExpandableListActivity mActivity;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<DownloadContent>> listChildData) {
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mListDataChild = listChildData;
        this.mActivity = (ExpandableListActivity) context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final DownloadContent content = (DownloadContent) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater != null ? infalInflater.inflate(R.layout.each_trainging, null) : null;
        }

        ImageView imgDownload, imgShare;
        TextView txtCount, txtName;
        RelativeLayout layoutMain;

        if (convertView != null) {
            layoutMain = convertView.findViewById(R.id.layoutMain);
            txtCount = convertView.findViewById(R.id.txtCount);

            layoutMain.setOnClickListener(v -> {
                if (isFileAvailable(content)) {
                    if (content.getFileType().equalsIgnoreCase("zip")) {
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/UnZip/" + content.getName();
                        if (new File(filePath + "/story_html5.html").exists()) {
                            Intent intent = new Intent(mContext, ActivityWebView.class);
                            intent.putExtra(Constants.URL, "file:///" + filePath + "/story_html5.html");
                            intent.putExtra(Constants.TITLE, content.getName());
                            mContext.startActivity(intent);
                        } else {
                            deleteRecursive(new File(filePath));
                            notifyDataSetChanged();
                            Utills.showToast("File is corrupted. Please download again.", mContext);
                        }
                    } else if (content.getFileType().equalsIgnoreCase("audio")) {
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/MV/Zip/" + content.getName() + ".mp3";
                        File audioFile = new File(filePath);
                        Uri outputUri = FileProvider.getUriForFile(mContext,
                                mContext.getPackageName() + ".fileprovider", audioFile);

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(outputUri, "audio/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        PackageManager packageManager = mContext.getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            mContext.startActivity(intent);
                        } else {
                            Utills.showToast("No Application available to open Audio file", mContext);
                        }
                    } else if (content.getFileType().equalsIgnoreCase("video")) {
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/MV/Zip/" + content.getName() + ".mp4";
                        File videoFile = new File(filePath);
                        Uri outputUri = FileProvider.getUriForFile(mContext,
                                mContext.getPackageName() + ".fileprovider", videoFile);

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(outputUri, "video/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        PackageManager packageManager = mContext.getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            mContext.startActivity(intent);
                        } else {
                            Utills.showToast("No Application available to open Video file", mContext);
                        }
                    } else if (content.getFileType().equalsIgnoreCase("pdf")) {
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/MV/Zip/" + content.getName() + ".pdf";
                        File pdfFile = new File(filePath);
                        Uri outputUri = FileProvider.getUriForFile(mContext,
                                mContext.getPackageName() + ".fileprovider", pdfFile);

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(outputUri, "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        PackageManager packageManager = mContext.getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            mContext.startActivity(intent);
                        } else {
                            Utills.showToast("No Application available to open PDF file", mContext);
                        }
                    } else if (content.getFileType().equalsIgnoreCase("ppt")) {
                        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/MV/Zip/" + content.getName() + ".ppt";
                        File pptFile = new File(filePath);
                        Uri outputUri = FileProvider.getUriForFile(mContext,
                                mContext.getPackageName() + ".fileprovider", pptFile);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(outputUri, "application/vnd.ms-powerpoint");

                        PackageManager pm = mContext.getPackageManager();
                        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
                        if (list.size() > 0) {
                            mContext.startActivity(intent);
                        } else {
                            Utills.showToast("No Application available to open PPT file", mContext);
                        }
                    }
                } else {
                    showNoFilePresentPopUp();
                }
            });

            imgDownload = convertView.findViewById(R.id.imgDownload);
            imgDownload.setOnClickListener(view -> mActivity.startDownload(content));
            imgShare = convertView.findViewById(R.id.imgshare);

            imgShare.setOnClickListener(v -> {
                String filePath = "";

                if (content.getFileType().equalsIgnoreCase("audio")) {
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + content.getName() + ".mp3";
                } else if (content.getFileType().equalsIgnoreCase("video")) {
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + content.getName() + ".mp4";
                } else if (content.getFileType().equalsIgnoreCase("pdf")) {
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + content.getName() + ".pdf";
                } else if (content.getFileType().equalsIgnoreCase("zip")) {
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + content.getName() + ".zip";
                }

                File pptFile = new File(filePath);
                Uri outputUri = FileProvider.getUriForFile(mContext,
                        mContext.getPackageName() + ".fileprovider", pptFile);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("application/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Log.e("file path", filePath);

                intent.putExtra(Intent.EXTRA_STREAM, outputUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(Intent.createChooser(intent, "Share Content"));
            });

            txtName = convertView.findViewById(R.id.txtName);
            txtCount.setVisibility(View.GONE);
            txtName.setText(content.getName());

            if (isFileAvailable(content)) {
                imgDownload.setVisibility(View.GONE);
                imgShare.setVisibility(View.VISIBLE);
            } else {
                imgDownload.setVisibility(View.VISIBLE);
                imgShare.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    private void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        boolean delete = fileOrDirectory.delete();
        System.out.print("File deleted" + delete);
    }

    @SuppressWarnings("deprecation")
    private void showNoFilePresentPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.fileNotPresent));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), (dialog, which) -> alertDialog.dismiss());

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition)).size();
    }

    private boolean isFileAvailable(DownloadContent downloadContent) {
        if (downloadContent.getFileType() != null) {
            if (downloadContent.getFileType().equalsIgnoreCase("zip")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/UnZip/" + downloadContent.getName();
                return new File(filePath).exists();
            } else if (downloadContent.getFileType().equalsIgnoreCase("pdf")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + downloadContent.getName() + ".pdf";
                return new File(filePath).exists();
            } else if (downloadContent.getFileType().equalsIgnoreCase("video")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + downloadContent.getName() + ".mp4";
                return new File(filePath).exists();
            } else if (downloadContent.getFileType().equalsIgnoreCase("audio")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + downloadContent.getName() + ".mp3";
                return new File(filePath).exists();
            } else if (downloadContent.getFileType().equalsIgnoreCase("ppt")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + downloadContent.getName() + ".ppt";
                return new File(filePath).exists();
            }
        }
        return false;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater != null ? layoutInflater.inflate(R.layout.list_group, null) : null;
        }

        if (convertView != null) {
            ImageView imgGroup = convertView.findViewById(R.id.imgGroup);
            if (isExpanded) {
                imgGroup.setImageResource(R.drawable.downarrow);
            } else {
                imgGroup.setImageResource(R.drawable.rightarrow);
            }

            TextView txtName = convertView.findViewById(R.id.txtName);
            txtName.setText(headerTitle);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}