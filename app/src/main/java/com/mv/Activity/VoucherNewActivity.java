package com.mv.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityVoucherNewBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class VoucherNewActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityVoucherNewBinding binding;
    private int mProjectSelect = 0;
    private List<String> projectList = new ArrayList<>();
    private Voucher mVoucher;
    private boolean isAdd;
    private int voucherId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_voucher_new);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initViews();
    }

    private void initViews() {
        projectList = Arrays.asList(getResources().getStringArray(R.array.array_of_project));
        setActionbar(getString(R.string.voucher_new));
        binding.txtDate.setOnClickListener(this);
        binding.spinnerProject.setOnItemSelectedListener(this);
        if (getIntent().getExtras().getString(Constants.ACTION).equalsIgnoreCase(Constants.ACTION_ADD)) {
            isAdd = true;
        } else {
            isAdd = false;
            mVoucher = (Voucher) getIntent().getSerializableExtra(Constants.VOUCHER);
            voucherId = mVoucher.getUniqueId();
            binding.txtDate.setText(mVoucher.getDate());
            binding.editTextCount.setText(mVoucher.getNoOfPeople());
            binding.editTextDescription.setText(mVoucher.getDecription());
            mProjectSelect = projectList.indexOf(mVoucher.getProject());
            binding.spinnerProject.setSelection(mProjectSelect);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.txtDate:
                showDateDialog();
                break;
        }
    }

    private void showDateDialog() {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        binding.txtDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    private static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
    }

    public void onSubmitClick() {
        if (isValid()) {
            Voucher voucher = new Voucher();
            if (!isAdd)
                voucher.setUniqueId(mVoucher.getUniqueId());
            voucher.setProject(projectList.get(mProjectSelect));
            voucher.setDate(binding.txtDate.getText().toString().trim());
            voucher.setDecription(binding.editTextDescription.getText().toString().trim());
            voucher.setNoOfPeople(binding.editTextCount.getText().toString().trim());
            AppDatabase.getAppDatabase(this).userDao().insertVoucher(voucher);
            Utills.showToast("Voucher Added successfully", this);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }


    private boolean isValid() {
        String str = "";
        if (mProjectSelect == 0) {
            str = "Please select Project";
        } else if (binding.txtDate.getText().toString().trim().length() == 0) {
            str = "Please select Date";
        } else if (binding.editTextDescription.getText().toString().trim().length() == 0) {
            str = "Please enter Description Of Tour";
        } else if (binding.editTextCount.getText().toString().trim().length() == 0) {
            str = "Please enter No Of People Travelled";
        }
        if (str.length() != 0) {
            Utills.showToast(str, this);
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setActionbar(String Title) {

        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        String str = Title;
        if (Title != null && Title.contains("\n"))
            str = Title.replace("\n", " ");
        toolbar_title.setText(str);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.GONE);
        img_list.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinnerProject:
                mProjectSelect = i;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}
