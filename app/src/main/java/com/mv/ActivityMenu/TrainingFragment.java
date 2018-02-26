package com.mv.ActivityMenu;

/**
 * Created by Rohit Gujar on 09-10-2017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.TrainingAdapter;
import com.mv.Model.Download;
import com.mv.Model.DownloadContent;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.DownloadService;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrainingFragment extends AppCompatActivity implements View.OnClickListener {
    public static final String MESSAGE_PROGRESS = "message_progress";
    private RecyclerView recyclerView;
    private TrainingAdapter adapter;
    private PreferenceHelper preferenceHelper;
    private ArrayList<DownloadContent> mList = new ArrayList<DownloadContent>();
    TextView textNoData;
    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        context = this;
        setActionbar(getString(R.string.training_content));
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        textNoData = (TextView) findViewById(R.id.textNoData);
        preferenceHelper = new PreferenceHelper(context);
        registerReceiver();
        setRecyclerView();
      /*  if (AppDatabase.getAppDatabase(TrainingFragment.this).userDao().getDownloadContent().size() == 0) {
            if (Utills.isConnected(context)) {
                getData();
            } else {
                Utills.showInternetPopUp(context);
            }
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
          //  List<DownloadContent> temp = AppDatabase.getAppDatabase(TrainingFragment.this).userDao().getDownloadContent();
            mList.clear();
            for (DownloadContent content : temp) {
                mList.add(content);
            }
            adapter.notifyDataSetChanged();
            if (Utills.isConnected(context)) {
                getData();
            }
        }
*/
    }

    private void setActionbar(String Title) {
        String str = Title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }
        LinearLayout layoutList = (LinearLayout) findViewById(R.id.layoutList);
        layoutList.setVisibility(View.GONE);
        RelativeLayout mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);
        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                context.finish();
                context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage("Internet connection is required");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    private void getData() {
        Utills.showProgressDialog(context, "Loading Downloads", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = "";
        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getdownloadContentData?userId=" + User.getCurrentUser(context).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str != null && str.length() > 0) {

                            JSONArray jsonArray = new JSONArray(str);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            List<DownloadContent> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), DownloadContent[].class));
                            if (temp.size() != 0) {
                                mList.clear();
                                for (DownloadContent content : temp) {
                                    mList.add(content);
                                }
                                AppDatabase.getAppDatabase(TrainingFragment.this).userDao().clearDownloadContent();
                                AppDatabase.getAppDatabase(TrainingFragment.this).userDao().insertDownloadContent(mList);
                                adapter.notifyDataSetChanged();
                                textNoData.setVisibility(View.GONE);
                            } else {
                                textNoData.setVisibility(View.VISIBLE);
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

    private void registerReceiver() {

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }

    public void startDownload(int position) {
        Utills.showToast("Downloading Started...", context);
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("URL", mList.get(position).getUrl());
        intent.putExtra("fragment_flag", "Training_Fragment");
        if (mList.get(position).getFileType().equalsIgnoreCase("zip")) {
            intent.putExtra("FILENAME", mList.get(position).getName() + ".zip");
            intent.putExtra("FILETYPE", mList.get(position).getName() + "zip");
        } else if (mList.get(position).getFileType().equalsIgnoreCase("pdf")) {
            intent.putExtra("FILENAME", mList.get(position).getName() + ".pdf");
            intent.putExtra("FILETYPE", mList.get(position).getName() + "pdf");
        } else if (mList.get(position).getFileType().equalsIgnoreCase("audio")) {
            intent.putExtra("FILENAME", mList.get(position).getName() + ".mp3");
            intent.putExtra("FILETYPE", mList.get(position).getName() + "audio");
        } else if (mList.get(position).getFileType().equalsIgnoreCase("video")) {
            intent.putExtra("FILENAME", mList.get(position).getName() + ".mp4");
            intent.putExtra("FILETYPE", mList.get(position).getName() + "video");
        } else if (mList.get(position).getFileType().equalsIgnoreCase("ppt")) {
            intent.putExtra("FILENAME", mList.get(position).getName() + ".ppt");
            intent.putExtra("FILETYPE", mList.get(position).getName() + "ppt");
        }
        context.startService(intent);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MESSAGE_PROGRESS)) {
                Download download = intent.getParcelableExtra("download");
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        }
    };

    private void setRecyclerView() {
        adapter = new TrainingAdapter(context, this, mList);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }
}
