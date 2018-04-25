package com.mv.ActivityMenu;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.TeamManagementAdapter;
import com.mv.BR;
import com.mv.Model.ParentViewModel;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.databinding.ActivityNewTemplateBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanostuffs on 16-11-2017.
 */

public class TeamManagementFragment extends AppCompatActivity implements View.OnClickListener {
    private PreferenceHelper preferenceHelper;
    List<Template> processAllList = new ArrayList<>();
    private TeamManagementAdapter mAdapter;
    ArrayList<String> menuList;
    private ActivityNewTemplateBinding binding;
    RecyclerView.LayoutManager mLayoutManager;
    TextView textNoData;

    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_template);
        binding.setVariable(BR.vm, new ParentViewModel());

    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
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

    private void initViews() {
        setActionbar(getString(R.string.team_management));
        textNoData = (TextView) findViewById(R.id.textNoData);
        preferenceHelper = new PreferenceHelper(context);
        menuList = new ArrayList<>();
        menuList.add(getString(R.string.team_user_approval));
        menuList.add(getString(R.string.team_form_approval));
        menuList.add(getString(R.string.expense_approval));
        menuList.add(getString(R.string.adavance_approval));
        menuList.add(getString(R.string.leave));
        processAllList.clear();
        for (int i = 0; i < menuList.size(); i++) {
            Template processList = new Template();
            processList.setName(menuList.get(i));
            processAllList.add(processList);
        }
        mAdapter = new TeamManagementAdapter(processAllList, context);
        mLayoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        binding.swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        binding.swiperefresh.setRefreshing(false);
                    }
                }
        );
        binding.swiperefresh.setRefreshing(false);

    }

}
