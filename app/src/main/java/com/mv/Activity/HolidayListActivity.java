package com.mv.Activity;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.HolldayListAdapter;
import com.mv.Model.HolidayListModel;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityHolidayListBinding;

import java.util.ArrayList;
import java.util.List;


public class HolidayListActivity extends AppCompatActivity implements View.OnClickListener {
    protected ActivityHolidayListBinding binding;
    Activity context;
    List<HolidayListModel> holidayListModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_holiday_list);
        binding.setClander(this);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        holidayListModels = AppDatabase.getAppDatabase(HolidayListActivity.this).userDao().getAllHolidayList();
        List<HolidayListModel> temp = new ArrayList<>();
        for (int i = 0; i < holidayListModels.size(); i++) {
            if (!holidayListModels.get(i).getCategory__c().equals("Weekly Off")) {
                temp.add(holidayListModels.get(i));
            }

        }
        HolldayListAdapter holldayListAdapter = new HolldayListAdapter(context, temp);
        binding.recyclerView.setAdapter(holldayListAdapter);
        setActionbar(getString(R.string.holiday_list));
        binding.swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (Utills.isConnected(context))
                            binding.swiperefresh.setRefreshing(false);
                    }
                }
        );

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setActionbar(String Title) {
        String str = Title;
        if (str.contains("\n")) {
            str = str.replace("\n", " ");
        }
        LinearLayout layoutList = (LinearLayout) findViewById(R.id.layoutList);

        RelativeLayout mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(str);
        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setImageResource(R.drawable.ic_action_calender);
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

}
