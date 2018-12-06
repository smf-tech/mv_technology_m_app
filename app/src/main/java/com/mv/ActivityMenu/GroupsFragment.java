package com.mv.ActivityMenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Activity.CommunityHomeActivity;
import com.mv.Activity.IssueTemplateActivity;
import com.mv.Activity.ReportingTemplateActivity;
import com.mv.Adapter.GroupAdapter;
import com.mv.BuildConfig;
import com.mv.Model.Community;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityGroupBinding;

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


public class GroupsFragment extends AppCompatActivity implements View.OnClickListener {
    private GroupAdapter mAdapter;
    private ActivityGroupBinding binding;
    private List<Community> communityList = new ArrayList<>();
    private List<Community> replicaCommunityList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
    private TextView textNoData;
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group);
        binding.setFragment(this);
        //here data must be an instance of the class MarsDataProvider
        Utills.setupUI(findViewById(R.id.layout_main), context);
        initViews();
        getCommunities(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setRecyclerView() {
        List<Community> temp = AppDatabase.getAppDatabase(context).userDao().getAllCommunities();
        communityList.clear();
        replicaCommunityList.clear();
        for (int i = 0; i < temp.size(); i++) {
            communityList.add(temp.get(i));
            replicaCommunityList.add(temp.get(i));
            mAdapter.notifyDataSetChanged();
        }
    }
    private void getCommunities(boolean isDialogShow) {
        List<Community> temp = AppDatabase.getAppDatabase(context).userDao().getAllCommunities();
        if (temp.size() == 0) {
            if (Utills.isConnected(context))
                getAllCommunities(false, isDialogShow);
            else
                showPopUp();
        } else {
            communityList.clear();
            replicaCommunityList.clear();
            for (int i = 0; i < temp.size(); i++) {
                communityList.add(temp.get(i));
                replicaCommunityList.add(temp.get(i));
            }
            mAdapter.notifyDataSetChanged();
            if (Utills.isConnected(context))
                getAllCommunities(true, isDialogShow);
        }
    }


    private void initViews() {
        Intent receivedIntent = getIntent();
        setActionbar(getString(R.string.community));
        textNoData = (TextView) findViewById(R.id.textNoData);
        binding.editTextEmail.addTextChangedListener(watch);
        binding.swiperefresh.setOnRefreshListener(
                () -> getCommunities(false)
        );
        preferenceHelper = new PreferenceHelper(context);
        mAdapter = new GroupAdapter(communityList, context, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        String receivedAction = receivedIntent.getAction();
        String receivedType = receivedIntent.getType();
        //make sure it's an action and type we can handle
        if (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND)) {
             if (receivedType!= null && receivedType.startsWith("image/")) {
                //handle sent image
                Constants.shareUri = receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            }
//            else if (receivedType.startsWith("text/")) {
//                //handle sent text
//            }
            //content is being shared
        } else {
            //app has been launched directly, not from share list
            Constants.shareUri = null;
        }
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

    private void getAllCommunities(boolean isTimePresent, boolean isDialogShow) {
        if (isDialogShow) {
            Utills.showProgressDialog(context, "Loading Communities", getString(R.string.progress_please_wait));
        }

        ServiceRequest apiService = ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/MV_GetCommunities_c?userId=" + User.getCurrentUser(context).getMvUser().getId();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            List<Community> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Community[].class));
                            List<Community> list = AppDatabase.getAppDatabase(context).userDao().getAllCommunities();
                            if ((temp.size() != 0) || (list.size() != 0)) {
                                AppDatabase.getAppDatabase(context).userDao().clearTableCommunity();
                                for (int i = 0; i < temp.size(); i++) {
                                   /* if (temp.get(i).getErrorMsg().equalsIgnoreCase("User is Inactive")) {
                                        AppDatabase.getAppDatabase(context).userDao().clearTableCommunity();
                                        AppDatabase.getAppDatabase(context).userDao().clearTableCotent();
                                        AppDatabase.getAppDatabase(context).userDao().clearProcessTable();
                                        AppDatabase.getAppDatabase(context).userDao().clearTaskContainer();
                                        AppDatabase.getAppDatabase(context).userDao().clearLocation();
                                        User.clearUser();
                                        binding.recyclerView.setAdapter(null);
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                        break;
                                    }*/
//                                    int j;
//                                    boolean isPresent = false;
//                                    for (j = 0; j < list.size(); j++) {
//                                        if (list.get(j).getId()!=null&&list.get(j).getId().equalsIgnoreCase(temp.get(i).getId())) {
//                                            temp.get(i).setUnique_Id(list.get(j).getUnique_Id());
//                                            temp.get(i).setMuteNotification(list.get(j).getMuteNotification());
//                                            isPresent = true;
//                                            break;
//                                        }
//                                    }
//
//                                    if (isPresent) {
//                                        communityList.set(j, temp.get(i));
//                                        replicaCommunityList.set(j, temp.get(i));
//                                        AppDatabase.getAppDatabase(context).userDao().updateCommunities(temp.get(i));
//                                    } else {
//                                        communityList.add(temp.get(i));
//                                        replicaCommunityList.add(temp.get(i));
//                                        AppDatabase.getAppDatabase(context).userDao().insertCommunities(temp.get(i));
//                                    }
                                    AppDatabase.getAppDatabase(context).userDao().insertCommunities(temp.get(i));
                                }
                                setRecyclerView();
                                mAdapter.notifyDataSetChanged();
                                textNoData.setVisibility(View.GONE);
                            //    getCommunities(false);
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

    private void showPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage("Internet connection is required");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting CANCEL Button
        alertDialog.setButton2(getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
            context.finish();
            context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });
        // Setting OK Button
        alertDialog.setButton(getString(android.R.string.ok), (dialog, which) -> {
            alertDialog.dismiss();
            context.finish();
            context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCommunities(false);
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

    private TextWatcher watch = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
            // TODO Auto-generated method stub
            setFilter(s.toString());

        }
    };

    private void setFilter(String s) {
        communityList.clear();
        communityList.addAll(replicaCommunityList);

        List<Community> list = new ArrayList<>();
        for (int i = 0; i < communityList.size(); i++) {
            if (communityList.get(i).getName() != null &&
                    communityList.get(i).getName().toLowerCase().contains(s.toLowerCase())) {
                list.add(communityList.get(i));
            }
        }

        communityList.clear();
        communityList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

    public void onLayoutGroupClick(int position) {
        if (position > -1) {
            if (Constants.shareUri != null) {
                Intent intent;
                if (communityList.get(position).getName().equalsIgnoreCase("HO Support")) {
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Issue");
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, BuildConfig.ISSUEID);
                    intent = new Intent(context, IssueTemplateActivity.class);
                    intent.putExtra("EDIT", false);
                    context.startActivity(intent);
                } else {
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Report");
                    preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, BuildConfig.REPORTID);
                    intent = new Intent(context, ReportingTemplateActivity.class);
                    intent.putExtra("EDIT", false);
                    context.startActivity(intent);
                }
            } else {
                preferenceHelper.insertString(PreferenceHelper.COMMUNITYID, communityList.get(position).getId());
                List<Community> list = new ArrayList<>();
                list.addAll(communityList);
                list.remove(position);
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(list);
                Intent intent = new Intent(context, CommunityHomeActivity.class);
                intent.putExtra(Constants.TITLE, communityList.get(position).getName());
                //sending new intent to check user can post or not
                intent.putExtra("CanPost", communityList.get(position).getCanPost());
                intent.putExtra(Constants.LIST, json);
                startActivity(intent);
            }
        }
    }
}