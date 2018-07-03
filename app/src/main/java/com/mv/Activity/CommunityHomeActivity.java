package com.mv.Activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.ContentAdapter;
import com.mv.Model.Community;
import com.mv.Model.Content;
import com.mv.Model.Download;
import com.mv.Model.Template;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.EndlessRecyclerViewScrollListener;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityCommunityHomeBinding;

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

public class CommunityHomeActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {


    private ImageView img_back, img_list, img_logout, img_filter;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private FloatingActionButton fab_add_list;
    private PreferenceHelper preferenceHelper;
    private List<Content> chatList = new ArrayList<Content>();
    private List<Content> mypostlist = new ArrayList<>();
    private List<Content> mylocationlist = new ArrayList<>();
    private List<Content> otherlocationlist = new ArrayList<>();

    private ContentAdapter adapter;
    private ActivityCommunityHomeBinding binding;
    public List<Community> communityList = new ArrayList<>();
    private String type = "";
    private int position;
    private List<Template> templateList = new ArrayList<>();
    private ArrayList<Content> filterlist = new ArrayList<>();
    RecyclerView recyclerView;
    Button btn_mypost, btn_allposts, btn_mylocation, btn_otherlcation;
    LinearLayout lnr_filter;
    private boolean filter = false;
    public String json;
    public String HoSupportCommunity = "";
    private Boolean mySelection = false, myLocation = false;
    int filterflag = 0;
    TextView textNoData;
    public static final String MESSAGE_PROGRESS = "message_progress";
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setContentView(R.layout.activity_community_home);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_community_home);
        binding.setActivity(this);
        initViews();
        registerReceiver(); //
        binding.swipeRefreshLayout.setOnRefreshListener(this);
        adapter = new ContentAdapter(this, chatList);
        recyclerView.setAdapter(adapter);
        getChats(true);
    }

    /*For setting differnt languages like english, marathi*/
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    /*Get the Chat List from Database and set to the adapter , if No vales in table then get Chats from Server*/
    private void getChats(boolean isDialogShow) {
        List<Content> temp = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);
        if (temp.size() == 0) {
            if (Utills.isConnected(this))
                /*Api Call if  internet is available */
                getAllChats(false, isDialogShow, false);
            else
                showPopUp();
        } else {
            chatList.clear();
            for (int i = 0; i < temp.size(); i++) {
                chatList.add(temp.get(i));
            }
            adapter.notifyDataSetChanged();
            if (Utills.isConnected(this))
                getAllChats(true, isDialogShow, false);
        }
    }

    /*Api Call of getChatContentNew */
    private void getAllChats(boolean isTimePresent, boolean isDialogShow, boolean isPrevious) {
        if (isDialogShow)
            Utills.showProgressDialog(this, getString(R.string.loading_chats), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = "";
        if (isTimePresent) {
            if (!isPrevious) {
                url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + "/services/apexrest/getChatContentNewLates?CommunityId=" + preferenceHelper.getString(PreferenceHelper.COMMUNITYID)
                        + "&userId=" + User.getCurrentUser(this).getMvUser().getId() + "&timestamp=" + AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID)).get(0).getTime()
                        + "&isPrevious=false";
            } else {
                url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + "/services/apexrest/getChatContentNewLates?CommunityId=" + preferenceHelper.getString(PreferenceHelper.COMMUNITYID)
                        + "&userId=" + User.getCurrentUser(this).getMvUser().getId() + "&timestamp=" + AppDatabase.getAppDatabase(getApplicationContext()).userDao().getLastChatTime(preferenceHelper.getString(PreferenceHelper.COMMUNITYID))
                        + "&isPrevious=true";
            }
        } else {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getChatContentNewLates?CommunityId=" + preferenceHelper.getString(PreferenceHelper.COMMUNITYID) + "&userId=" + User.getCurrentUser(this).getMvUser().getId();
        }
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            List<Community> list = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
                            Community community = new Community();
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).getId().equalsIgnoreCase(preferenceHelper.getString(PreferenceHelper.COMMUNITYID))) {
                                    community = list.get(i);
                                    break;
                                }
                            }
                            if (community != null) {
                                community.setCount("" + 0);
                                AppDatabase.getAppDatabase(getApplicationContext()).userDao().updateCommunities(community);
                            }
                            JSONArray jsonArray = new JSONArray(data);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            List<Content> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Content[].class));
                            List<Content> contentList = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);
                            if ((temp.size() != 0) || (contentList.size() != 0)) {
                                for (int i = 0; i < temp.size(); i++) {
                                  /*  if (temp.get(i).getErrorMsg().equalsIgnoreCase("User is Inactive")) {
                                        AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().clearTableCommunity();
                                        AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().clearTableCotent();
                                        AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().clearProcessTable();
                                        AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().clearTaskContainer();
                                        AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().clearLocation();
                                        User.clearUser();

                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                        break;
                                    }*/
                                    int j;
                                    boolean isPresent = false;
                                    for (j = 0; j < contentList.size(); j++) {
                                        if ((contentList.get(j).getId() != null) &&
                                                (contentList.get(j).getId().equalsIgnoreCase(temp.get(i).getId()))) {
                                            temp.get(i).setUnique_Id(contentList.get(j).getUnique_Id());
                                            temp.get(i).setCommunity_id(preferenceHelper.getString(PreferenceHelper.COMMUNITYID));
                                            isPresent = true;
                                            break;
                                        }
                                    }
                                    if (isPresent) {
                                        AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().updateContent(temp.get(i));
                                    } else {
                                        temp.get(i).setCommunity_id(preferenceHelper.getString(PreferenceHelper.COMMUNITYID));
                                        AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().insertChats(temp.get(i));
                                    }
                                }
                                AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().deletepost(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), false, true);

                                if (filterflag == 0) {
                                    allPost();
                                } else if (filterflag == 1) {
                                    myPost();
                                } else if (filterflag == 2) {
                                    myLocation();
                                } else if (filterflag == 3) {
                                    otherLocation();
                                }
                                textNoData.setVisibility(View.GONE);
                            } else {
                                textNoData.setVisibility(View.VISIBLE);
                            }
                        }
                        //adapter.notifyDataSetChanged();
                    }
                    Utills.hideProgressDialog();
                    binding.swipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    Utills.hideProgressDialog();
                    binding.swipeRefreshLayout.setRefreshing(false);
                    e.printStackTrace();
                } catch (IOException e) {
                    Utills.hideProgressDialog();
                    binding.swipeRefreshLayout.setRefreshing(false);
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

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null && adapter.popup != null)
            adapter.popup.dismiss();
    }

    /*Popup For Checking Internet Connection*/
    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle(getString(R.string.app_name));

        alertDialog.setMessage(getString(R.string.error_no_internet));

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

    /*Initialize all views and Set the filter on particular button click*/
    private void initViews() {
        setActionbar(getIntent().getExtras().getString(Constants.TITLE));
        json = getIntent().getExtras().getString(Constants.LIST);
        HoSupportCommunity = (getIntent().getExtras().getString(Constants.TITLE));
        final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        communityList = Arrays.asList(gson.fromJson(json, Community[].class));
        preferenceHelper = new PreferenceHelper(this);
        fab_add_list = (FloatingActionButton) findViewById(R.id.fab_add_list);
        textNoData = (TextView) findViewById(R.id.textNoData);
        fab_add_list.setOnClickListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        btn_allposts = (Button) findViewById(R.id.btn_allposts);
        btn_mypost = (Button) findViewById(R.id.btn_mypost);
        btn_mylocation = (Button) findViewById(R.id.btn_mylocation);
        btn_otherlcation = (Button) findViewById(R.id.btn_otherlocation);
        lnr_filter = (LinearLayout) findViewById(R.id.lnr_filter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        /*Change the visiblity of filter button on scroll*/
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (getIntent().getExtras().getString(Constants.TITLE).equalsIgnoreCase("HO Support")) {
                    binding.lnrFilter.setVisibility(View.GONE);
                } else {
                    if (dy < -5 && ((lnr_filter.getVisibility() == View.GONE))) {
                        lnr_filter.setVisibility(View.VISIBLE);
                    } else if (dy > 5 && (lnr_filter.getVisibility() == View.VISIBLE)) {
                        lnr_filter.setVisibility(View.GONE);
                    }
                }


            }
        });
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.i("page", "" + page);
                Log.i("totalItemsCount", "" + totalItemsCount);
                getAllChats(true, true, true);
                //loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);
        /*Display the post of only registered users */
        btn_mypost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPost();
            }
        });

        /*Display the posts of all users*/
        btn_allposts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allPost();
            }
        });

       /*Display posts of register user's taluka*/
        btn_mylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myLocation();
            }
        });

        /*Displays the posts other than user taluka */

        btn_otherlcation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherLocation();
            }
        });
        if (getIntent().getExtras().getString(Constants.TITLE).equalsIgnoreCase("HO Support")) {
            binding.lnrFilter.setVisibility(View.GONE);
        } else {
            binding.lnrFilter.setVisibility(View.VISIBLE);
        }
    }

    private void myPost() {
        mySelection = true;
        filterflag = 1;
        btn_mypost.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
        btn_allposts.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_mylocation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_otherlcation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        mypostlist = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);
        chatList.clear();
        for (int i = 0; i < mypostlist.size(); i++) {
            if (mypostlist.get(i).getUser_id() != null && (mypostlist.get(i).getUser_id().equals(User.getCurrentUser(getApplicationContext()).getMvUser().getId()))) {
                chatList.add(mypostlist.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void allPost() {
        btn_allposts.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
        btn_mypost.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_mylocation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_otherlcation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        mySelection = false;
        filterflag = 0;
        mypostlist = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);
        chatList.clear();
        for (int i = 0; i < mypostlist.size(); i++) {
            chatList.add(mypostlist.get(i));
        }
        adapter.notifyDataSetChanged();
    }

    private void myLocation() {
        btn_mylocation.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
        btn_allposts.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_mypost.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_otherlcation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        myLocation = true;
        filterflag = 2;
        chatList.clear();
        mylocationlist = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);
        for (int i = 0; i < mylocationlist.size(); i++) {
            if (mylocationlist.get(i).getTaluka() != null) {
                if (mylocationlist.get(i).getTaluka().equalsIgnoreCase(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka())) {
                    chatList.add(mylocationlist.get(i));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void otherLocation() {
        btn_otherlcation.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
        btn_allposts.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_mylocation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_mypost.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        myLocation = false;
        filterflag = 3;
        chatList.clear();
        otherlocationlist = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);
        for (int i = 0; i < otherlocationlist.size(); i++) {
            if (otherlocationlist.get(i).getTaluka() != null) {
                if (!otherlocationlist.get(i).getTaluka().equals(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka())) {
                    chatList.add(otherlocationlist.get(i));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    /*Initialize views in actionbar and set ToolBar title*/
    private void setActionbar(final String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.GONE);
        img_list.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setImageResource(R.drawable.group);
        img_logout.setVisibility(View.VISIBLE);
        img_logout.setOnClickListener(this);
        img_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CommunityMemberNameActivity.class);
                startActivity(intent);
            }
        });
        img_filter = (ImageView) findViewById(R.id.img_filter);
        if (Title != null) {
            if (Title.equalsIgnoreCase("HO Support")) {
                img_logout.setVisibility(View.GONE);
            } else {
                img_logout.setVisibility(View.VISIBLE);
            }
        }

        img_filter.setVisibility(View.VISIBLE);
        img_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = true;
                if (Title.equalsIgnoreCase("HO Support")) {
                    HoSupportFilter();
                } else {
                    OtherFilter();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.fab_add_list:
               /* if (Utills.isConnected(this)) {
                    getAllTemplates();
                } else {
                    if (TextUtils.isEmpty(preferenceHelper.getString(Constants.TEMPLATES))) {
                        showPopUp();
                    } else {
                        try {
                            JSONArray jsonArray = new JSONArray(preferenceHelper.getString(Constants.TEMPLATES));
                            templateList.clear();
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            List<Template> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Template[].class));
                            for (int i = 0; i < temp.size(); i++) {
                                templateList.add(temp.get(i));
                            }
                            showDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }*/
                if (getIntent().getExtras().getString(Constants.TITLE).equalsIgnoreCase("HO Support")) {
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Issue");
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, Constants.ISSUEID);
                    intent = new Intent(CommunityHomeActivity.this, IssueTemplateActivity.class);
                    intent.putExtra("EDIT", false);
                    startActivity(intent);
                } else {
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Report");
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, Constants.REPORTID);
                    intent = new Intent(CommunityHomeActivity.this, ReportingTemplateActivity.class);
                    intent.putExtra("EDIT", false);
                    startActivity(intent);
                }

                break;


        }
    }

    private void getAllTemplates() {
        Utills.showProgressDialog(this, getString(R.string.loading_template), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/MV_GeTemplates_c";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    String strResponse = response.body().string();
                    JSONArray jsonArray = new JSONArray(strResponse);
                    preferenceHelper.insertString(Constants.TEMPLATES, strResponse);
                    templateList.clear();
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    List<Template> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Template[].class));
                    for (int i = 0; i < temp.size(); i++) {
                        templateList.add(temp.get(i));
                    }
                    showDialog();
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

    private void showDialog() {
        final String[] items = new String[templateList.size()];
        for (int i = 0; i < templateList.size(); i++) {

            if (i == 0)
                type = templateList.get(i).getName();
            items[i] = templateList.get(i).getName();
            if (getIntent().getExtras().getString(Constants.TITLE).equalsIgnoreCase("HO Support")) {

            }

        }

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select template type")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        type = items[i];
                        position = i;
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent;
                        if (TextUtils.isEmpty(type)) {
                            Utills.showToast(getString(R.string.select_temp), CommunityHomeActivity.this);
                        } else if (type.equalsIgnoreCase(Constants.TEMPLATE_REPORT)) {
                            preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, templateList.get(position).getName());
                            preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, templateList.get(position).getId());
                            intent = new Intent(CommunityHomeActivity.this, ReportingTemplateActivity.class);
                            startActivity(intent);
                        } else if (type.equalsIgnoreCase(Constants.TEMPLATE_ISSUE)) {
                            preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, templateList.get(position).getName());
                            preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, templateList.get(position).getId());
                            intent = new Intent(CommunityHomeActivity.this, IssueTemplateActivity.class);
                            startActivity(intent);
                        }

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        if (Utills.isConnected(this)) {
            binding.swipeRefreshLayout.setRefreshing(false);
            getAllChats(true, false, false);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Utills.isConnected(this)) {
            getAllChats(true, true, false);
        }
    }

    /*Show Filter popup for Ho Support*/
    private void HoSupportFilter() {
        AlertDialog.Builder b = new AlertDialog.Builder(CommunityHomeActivity.this);
        String title = getIntent().getExtras().getString(Constants.TITLE);

        b.setItems(R.array.array_of_issue, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {

                dialog.dismiss();
                switch (position) {
                    case 1:

                        setFilter("Training related");
                        break;

                    case 2:
                        setFilter("Content related");
                        break;

                    case 3:
                        setFilter("Technology related");
                        break;

                    case 4:
                        setFilter("HR related");
                        break;

                    case 5:
                        setFilter("Account related");
                        break;
                }
            }

        });

        b.show();
    }

    /*It shows the filter dialog for all communities except Ho support */
    private void OtherFilter() {
        AlertDialog.Builder b = new AlertDialog.Builder(CommunityHomeActivity.this);
        String title = getIntent().getExtras().getString(Constants.TITLE);
        b.setItems(R.array.array_of_reporting_type, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {

                dialog.dismiss();
                switch (position) {
                    case 1:

                        setFilter("Information Sharing");


                        break;
                    case 2:
                        setFilter("Events Update");
                        break;
                    case 3:
                        setFilter("Success Stories");
                        break;

                    case 4:
                        setFilter("Press Cuttings");
                        break;
                }
            }

        });
        b.show();
    }

    /*Set the filterlist to adapter according to respective filter parameter along with report type and issue type */
    private void setFilter(String filtertype) {
        chatList.clear();
        if (filterflag == 0) {
            List<Content> contentList = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), filtertype);
            for (int i = 0; i < contentList.size(); i++) {
                chatList.add(contentList.get(i));
            }
        } else if (filterflag == 1) {
            List<Content> contentList = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getMyChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), User.getCurrentUser(getApplicationContext()).getMvUser().getId(), filtertype);
            for (int i = 0; i < contentList.size(); i++) {
                chatList.add(contentList.get(i));
            }
        } else if (filterflag == 2) {
            List<Content> contentList = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getMyLocationChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), filtertype, User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());
            for (int i = 0; i < contentList.size(); i++) {
                chatList.add(contentList.get(i));
            }
        } else if (filterflag == 3) {
            List<Content> contentList = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().getOtherChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), filtertype, User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());
            for (int i = 0; i < contentList.size(); i++) {
                chatList.add(contentList.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }

    /*Get the the intent from download service for checking file is completely donloaded or not*/
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

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(CommunityHomeActivity.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }
}

