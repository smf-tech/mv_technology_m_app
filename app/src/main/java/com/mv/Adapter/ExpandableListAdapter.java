package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
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

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<DownloadContent>> _listDataChild;
    private ExpandableListActivity _activity;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<DownloadContent>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._activity = (ExpandableListActivity) context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
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
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater != null ? infalInflater.inflate(R.layout.each_trainging, null) : null;
        }

        ImageView imgDownload, imgshare;
        TextView txtCount, txtName;
        RelativeLayout layoutMain;


        layoutMain = convertView.findViewById(R.id.layoutMain);
        txtCount = convertView.findViewById(R.id.txtCount);
        layoutMain.setOnClickListener(v -> {
               /* Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///" + Environment.getExternalStorageDirectory().getPath() + "/MV_e-learning_Mar/Modules/1/story_html5.html"));
                _context.startActivity(browserIntent);*/
            if (isFileAvalible(content)) {
                if (content.getFileType().equalsIgnoreCase("zip")) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/UnZip/" + content.getName();
                    if (new File(filePath + "/story_html5.html").exists()) {
                        Intent intent = new Intent(_context, ActivityWebView.class);
                        intent.putExtra(Constants.URL, "file:///" + filePath + "/story_html5.html");
                        intent.putExtra(Constants.TITLE, content.getName());
                        _context.startActivity(intent);
                    } else {
                        deleteRecursive(new File(filePath));
                        notifyDataSetChanged();
                        Utills.showToast("File is corrupted. Please download again.", _context);
                    }
                } else if (content.getFileType().equalsIgnoreCase("audio")) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + content.getName() + ".mp3";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    File file = new File("/sdcard/test.mp3");
                    intent.setDataAndType(Uri.fromFile(new File(filePath)), "audio/*");
                    PackageManager packageManager = _context.getPackageManager();
                    if (intent.resolveActivity(packageManager) != null) {
                        _context.startActivity(intent);
                    } else {
                        Utills.showToast("No Application available to open Audio file", _context);
                    }
                } else if (content.getFileType().equalsIgnoreCase("video")) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + content.getName() + ".mp4";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(filePath)), "video/*");
                    PackageManager packageManager = _context.getPackageManager();
                    if (intent.resolveActivity(packageManager) != null) {
                        _context.startActivity(intent);
                    } else {
                        Utills.showToast("No Application available to open Video file", _context);
                    }
                } else if (content.getFileType().equalsIgnoreCase("pdf")) {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + content.getName() + ".pdf";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PackageManager packageManager = _context.getPackageManager();
                    if (intent.resolveActivity(packageManager) != null) {
                        _context.startActivity(intent);
                    } else {
                        Utills.showToast("No Application available to open PDF file", _context);
                    }
                } else if (content.getFileType().equalsIgnoreCase("ppt")) {
                      /*  String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + content.getName() + ".pdf";
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PackageManager packageManager = _context.getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            _context.startActivity(intent);
                        } else {
                            Utills.showToast("No Application available to open PDF file", _context);
                        }*/
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + content.getName() + ".ppt";
                    Uri uri = Uri.fromFile(new File(filePath));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                    PackageManager pm = _context.getPackageManager();
                    List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
                    if (list.size() > 0)
                        _context.startActivity(intent);
                    else
                        Utills.showToast("No Application available to open PPT file", _context);
                }
            } else {
                showNoFilePresentPopUp();
            }


        });
        imgDownload = convertView.findViewById(R.id.imgDownload);
     //   imgshare = convertView.findViewById(R.id.imgshare);
        imgDownload.setOnClickListener(view -> _activity.startDownload(content));
        imgshare = convertView.findViewById(R.id.imgshare);
        imgshare.setOnClickListener(v -> {
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

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.e("file path", filePath);

            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
            _context.startActivity(Intent.createChooser(intent, "Share Content"));
        });

        txtName = convertView.findViewById(R.id.txtName);

        txtCount.setVisibility(View.GONE);
        txtName.setText(content.getName());

        if (isFileAvalible(content)) {
            imgDownload.setVisibility(View.GONE);
            imgshare.setVisibility(View.VISIBLE);
        } else {
            imgDownload.setVisibility(View.VISIBLE);
            imgshare.setVisibility(View.GONE);
        }
        return convertView;
    }

    private void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    private void showNoFilePresentPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(_context).create();

        // Setting Dialog Title
        alertDialog.setTitle(_context.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(_context.getString(R.string.fileNotPresent));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);
        // Setting OK Button
        alertDialog.setButton(_context.getString(android.R.string.ok), (dialog, which) -> alertDialog.dismiss());

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    private boolean isFileAvalible(DownloadContent downloadContent) {
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
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater != null ? infalInflater.inflate(R.layout.list_group, null) : null;
        }
        ImageView imgGroup = convertView.findViewById(R.id.imgGroup);
        if (isExpanded) {
            imgGroup.setImageResource(R.drawable.downarrow);
        } else {
            imgGroup.setImageResource(R.drawable.rightarrow);
        }
        TextView txtName = convertView
                .findViewById(R.id.txtName);
        //     date.setTypeface(null, Typeface.BOLD);
        txtName.setText(headerTitle);

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
