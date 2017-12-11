package com.mv.Fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mv.Adapter.TeamManagementUserProfileAdapter;
import com.mv.BR;
import com.mv.Model.ParentViewModel;
import com.mv.Model.Template;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityNewTemplateBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nanostuffs on 16-11-2017.
 */

public class TeamManagementFragment  extends Fragment {
    private PreferenceHelper preferenceHelper;
    List<Template> processAllList = new ArrayList<>();
    private TeamManagementUserProfileAdapter mAdapter;
    private ActivityNewTemplateBinding binding;
    RecyclerView.LayoutManager mLayoutManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.activity_new_template, container, false);
        View view = binding.getRoot();
        binding.setVariable(BR.vm, new ParentViewModel());
        RelativeLayout mToolBar = (RelativeLayout) view.findViewById(R.id.toolbar);
        mToolBar.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    private void initViews() {
        preferenceHelper = new PreferenceHelper(getActivity());


        Template processList = new Template();
        processList.setName("User Profile");
        processAllList.clear();
        processAllList.add(processList);
        mAdapter = new TeamManagementUserProfileAdapter(processAllList, getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);


    }

}
