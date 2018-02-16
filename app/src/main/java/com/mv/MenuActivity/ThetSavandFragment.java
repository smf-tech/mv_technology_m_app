package com.mv.MenuActivity;

import android.app.Activity;
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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.mv.Utils.LocaleManager;
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

public class ThetSavandFragment extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private FragmentThetSavandBinding binding;
    private PreferenceHelper preferenceHelper;
    private List<Content> chatList = new ArrayList<Content>();
    private ArrayList<Content> mypostlist = new ArrayList<>();
    private ThetSavandAdapter adapter;

    private Boolean mySelection = false;
    private FloatingActionButton fab_add_broadcast;
    MediaPlayer mPlayer = MediaSongSingleToneClass.getInstance();
    ThetSavandFragment fragment;
    Button btn_mypost, btn_allposts;
    LinearLayout lnr_filter;
    RecyclerView recyclerView;
    TextView textNoData;
    Activity context;
    public static final String MESSAGE_PROGRESS = "message_progress";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        fragment = this;
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_thet_savand);
        binding.setFragment(this);

        //here data must be an instance of the class MarsDataProvider
        // Utills.setupUI(view.findViewById(R.id.layout_main), context);
        binding.swipeRefreshLayout.setOnRefreshListener(this);
        initViews();
        getChats(true);
    }

    /*It is used for setting different languages like english , marathi*/
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    /*Initialize differnt views. */
    private void initViews() {
        preferenceHelper = new PreferenceHelper(context);
        setActionbar(getString(R.string.thet_savnd));
        fab_add_broadcast = (FloatingActionButton) findViewById(R.id.fab_add_broadcast);
        binding.fabAddBroadcast.setOnClickListener(this);
        binding.fabAddBroadcast.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        textNoData = (TextView) findViewById(R.id.textNoData);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        btn_allposts = (Button) findViewById(R.id.btn_allposts);
        btn_mypost = (Button) findViewById(R.id.btn_mypost);
        lnr_filter = (LinearLayout) findViewById(R.id.lnr_filter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        /*Change the visiblity of filter button on scroll*/
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
         /*Display the post of only registered users */
        btn_mypost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mypostlist.clear();

                btn_mypost.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
                btn_allposts.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
                for (int i = 0; i < chatList.size(); i++) {
                    if (chatList.get(i).getUser_id().equals(User.getCurrentUser(context).getId())) {
                        mypostlist.add(chatList.get(i));
                    }
                }
                mySelection = true;
                adapter = new ThetSavandAdapter(context, fragment, mypostlist);
                recyclerView.setAdapter(adapter);
            }
        });
        /*Display all posts*/
        btn_allposts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySelection = false;
                btn_allposts.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
                btn_mypost.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
                adapter = new ThetSavandAdapter(context, fragment, chatList);
                recyclerView.setAdapter(adapter);
            }
        });

        /*It is from downloadservice to check file is completely downloaded or not.*/
        registerReceiver();
    }

    public void stopAudio() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
    }

    /*Get the Chat List from Database and set to the adapter , if No vales in table then get Chats from Server*/
    private void getChats(boolean isDialogShow) {
        chatList.clear();
        chatList = AppDatabase.getAppDatabase(context).userDao().getThetSavandChats(true);
        if (chatList.size() == 0) {
            if (Utills.isConnected(context))
                getAllChats(false, isDialogShow);
            else
                showPopUp();
        } else {

            if (mySelection) {
                adapter = new ThetSavandAdapter(context, this, mypostlist);
                recyclerView.setAdapter(adapter);
            } else {
                adapter = new ThetSavandAdapter(context, this, chatList);
                recyclerView.setAdapter(adapter);
            }
            if (Utills.isConnected(context))
                getAllChats(true, isDialogShow);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
        getChats(true);
    }

    /*getTheatSawandContent api is called here.*/
    private void getAllChats(boolean isTimePresent, boolean isDialogShow) {
        if (isDialogShow)
            Utills.showProgressDialog(ThetSavandFragment.this, "Loading Chats", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = "";
        if (isTimePresent)
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getTheatSawandContent?userId=" + User.getCurrentUser(context).getId()
                    + "&timestamp=" + chatList.get(0).getTime();
        else
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getTheatSawandContent?userId=" + User.getCurrentUser(context).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str != null && str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            List<Content> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Content[].class));
                            List<Content> contentList = AppDatabase.getAppDatabase(context).userDao().getThetSavandChats(true);
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
                                        AppDatabase.getAppDatabase(context).userDao().updateContent(temp.get(i));
                                    } else {
                                        chatList.add(0, temp.get(i));
                                        AppDatabase.getAppDatabase(context).userDao().insertChats(temp.get(i));
                                    }
                                }






                                mypostlist.clear();

                                for (int i = 0; i < chatList.size(); i++) {

                                    if (chatList.get(i).getUser_id().equals(User.getCurrentUser(context).getId())) {
                                        mypostlist.add(chatList.get(i));
                                    }
                                }

                                if (mySelection) {
                                    adapter = new ThetSavandAdapter(context, fragment, mypostlist);
                                    recyclerView.setAdapter(adapter);
                                } else {
                                    adapter = new ThetSavandAdapter(context, fragment, chatList);
                                    recyclerView.setAdapter(adapter);
                                }
                                textNoData.setVisibility(View.GONE);
                            }
                        } else {
                            textNoData.setVisibility(View.VISIBLE);
                        }
                    }
                    Utills.hideProgressDialog();
                    binding.swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Utills.hideProgressDialog();
                    binding.swipeRefreshLayout.setRefreshing(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    Utills.hideProgressDialog();
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /*It shows the popup for Internet connection is available or not*/
    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.error_no_internet));

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
                intent = new Intent(context, AddThetSavadActivity.class);
                intent.putExtra("EDIT", false);
                startActivity(intent);
                break;
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
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

    /*It initialize all views in actionbar.*/
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

    /*Get the the intent from download service after file is completely donloaded*/
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

    /*Register reciver*/
    private void registerReceiver() {

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }


}
