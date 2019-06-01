package com.mv.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.ExpandableListAdapter;
import com.mv.Model.DownloadContent;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.DownloadService;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpandableListActivity extends Activity implements View.OnClickListener {
    private int lastExpandedPosition = -1;
    private static final String LANGUAGE = "language";
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader = new ArrayList<>();
    private HashMap<String, List<DownloadContent>> listDataChild = new HashMap<>();
    private static final String MESSAGE_PROGRESS = "message_progress";
    private TextView textNoData;
    private PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expandable_list);
        setActionbar(getString(R.string.training_content));
        textNoData = findViewById(R.id.textNoData);
        registerReceiver();
        preferenceHelper = new PreferenceHelper(this);

        // get the listview
        expListView = findViewById(R.id.lvExp);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        // Listview on child click listener
        expListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> false);
        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(groupPosition -> {
            if (lastExpandedPosition != -1
                    && groupPosition != lastExpandedPosition) {
                expListView.collapseGroup(lastExpandedPosition);
            }
            lastExpandedPosition = groupPosition;

        });
        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(groupPosition -> {
        });

        if (AppDatabase.getAppDatabase(ExpandableListActivity.this).userDao()
                .getDownloadContent(preferenceHelper.getString(LANGUAGE)).size() == 0) {

            if (Utills.isConnected(ExpandableListActivity.this)) {
                getData();
            } else {
                Utills.showInternetPopUp(ExpandableListActivity.this);
            }
        } else {
            prepareListData();
            listAdapter.notifyDataSetChanged();
            if (Utills.isConnected(ExpandableListActivity.this)) {
                getData();
            }
        }
//        temp();
    }
    public void temp(){
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/MV/Zip/Marathi.epub";
        File epubFile = new File(filePath);
        Uri outputUri = FileProvider.getUriForFile(this,
                this.getPackageName() + ".fileprovider", epubFile);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(outputUri, "application/epub+zip");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PackageManager packageManager = this.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            this.startActivity(intent);
        } else {
            showDialog();
        }
    }
    public void showDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getResources().getString(R.string.alert));
        alertDialog.setMessage(getResources().getString(R.string.alert_message));
        alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.faultexception.reader&hl=en" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.faultexception.reader&hl=en" + appPackageName)));
                }
            }
        });
        alertDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void getData() {
        Utills.showProgressDialog(ExpandableListActivity.this, "Loading Downloads", getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(ExpandableListActivity.this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getdownloadContentData?userId=" + User.getCurrentUser(ExpandableListActivity.this).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

                            List<DownloadContent> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), DownloadContent[].class));
                            if (temp.size() != 0) {
                                AppDatabase.getAppDatabase(ExpandableListActivity.this).userDao().clearDownloadContent();
                                AppDatabase.getAppDatabase(ExpandableListActivity.this).userDao().insertDownloadContent(temp);
                                prepareListData();
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void setActionbar(String title) {
        String str = title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }

        LinearLayout layoutList = findViewById(R.id.layoutList);
        layoutList.setVisibility(View.GONE);

        TextView toolbar_title = findViewById(R.id.toolbar_title);

        toolbar_title.setText(str);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }

    private void registerReceiver() {

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(ExpandableListActivity.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(MESSAGE_PROGRESS)) {
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    public void startDownload(DownloadContent content,final int groupPosition, final int childPosition) {
        Utills.showToast("Downloading Started...", ExpandableListActivity.this);
        Intent intent = new Intent(ExpandableListActivity.this, DownloadService.class);
        intent.putExtra("URL", content.getUrl());
        intent.putExtra("fragment_flag", "Training_Fragment");
        if (content.getFileType().equalsIgnoreCase("zip")) {
            intent.putExtra("FILENAME", content.getName() + ".zip");
            intent.putExtra("FILETYPE", "zip");
        } else if (content.getFileType().equalsIgnoreCase("pdf")) {
            intent.putExtra("FILENAME", content.getName() + ".pdf");
            intent.putExtra("FILETYPE", "pdf");
        } else if (content.getFileType().equalsIgnoreCase("audio")) {
            intent.putExtra("FILENAME", content.getName() + ".mp3");
            intent.putExtra("FILETYPE", "audio");
        } else if (content.getFileType().equalsIgnoreCase("video")) {
            intent.putExtra("FILENAME", content.getName() + ".mp4");
            intent.putExtra("FILETYPE", "video");
        } else if (content.getFileType().equalsIgnoreCase("ppt")) {
            intent.putExtra("FILENAME", content.getName() + ".ppt");
            intent.putExtra("FILETYPE", "ppt");
        }else if (content.getFileType().equalsIgnoreCase("epub")) {
            intent.putExtra("FILENAME", content.getName() + ".epub");
            intent.putExtra("FILETYPE", "epub");
        }
        listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setDownloadFlag(1);
        startService(intent);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader.clear();
        listDataChild.clear();

        String lang = preferenceHelper.getString(LANGUAGE);
        if (lang == null || lang.length() == 0) {
            lang = Constants.LANGUAGE_ENGLISH;
        }

        List<String> temp = AppDatabase.getAppDatabase(
                ExpandableListActivity.this).userDao().getDistinctDownloadContent(lang);
        listDataHeader.addAll(temp);

        for (String downloadContent : listDataHeader) {
            listDataChild.put(downloadContent, AppDatabase.getAppDatabase(
                    ExpandableListActivity.this).userDao().getDownloadContent(
                    downloadContent, preferenceHelper.getString(LANGUAGE)));
        }

        if (listDataHeader.size() > 0) {
            textNoData.setVisibility(View.GONE);
        } else {
            textNoData.setVisibility(View.VISIBLE);
        }
    }
}