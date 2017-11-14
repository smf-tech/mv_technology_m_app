package com.mv.Fragment;

/**
 * Created by Rohit Gujar on 09-10-2017.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Activity.CommunityHomeActivity;
import com.mv.Activity.IssueTemplateActivity;
import com.mv.Activity.ReportingTemplateActivity;
import com.mv.Adapter.GroupAdapter;
import com.mv.Model.Community;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
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


public class GroupsFragment extends Fragment implements View.OnClickListener {
    private GroupAdapter mAdapter;
    private ActivityGroupBinding binding;
    private List<Community> communityList = new ArrayList<>();
    private List<Community> replicaCommunityList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(
                inflater, R.layout.activity_group, container, false);
        View view = binding.getRoot();
        //here data must be an instance of the class MarsDataProvider
        Utills.setupUI(view.findViewById(R.id.layout_main), getActivity());
        binding.setFragment(this);
        initViews();
        getCommunities(true);
        return view;
    }

    private void getCommunities(boolean isDialogShow) {
        List<Community> temp = AppDatabase.getAppDatabase(getActivity()).userDao().getAllCommunities();
        if (temp.size() == 0) {
            if (Utills.isConnected(getActivity()))
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
            if (Utills.isConnected(getActivity()))
                getAllCommunities(true, isDialogShow);
        }
    }


    private void initViews() {
        binding.editTextEmail.addTextChangedListener(watch);
        binding.swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getCommunities(false);
                    }
                }
        );
        preferenceHelper = new PreferenceHelper(getActivity());
        mAdapter = new GroupAdapter(communityList, getActivity(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
    }

    private void getAllCommunities(boolean isTimePresent, boolean isDialogShow) {
        if (isDialogShow)
            Utills.showProgressDialog(getActivity(), "Loading Communities", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(getActivity()).create(ServiceRequest.class);
        String url = "";
        if (isTimePresent)
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/MV_GetCommunities_c?userId=" + User.getCurrentUser(getActivity()).getId()
                    + "&timestamp=" + communityList.get(communityList.size() - 1).getTime();
        else
            url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                    + "/services/apexrest/MV_GetCommunities_c?userId=" + User.getCurrentUser(getActivity()).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                binding.swiperefresh.setRefreshing(false);
                try {
                    if (response.body() != null) {
                        String str = response.body().string();
                        if (str != null && str.length() > 0) {
                            JSONArray jsonArray = new JSONArray(str);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            List<Community> temp = Arrays.asList(gson.fromJson(jsonArray.toString(), Community[].class));
                            for (int i = 0; i < temp.size(); i++) {
                                communityList.add(temp.get(i));
                                replicaCommunityList.add(temp.get(i));
                                AppDatabase.getAppDatabase(getActivity()).userDao().insertCommunities(temp.get(i));
                            }
                            mAdapter.notifyDataSetChanged();
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
            case R.id.img_back:
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    TextWatcher watch = new TextWatcher() {

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
        List<Community> list = new ArrayList<>();
        communityList.clear();
        for (int i = 0; i < replicaCommunityList.size(); i++) {
            communityList.add(replicaCommunityList.get(i));
        }
        for (int i = 0; i < communityList.size(); i++) {
            if (communityList.get(i).getName().toLowerCase().contains(s.toLowerCase())) {
                list.add(communityList.get(i));
            }
        }
        communityList.clear();
        for (int i = 0; i < list.size(); i++) {
            communityList.add(list.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }


    public void onLayoutGroupClick(int position) {
        if (Constants.shareUri != null) {
            Intent intent;
            if (communityList.get(position).getName().equalsIgnoreCase("HO Support")) {
                preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Issue");
                preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, Constants.ISSUEID);
                intent = new Intent(getActivity(), IssueTemplateActivity.class);
                getActivity().startActivity(intent);
            } else {
                preferenceHelper.insertString(PreferenceHelper.TEMPLATENAME, "Report");
                preferenceHelper.insertString(PreferenceHelper.TEMPLATEID, Constants.REPORTID);
                intent = new Intent(getActivity(), ReportingTemplateActivity.class);
                getActivity().startActivity(intent);
            }
        } else {
            preferenceHelper.insertString(PreferenceHelper.COMMUNITYID, communityList.get(position).getId());
            List<Community> list = new ArrayList<Community>();
            for (int i = 0; i < communityList.size(); i++) {
                list.add(communityList.get(i));
            }
            list.remove(position);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(list);
            Intent intent;
            intent = new Intent(getActivity(), CommunityHomeActivity.class);
            intent.putExtra(Constants.TITLE, communityList.get(position).getName());
            intent.putExtra(Constants.LIST, json);
            startActivity(intent);
        }

    }

}