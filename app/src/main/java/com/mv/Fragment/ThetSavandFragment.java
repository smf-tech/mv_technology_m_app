package com.mv.Fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Activity.AddThetSavadActivity;
import com.mv.Adapter.ThetSavandAdapter;
import com.mv.Model.Content;
import com.mv.Model.Download;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.MediaSongSingleToneClass;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.FragmentThetSavandBinding;

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
 * Created by Nanostuffs on 27-12-2017.
 */

public class ThetSavandFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private FragmentThetSavandBinding binding;
    private PreferenceHelper preferenceHelper;
    private List<Content> chatList = new ArrayList<Content>();
    private ArrayList<Content> mypostlist = new ArrayList<>();
    private ThetSavandAdapter adapter;
    private View view;
    private Boolean mySelection = false;
    private FloatingActionButton fab_add_broadcast;
    MediaPlayer mPlayer = MediaSongSingleToneClass.getInstance();
    ThetSavandFragment fragment;
    Button btn_mypost, btn_allposts;
    LinearLayout lnr_filter;
    RecyclerView recyclerView;
    TextView textNoData;
    public static final String MESSAGE_PROGRESS = "message_progress";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_thet_savand, container, false);
        view = binding.getRoot();
        fragment = this;
        //here data must be an instance of the class MarsDataProvider
        Utills.setupUI(view.findViewById(R.id.layout_main), getActivity());
        binding.swipeRefreshLayout.setOnRefreshListener(this);
        initViews();
        getChats(true);
        return view;
    }

    public void onAddClick() {

    }

    private void initViews() {
        preferenceHelper = new PreferenceHelper(getActivity());
        fab_add_broadcast = (FloatingActionButton) view.findViewById(R.id.fab_add_broadcast);
        fab_add_broadcast.setOnClickListener(this);
        binding.fabAddBroadcast.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        textNoData = (TextView) view.findViewById(R.id.textNoData);
        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        btn_allposts = (Button) view.findViewById(R.id.btn_allposts);
        btn_mypost = (Button) view.findViewById(R.id.btn_mypost);
        lnr_filter = (LinearLayout) view.findViewById(R.id.lnr_filter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < -5 && ((lnr_filter.getVisibility() == View.GONE))) {
                    lnr_filter.setVisibility(View.VISIBLE);
                    //fab_add_broadcast.setVisibility(View.VISIBLE);
                } else if (dy > 5 && (lnr_filter.getVisibility() == View.VISIBLE)) {
                    lnr_filter.setVisibility(View.GONE);
                    // fab_add_broadcast.setVisibility(View.INVISIBLE);
                }

            }
        });

        btn_mypost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mypostlist.clear();
              btn_mypost.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
              btn_allposts.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));

                for (int i = 0; i < chatList.size(); i++) {
                    if (chatList.get(i).getUser_id().equals(User.getCurrentUser(getActivity()).getId())) {
                        mypostlist.add(chatList.get(i));
                    }
                }
                mySelection = true;
                adapter = new ThetSavandAdapter(getActivity(), fragment, mypostlist);
                recyclerView.setAdapter(adapter);
            }
        });
        btn_allposts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySelection = false;
               btn_allposts.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
              btn_mypost.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));

                adapter = new ThetSavandAdapter(getActivity(), fragment, chatList);
                recyclerView.setAdapter(adapter);
            }
        });
        registerReceiver();
    }

    public void stopAudio() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
    }


    private void getChats(boolean isDialogShow) {
        chatList.clear();
        chatList = AppDatabase.getAppDatabase(getActivity()).userDao().getThetSavandChats();
        if (chatList.size() == 0) {
            if (Utills.isConnected(getActivity()))
                getAllChats(false, isDialogShow);
            else
                showPopUp();
        } else {

            if (mySelection) {
                adapter = new ThetSavandAdapter(getActivity(), this, mypostlist);
                recyclerView.setAdapter(adapter);
            } else {
                adapter = new ThetSavandAdapter(getActivity(), this, chatList);
                recyclerView.setAdapter(adapter);
            }
            if (Utills.isConnected(getActivity()))
                getAllChats(true, isDialogShow);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
        getChats(true);
    }

    private void getAllChats(boolean isTimePresent, boolean isDialogShow) {
        if (isDialogShow)
            Utills.showProgressDialog(getActivity(), "Loading Chats", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(getActivity()).create(ServiceRequest.class);
        String url = "";
        if (isTimePresent)
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getTheatSawandContent?userId=" + User.getCurrentUser(getActivity()).getId()
                    + "&timestamp=" + chatList.get(0).getTime();
        else
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getTheatSawandContent?userId=" + User.getCurrentUser(getActivity()).getId();
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
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            List<Content> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Content[].class));
                            List<Content> contentList = AppDatabase.getAppDatabase(getActivity()).userDao().getThetSavandChats();
                            if ((temp.size() != 0) || (contentList.size() != 0)) {
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
                                        AppDatabase.getAppDatabase(getActivity()).userDao().updateContent(temp.get(i));
                                    } else {
                                        chatList.add(0, temp.get(i));
                                        AppDatabase.getAppDatabase(getActivity()).userDao().insertChats(temp.get(i));
                                    }
                                }

                                mypostlist.clear();

                                for (int i = 0; i < chatList.size(); i++) {

                                    if (chatList.get(i).getUser_id().equals(User.getCurrentUser(getActivity()).getId())) {
                                        mypostlist.add(chatList.get(i));
                                    }
                                }

                                if (mySelection) {
                                    adapter = new ThetSavandAdapter(getActivity(), fragment, mypostlist);
                                    recyclerView.setAdapter(adapter);
                                } else {
                                    adapter = new ThetSavandAdapter(getActivity(), fragment, chatList);
                                    recyclerView.setAdapter(adapter);
                                }
                                textNoData.setVisibility(View.GONE);
                            }
                        } else {
                            textNoData.setVisibility(View.VISIBLE);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add_broadcast:
                Intent intent;
                intent = new Intent(getActivity(), AddThetSavadActivity.class);
                intent.putExtra("EDIT", false);
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

    private void registerReceiver() {

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }


}
