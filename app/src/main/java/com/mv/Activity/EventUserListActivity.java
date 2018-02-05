package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.mv.Adapter.EventUserListAdapter;
import com.mv.Model.Community;
import com.mv.Model.EventUser;
import com.mv.Model.Task;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.databinding.ActivityEventUserListBinding;

import java.util.ArrayList;
import java.util.List;


public class EventUserListActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ActivityEventUserListBinding binding;
    private ImageView img_back, img_logout;
    private TextView toolbar_title;
    ArrayList<EventUser> eventUsers=new ArrayList<>();
    ArrayList<EventUser> eventUsersFliter=new ArrayList<>();
    ArrayList<EventUser> selectedUser=new ArrayList<>();
    private RelativeLayout mToolBar;
    //private ActivityProgrammeManagmentBinding binding;
    private PreferenceHelper preferenceHelper;

    private EventUserListAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_user_list);
        binding.setActivity(this);
        initViews();

    }


    private void initViews() {
        eventUsers = getIntent().getExtras().getParcelableArrayList(Constants.PROCESS_ID);

        preferenceHelper = new PreferenceHelper(this);
        setActionbar("User List");
        binding.swiperefresh.setOnRefreshListener(this);
        mAdapter = new EventUserListAdapter(eventUsers, EventUserListActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mAdapter);
        binding.btnSubmit.setOnClickListener(this);
        binding.editTextEmail.addTextChangedListener(watch);
        binding.cbEventSelectAll.setOnClickListener(this);
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
        List<EventUser> list = new ArrayList<>();

        eventUsersFliter.clear();
        for (int i = 0; i < eventUsers.size(); i++) {
            eventUsersFliter.add(eventUsers.get(i));
        }
        list.clear();
        for (int i = 0; i < eventUsersFliter.size(); i++) {
            if (eventUsersFliter.get(i).getUserName().toLowerCase().contains(s.toLowerCase())) {
                list.add(eventUsersFliter.get(i));
            }
        }

        mAdapter = new EventUserListAdapter(list, EventUserListActivity.this);
        binding.recyclerView.setAdapter(mAdapter);
    }
    public void saveDataToList(EventUser eventUser ,boolean isSelected) {
        if(isSelected)
        selectedUser.add(eventUser);
        else
            selectedUser.remove(eventUsers);

        eventUsers.set( eventUsers.indexOf(eventUser), eventUser);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_submit:
                Intent openClass = new Intent(EventUserListActivity.this, CalenderFliterActivity.class);
                // openClass.putExtra(Constants.PROCESS_ID, dashaBoardListModel);
                if(binding.cbEventSelectAll.isChecked())
                    openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, eventUsers);
                else
                openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, selectedUser);
                //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                setResult(RESULT_OK, openClass);
                //  startActivity(openClass);
                finish();
                break;
            case R.id.cb_event_select_all:
                if (((CheckBox) view).isChecked()) {
                    for (EventUser eventUser : eventUsers) {
                        eventUser.setUserSelected(true);
                    }
                }
                else
                    {
                        for (EventUser eventUser : eventUsers) {
                            eventUser.setUserSelected(false);
                        }
                    }

                mAdapter.notifyDataSetChanged();
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
