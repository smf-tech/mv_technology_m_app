package com.mv.Activity;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.R;
import com.mv.Utils.LocaleManager;
import com.mv.databinding.ActivityCalenderEventReportingBinding;

public class CalenderEventReportingActivity extends AppCompatActivity implements View.OnClickListener {
    private Activity context;
    private ActivityCalenderEventReportingBinding binding;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender_event_reporting);
        context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reporting_template);
        binding.setActivity(this);

        initViews();
    }

    private void initViews() {
        setActionbar(getString(R.string.claender_reportinng));
        binding.btnSubmit.setOnClickListener(this);
        binding.etCalEventDate.setOnClickListener(this);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setActionbar(String Title) {

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                context.finish();
                context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_submit:

                break;
            case R.id.cal_event_date:

                break;
        }
    }
}
