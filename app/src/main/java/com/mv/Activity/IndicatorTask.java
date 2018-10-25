package com.mv.Activity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.IndicatortaskAdapter;
import com.mv.Model.DashaBoardListModel;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.databinding.ActivityIndicatorTaskBinding;

public class IndicatorTask extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ActivityIndicatorTaskBinding binding;
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    DashaBoardListModel dashaBoardListModel;
    private RelativeLayout mToolBar;
    //private ActivityProgrammeManagmentBinding binding;
    private PreferenceHelper preferenceHelper;

    private IndicatortaskAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_indicator_task_);
        binding.setActivity(this);
        initViews();

    }

    private void initViews() {

        if(getIntent().getExtras().getParcelable(Constants.PROCESS_ID)!=null)
          dashaBoardListModel = getIntent().getExtras().getParcelable(Constants.PROCESS_ID);

        preferenceHelper = new PreferenceHelper(this);
        setActionbar(dashaBoardListModel.getName());
        binding.swiperefresh.setOnRefreshListener(this);
        mAdapter = new IndicatortaskAdapter(dashaBoardListModel, IndicatorTask.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);

    }


    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }





    @Override
    public void onRefresh() {
        binding.swiperefresh.setRefreshing(false);

    }
}
