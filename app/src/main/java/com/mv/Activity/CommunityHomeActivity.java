package com.mv.Activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.ContentAdapter;
import com.mv.BuildConfig;
import com.mv.Model.Community;
import com.mv.Model.Content;
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

public class CommunityHomeActivity extends AppCompatActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private ImageView img_more;
    private TextView textNoData;
    private LinearLayout lnr_filter;
    private PopupMenu popup;
    private Button btn_myPost, btn_allPosts, btn_myLocation, btn_otherLocation;
    private PreferenceHelper preferenceHelper;

    private List<Content> chatList = new ArrayList<>();
    private List<Content> myPostList = new ArrayList<>();
    public List<Community> communityList = new ArrayList<>();

    private ContentAdapter adapter;
    private RecyclerView recyclerView;
    private ActivityCommunityHomeBinding binding;

    private int filterFlag = 0;
    private String sortString = "";

    public String json;
    public String hoSupportCommunity = "";
    public static final String MESSAGE_PROGRESS = "message_progress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setContentView(R.layout.activity_community_home);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_community_home);
        binding.setActivity(this);

        initViews();
        registerReceiver();

        binding.swipeRefreshLayout.setOnRefreshListener(this);
        adapter = new ContentAdapter(this, chatList);
        recyclerView.setAdapter(adapter);
        getChats();
    }

    /*For setting different languages like english, marathi*/
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    /*Get the Chat List from Database and set to the adapter , if No vales in table then get Chats from Server*/
    private void getChats() {
        List<Content> temp = AppDatabase.getAppDatabase(getApplicationContext())
                .userDao().getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),
                        true, false);

        if (temp.size() == 0) {
            if (Utills.isConnected(this)) {
                /*Api Call if  internet is available */
                getAllChats(false, true, false);
            } else {
                showPopUp();
            }
        } else {
            chatList.clear();
            chatList.addAll(temp);
            adapter.notifyDataSetChanged();

            if (Utills.isConnected(this)) {
                getAllChats(true, true, false);
            }
        }
    }

    /*Api Call of getChatContentNew */
    private void getAllChats(boolean isTimePresent, boolean isDialogShow, boolean isPrevious) {
        if (isDialogShow) {
            Utills.showProgressDialog(this, getString(R.string.loading_chats), getString(R.string.progress_please_wait));
        }

        String url;
        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        if (isTimePresent && AppDatabase.getAppDatabase(getApplicationContext()).userDao()
                .getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID)).size() > 0) {

            if (!isPrevious) {
                url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + "/services/apexrest/getChatContentNewLates?CommunityId="
                        + preferenceHelper.getString(PreferenceHelper.COMMUNITYID) + "&userId="
                        + User.getCurrentUser(this).getMvUser().getId() + "&timestamp="
                        + AppDatabase.getAppDatabase(getApplicationContext()).userDao()
                        .getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID))
                        .get(0).getTime() + "&isPrevious=false";
            } else {
                url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                        + "/services/apexrest/getChatContentNewLates?CommunityId="
                        + preferenceHelper.getString(PreferenceHelper.COMMUNITYID) + "&userId="
                        + User.getCurrentUser(this).getMvUser().getId() + "&timestamp="
                        + AppDatabase.getAppDatabase(getApplicationContext()).userDao()
                        .getLastChatTime(preferenceHelper.getString(PreferenceHelper.COMMUNITYID)) + "&isPrevious=true";
            }
        } else {
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/getChatContentNewLates?CommunityId="
                    + preferenceHelper.getString(PreferenceHelper.COMMUNITYID) + "&userId="
                    + User.getCurrentUser(this).getMvUser().getId();
        }

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
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
                            List<Content> contentList = AppDatabase.getAppDatabase(
                                    getApplicationContext()).userDao().getAllChats(
                                    preferenceHelper.getString(PreferenceHelper.COMMUNITYID),
                                    true, false);

                            if ((temp.size() != 0) || (contentList.size() != 0)) {
                                for (int i = 0; i < temp.size(); i++) {
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

                                AppDatabase.getAppDatabase(CommunityHomeActivity.this)
                                        .userDao().deletepost(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),
                                        false, true);

                                setFilter(sortString);
                                textNoData.setVisibility(View.GONE);
                            } else {
                                textNoData.setVisibility(View.VISIBLE);
                            }
                        }
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

    /*Api Call of get my post only */
    private void getMyChats() {
        Utills.showProgressDialog(this, getString(R.string.loading_chats), getString(R.string.progress_please_wait));

        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getChatContentMyPosts?CommunityId="
                + preferenceHelper.getString(PreferenceHelper.COMMUNITYID) + "&userId="
                + User.getCurrentUser(this).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data.length() > 0) {
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
                            List<Content> contentList = AppDatabase.getAppDatabase(
                                    getApplicationContext()).userDao().getAllChats(preferenceHelper.getString(
                                    PreferenceHelper.COMMUNITYID), true, false);

                            if ((temp.size() != 0) || (contentList.size() != 0)) {
                                for (int i = 0; i < temp.size(); i++) {
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

                                AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao()
                                        .deletepost(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),
                                                false, true);

                                setFilter(sortString);
                                textNoData.setVisibility(View.GONE);
                            } else {
                                textNoData.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    myPost();
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
    @SuppressWarnings("deprecation")
    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.app_name));
        alertDialog.setMessage(getString(R.string.error_no_internet));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), (dialog, which) -> {
            alertDialog.dismiss();
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        // Showing Alert Message
        alertDialog.show();
    }

    /*Initialize all views and Set the filter on particular button click*/
    private void initViews() {
        String title = "";
        if (getIntent().getExtras() != null) {
            title = getIntent().getExtras().getString(Constants.TITLE);
            json = getIntent().getExtras().getString(Constants.LIST);
            hoSupportCommunity = (getIntent().getExtras().getString(Constants.TITLE));
        }
        setActionbar(title);

        final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        communityList = Arrays.asList(gson.fromJson(json, Community[].class));

        preferenceHelper = new PreferenceHelper(this);

        textNoData = (TextView) findViewById(R.id.textNoData);
        btn_allPosts = (Button) findViewById(R.id.btn_allposts);
        btn_myPost = (Button) findViewById(R.id.btn_mypost);
        btn_myLocation = (Button) findViewById(R.id.btn_mylocation);
        btn_otherLocation = (Button) findViewById(R.id.btn_otherlocation);
        lnr_filter = (LinearLayout) findViewById(R.id.lnr_filter);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        //check if user can post or not
        FloatingActionButton fab_add_list = (FloatingActionButton) findViewById(R.id.fab_add_list);
        fab_add_list.setOnClickListener(this);

        boolean canPost = getIntent().getBooleanExtra("CanPost", false);
        if (canPost) {
            fab_add_list.setVisibility(View.VISIBLE);
        } else {
            fab_add_list.setVisibility(View.GONE);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        /*Change the visibility of filter button on scroll*/
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (getIntent().getExtras().getString(Constants.TITLE) != null) {
                    if ("HO Support".equalsIgnoreCase(getIntent().getExtras().getString(Constants.TITLE))) {
                        binding.lnrFilter.setVisibility(View.GONE);
                    } else {
                        if (dy < -5 && ((lnr_filter.getVisibility() == View.GONE))) {
                            lnr_filter.setVisibility(View.VISIBLE);
                        } else if (dy > 5 && (lnr_filter.getVisibility() == View.VISIBLE)) {
                            lnr_filter.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (filterFlag == 0) {
                    getAllChats(true, true, true);
                }
            }
        };

        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);

        /*Display the post of only registered users */
        btn_myPost.setOnClickListener(v -> {
            btn_myPost.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
            btn_allPosts.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
            btn_myLocation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
            btn_otherLocation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));

            /*Api Call if  internet is available */
            if (Utills.isConnected(CommunityHomeActivity.this)) {
                getMyChats();
            } else {
                myPost();
            }
        });

        /*Display the posts of all users*/
        btn_allPosts.setOnClickListener(v -> {
            btn_allPosts.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
            btn_myPost.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
            btn_myLocation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
            btn_otherLocation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
            allPost();
        });

        /*Display posts of register user's taluka*/
        btn_myLocation.setOnClickListener(v -> myLocation());

        /*Displays the posts other than user taluka */
        btn_otherLocation.setOnClickListener(v -> otherLocation());

        binding.lnrFilter.setVisibility(View.VISIBLE);

        if (getIntent().getExtras() != null) {
            if ("HO Support".equalsIgnoreCase(getIntent().getExtras().getString(Constants.TITLE))) {
                binding.lnrFilter.setVisibility(View.GONE);
            }
        }
    }

    private void myPost() {
        filterFlag = 1;
        myPostList = AppDatabase.getAppDatabase(getApplicationContext()).userDao()
                .getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);

        chatList.clear();
        for (int i = 0; i < myPostList.size(); i++) {
            if (myPostList.get(i).getUser_id() != null && (myPostList.get(i).getUser_id()
                    .equals(User.getCurrentUser(getApplicationContext()).getMvUser().getId()))) {
                chatList.add(myPostList.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void allPost() {
        filterFlag = 0;
        myPostList = AppDatabase.getAppDatabase(getApplicationContext()).userDao()
                .getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);

        chatList.clear();
        chatList.addAll(myPostList);
        adapter.notifyDataSetChanged();
    }

    private void myLocation() {
        btn_myLocation.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
        btn_allPosts.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_myPost.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_otherLocation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));

        filterFlag = 2;
        chatList.clear();

        List<Content> myLocationList = AppDatabase.getAppDatabase(getApplicationContext()).userDao()
                .getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);

        for (int i = 0; i < myLocationList.size(); i++) {
            if (myLocationList.get(i).getTaluka() != null) {
                if (myLocationList.get(i).getTaluka().equalsIgnoreCase(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka())) {
                    chatList.add(myLocationList.get(i));
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void otherLocation() {
        btn_otherLocation.setBackground(getResources().getDrawable(R.drawable.selected_btn_background));
        btn_allPosts.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_myLocation.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));
        btn_myPost.setBackground(getResources().getDrawable(R.drawable.light_grey_btn_background));

        filterFlag = 3;
        chatList.clear();
        List<Content> otherLocationList = AppDatabase.getAppDatabase(getApplicationContext()).userDao()
                .getAllChats(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), true, false);

        for (int i = 0; i < otherLocationList.size(); i++) {
            if (otherLocationList.get(i).getTaluka() != null) {
                if (!otherLocationList.get(i).getTaluka().equals(User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka())) {
                    chatList.add(otherLocationList.get(i));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    /*Initialize views in actionbar and set ToolBar title*/
    private void setActionbar(final String title) {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(title);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);

        //****Changes for the mute notification****//
        img_more = (ImageView) findViewById(R.id.img_more);
        img_more.setVisibility(View.VISIBLE);

        img_more.setOnClickListener(v -> {
            popup = new PopupMenu(CommunityHomeActivity.this, img_more);
            popup.getMenuInflater().inflate(R.menu.popup_menu_cammunity, popup.getMenu());

            MenuItem group = popup.getMenu().findItem(R.id.mn_mumbers);
            MenuItem mute = popup.getMenu().findItem(R.id.mn_mute);

            if (title != null) {
                if (title.equalsIgnoreCase("HO Support")) {
                    group.setVisible(false);
                } else {
                    group.setVisible(true);
                }
            }

            Community community = AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao()
                    .getCommunityForMute(preferenceHelper.getString(PreferenceHelper.COMMUNITYID));

            if (community.getMuteNotification() != null || community.getMuteNotification().equals("Mute")) {
                mute.setTitle("Mute");
            } else {
                mute.setTitle("Unmute");
            }

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.mn_filter:
                        if (title != null && title.equalsIgnoreCase("HO Support")) {
                            HoSupportFilter();
                        } else {
                            OtherFilter();
                        }
                        break;

                    case R.id.mn_mumbers:
                        Intent intent = new Intent(getApplicationContext(), CommunityMemberNameActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.mn_mute:
                        if (community.getMuteNotification() == null || community.getMuteNotification().equals("Mute")) {
                            community.setMuteNotification("Unmute");
                            AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().updateCommunityForMute(community);
                            Toast.makeText(CommunityHomeActivity.this, "You have select Mute for this community.", Toast.LENGTH_LONG).show();
                        } else {
                            community.setMuteNotification("Mute");
                            Toast.makeText(CommunityHomeActivity.this, "You have select Unmute this community.", Toast.LENGTH_LONG).show();
                            AppDatabase.getAppDatabase(CommunityHomeActivity.this).userDao().updateCommunityForMute(community);
                        }
                        break;
                }
                return true;
            });
            popup.show();
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
                if (getIntent().getExtras() != null &&
                        "HO Support".equalsIgnoreCase(getIntent().getExtras().getString(Constants.TITLE))) {
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Issue");
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, BuildConfig.ISSUEID);
                    intent = new Intent(CommunityHomeActivity.this, IssueTemplateActivity.class);
                    intent.putExtra("EDIT", false);
                    startActivity(intent);
                } else {
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Report");
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, BuildConfig.REPORTID);
                    intent = new Intent(CommunityHomeActivity.this, ReportingTemplateActivity.class);
                    intent.putExtra("EDIT", false);
                    startActivity(intent);
                }
                break;
        }
    }

//    private void getAllTemplates() {
//        Utills.showProgressDialog(this, getString(R.string.loading_template), getString(R.string.progress_please_wait));
//        ServiceRequest apiService = ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
//        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/MV_GeTemplates_c";
//
//        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
//
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Utills.hideProgressDialog();
//                try {
//                    String strResponse = response.body().string();
//                    JSONArray jsonArray = new JSONArray(strResponse);
//                    preferenceHelper.insertString(Constants.TEMPLATES, strResponse);
//                    templateList.clear();
//
//                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
//                    List<Template> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Template[].class));
//                    templateList.addAll(temp);
//                    showDialog();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Utills.hideProgressDialog();
//            }
//        });
//    }

//    private void showDialog() {
//        final String[] items = new String[templateList.size()];
//        for (int i = 0; i < templateList.size(); i++) {
//
//            if (i == 0)
//                type = templateList.get(i).getName();
//            items[i] = templateList.get(i).getName();
//        }
//
//        // arraylist to keep the selected items
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle("Select template type")
//                .setSingleChoiceItems(items, 0, (dialogInterface, i) -> {
//                    type = items[i];
//                    position = i;
//                })
//                .setPositiveButton(getString(R.string.ok), (dialog1, id) -> {
//                    Intent intent;
//                    if (TextUtils.isEmpty(type)) {
//                        Utills.showToast(getString(R.string.select_temp), CommunityHomeActivity.this);
//                    } else if (type.equalsIgnoreCase(Constants.TEMPLATE_REPORT)) {
//                        preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, templateList.get(position).getName());
//                        preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, templateList.get(position).getId());
//                        intent = new Intent(CommunityHomeActivity.this, ReportingTemplateActivity.class);
//                        startActivity(intent);
//                    } else if (type.equalsIgnoreCase(Constants.TEMPLATE_ISSUE)) {
//                        preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, templateList.get(position).getName());
//                        preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, templateList.get(position).getId());
//                        intent = new Intent(CommunityHomeActivity.this, IssueTemplateActivity.class);
//                        startActivity(intent);
//                    }
//
//                }).setNegativeButton(getString(R.string.cancel), (dialog12, id) -> {
//                    //  Your code when user clicked on Cancel
//                }).create();
//        dialog.show();
//    }

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
            if (filterFlag == 0) {
                getAllChats(true, false, false);
            }
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
        b.setItems(R.array.array_of_issue, (dialog, position) -> {
            dialog.dismiss();

            switch (position) {
                case 1:
                    sortString = "Training related";
                    setFilter(sortString);
                    break;

                case 2:
                    sortString = "Content related";
                    setFilter(sortString);
                    break;

                case 3:
                    sortString = "Technology related";
                    setFilter(sortString);
                    break;

                case 4:
                    sortString = "HR related";
                    setFilter(sortString);
                    break;

                case 5:
                    sortString = "Account related";
                    setFilter(sortString);
                    break;
            }
        });
        b.show();
    }

    /*It shows the filter dialog for all communities except Ho support */
    private void OtherFilter() {
        AlertDialog.Builder b = new AlertDialog.Builder(CommunityHomeActivity.this);
        b.setItems(R.array.array_of_reporting_type, (dialog, position) -> {
            dialog.dismiss();

            switch (position) {
                case 1:
                    sortString = "Information Sharing";
                    setFilter(sortString);
                    break;

                case 2:
                    sortString = "Events Update";
                    setFilter(sortString);
                    break;

                case 3:
                    sortString = "Success Stories";
                    setFilter(sortString);
                    break;

                case 4:
                    sortString = "Press Cuttings";
                    setFilter(sortString);
                    break;
            }
        });
        b.show();
    }

    /*Set the filterList to adapter according to respective filter parameter along with report type and issue type */
    private void setFilter(String filterType) {
        chatList.clear();
        if (filterType == null || filterType.length() == 0) {
            allPost();
        } else {
            switch (filterFlag) {
                case 0: {
                    List<Content> contentList = AppDatabase.getAppDatabase(CommunityHomeActivity.this)
                            .userDao().getAllChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID), filterType);
                    chatList.addAll(contentList);
                    break;
                }

                case 1: {
                    List<Content> contentList = AppDatabase.getAppDatabase(CommunityHomeActivity.this)
                            .userDao().getMyChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),
                                    User.getCurrentUser(getApplicationContext()).getMvUser().getId(), filterType);
                    chatList.addAll(contentList);
                    break;
                }

                case 2: {
                    List<Content> contentList = AppDatabase.getAppDatabase(CommunityHomeActivity.this)
                            .userDao().getMyLocationChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),
                                    filterType, User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());
                    chatList.addAll(contentList);
                    break;
                }

                case 3: {
                    List<Content> contentList = AppDatabase.getAppDatabase(CommunityHomeActivity.this)
                            .userDao().getOtherChatsfilter(preferenceHelper.getString(PreferenceHelper.COMMUNITYID),
                                    filterType, User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka());
                    chatList.addAll(contentList);
                    break;
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    /*Get the the intent from download service for checking file is completely downloaded or not*/
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(MESSAGE_PROGRESS)) {
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        }
    };

    /*Register receiver*/
    private void registerReceiver() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(CommunityHomeActivity.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }
}