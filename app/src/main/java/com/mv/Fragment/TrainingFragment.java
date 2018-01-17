package com.mv.Fragment;

/**
 * Created by Rohit Gujar on 09-10-2017.
 */

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.TrainingAdapter;
import com.mv.Model.Download;
import com.mv.Model.DownloadContent;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.DownloadService;
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

public class TrainingFragment extends Fragment {
    public static final String MESSAGE_PROGRESS = "message_progress";
    private RecyclerView recyclerView;
    private View view;
    private TrainingAdapter adapter;
    private PreferenceHelper preferenceHelper;
    private ArrayList<DownloadContent> mList = new ArrayList<DownloadContent>();
    TextView textNoData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_training, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        textNoData = (TextView) view.findViewById(R.id.textNoData);
        preferenceHelper = new PreferenceHelper(getActivity());
        registerReceiver();
        setRecyclerView();
        if (TextUtils.isEmpty(preferenceHelper.getString(PreferenceHelper.TrainingContentData))) {
            if (Utills.isConnected(getActivity())) {
                getData();
            } else {
                Utills.showInternetPopUp(getActivity());
            }
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            List<DownloadContent> temp = Arrays.asList(gson.fromJson(preferenceHelper.getString(PreferenceHelper.TrainingContentData), DownloadContent[].class));
            mList.clear();
            for (DownloadContent content : temp) {
                mList.add(content);
            }
            adapter.notifyDataSetChanged();
            if (Utills.isConnected(getActivity())) {
                getData();
            }
        }


        return view;
    }

    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

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
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    private void getData() {
        Utills.showProgressDialog(getActivity(), "Loading Downloads", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(getActivity()).create(ServiceRequest.class);
        String url = "";
        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getdownloadContentData?userId=" + User.getCurrentUser(getActivity()).getId();
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
                             if (temp.size()!=0) {
                                 if (temp.size() > 0)
                                     preferenceHelper.insertString(PreferenceHelper.TrainingContentData, str);
                                 mList.clear();
                                 for (DownloadContent content : temp) {
                                     mList.add(content);
                                 }
                                 adapter.notifyDataSetChanged();
                                 textNoData.setVisibility(View.GONE);
                             }else {
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

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }

    public void startDownload(int position) {
        Utills.showToast("Downloading Started...", getActivity());
        Intent intent = new Intent(getActivity(), DownloadService.class);
        intent.putExtra("URL", mList.get(position).getUrl());
        intent.putExtra("fragment_flag","Training_Fragment");
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
        getActivity().startService(intent);
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
        adapter = new TrainingAdapter(getActivity(), this, mList);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
