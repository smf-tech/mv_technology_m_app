package com.mv.MenuActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Activity.BroadCastActivity;
import com.mv.Adapter.FragmentContentAdapter;
import com.mv.Model.Content;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.FragmentCommunityHomeBinding;

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

/**
 * Created by Rohit Gujar on 26-10-2017.
 */

public class CommunityHomeFragment extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private FragmentCommunityHomeBinding binding;
    private PreferenceHelper preferenceHelper;
    private ArrayList<Content> chatList = new ArrayList<Content>();
    private FragmentContentAdapter adapter;
    TextView textNoData;

    private FloatingActionButton fab_add_broadcast;
    RecyclerView recyclerView;
    Activity context;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        context=this;
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_community_home);
        binding.setFragment(this);

        //here data must be an instance of the class MarsDataProvider
        Utills.setupUI(findViewById(R.id.layout_main), context);
        binding.swipeRefreshLayout.setOnRefreshListener(this);
        initViews();
        getChats(true);
    }

    private void initViews() {
        preferenceHelper = new PreferenceHelper(context);
        fab_add_broadcast = (FloatingActionButton) findViewById(R.id.fab_add_broadcast);
        textNoData = (TextView) findViewById(R.id.textNoData);
        fab_add_broadcast.setOnClickListener(this);
        binding.fabAddBroadcast.setVisibility(View.GONE);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        adapter = new FragmentContentAdapter(context, chatList);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

    private void getChats(boolean isDialogShow) {
        List<Content> temp = AppDatabase.getAppDatabase(context).userDao().getAllBroadcastChats();
        if (temp.size() == 0) {
            if (Utills.isConnected(context))
                getAllChats(false, isDialogShow);
            else
                showPopUp();
        } else {
            chatList.clear();
            for (int i = 0; i < temp.size(); i++) {
                chatList.add(temp.get(i));
            }
            adapter.notifyDataSetChanged();
            if (Utills.isConnected(context))
                getAllChats(true, isDialogShow);
        }

    }

    private void getAllChats(boolean isTimePresent, boolean isDialogShow) {
        if (isDialogShow)
            Utills.showProgressDialog(context, "Loading Chats", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = "";
        if (isTimePresent)
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getBroadcastContent?userId=" + User.getCurrentUser(context).getId()
                    + "&timestamp=" + chatList.get(0).getTime();
        else
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getBroadcastContent?userId=" + User.getCurrentUser(context).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swipeRefreshLayout.setRefreshing(false);
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str != null && str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);

                                Log.e("array length,", String.valueOf(jsonArray.length()));
                                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                                List<Content> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Content[].class));

                                List<Content> contentList = AppDatabase.getAppDatabase(context).userDao().getAllBroadcastChats();
                            if ((temp.size() != 0) || (contentList.size()!=0)) {

                                for (int i = 0; i < temp.size(); i++) {
                                    int j;
                                    boolean isPresent = false;
                                    for (j = 0; j < contentList.size(); j++) {
                                        if (contentList.get(j).getId().equalsIgnoreCase(temp.get(i).getId())) {
                                            temp.get(i).setUnique_Id(contentList.get(j).getUnique_Id());
                                            isPresent = true;
                                            break;
                                        }
                                    }
                                    if (isPresent) {
                                        chatList.set(j, temp.get(i));
                                        AppDatabase.getAppDatabase(context).userDao().updateContent(temp.get(i));
                                    } else {
                                        chatList.add(temp.get(i));
                                        AppDatabase.getAppDatabase(context).userDao().insertChats(temp.get(i));
                                    }
                                }
                                adapter.notifyDataSetChanged();
                                textNoData.setVisibility(View.GONE);
                            }else {
                                     Log.e("temp size", String.valueOf(temp.size()));
                                     adapter.notifyDataSetChanged();
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
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
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
                context.finish();
                context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                context.finish();
                context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add_broadcast:
                Intent intent;
                intent = new Intent(context, BroadCastActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {

        binding.swipeRefreshLayout.setRefreshing(false);
        getChats(false);
    }
}
