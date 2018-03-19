package com.mv.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Model.Expense;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityExpenseNewBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class ExpenseNewActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityExpenseNewBinding binding;
    private int mParticularSelect = 0;
    private List<String> particularList = new ArrayList<>();
    private Expense mExpense;
    private boolean isAdd;
    private int voucherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_expense_new);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initViews();
    }

    private void initViews() {
        voucherId = getIntent().getExtras().getInt(Constants.VOUCHERID);
        particularList = Arrays.asList(getResources().getStringArray(R.array.array_of_particulars));
        setActionbar(getString(R.string.expense_new));
        binding.txtDate.setOnClickListener(this);
        binding.spinnerParticular.setOnItemSelectedListener(this);
        if (getIntent().getExtras().getString(Constants.ACTION).equalsIgnoreCase(Constants.ACTION_ADD)) {
            isAdd = true;
        } else {
            isAdd = false;
            mExpense = (Expense) getIntent().getSerializableExtra(Constants.EXPENSE);
            binding.txtDate.setText(mExpense.getDate());
            binding.editTextAmount.setText(mExpense.getAmount());
            binding.editTextDescription.setText(mExpense.getDecription());
            mParticularSelect = particularList.indexOf(mExpense.getPartuculars());
            binding.spinnerParticular.setSelection(mParticularSelect);
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

    public void onSubmitClick() {
        if (isValid()) {
            Expense expense = new Expense();
            if (!isAdd)
                expense.setUniqueId(mExpense.getUniqueId());
            expense.setPartuculars(particularList.get(mParticularSelect));
            expense.setDate(binding.txtDate.getText().toString().trim());
            expense.setDecription(binding.editTextDescription.getText().toString().trim());
            expense.setAmount(binding.editTextAmount.getText().toString().trim());
            Log.i("voucherId", "" + voucherId);
            expense.setVoucherId("" + voucherId);
            AppDatabase.getAppDatabase(this).userDao().insertExpense(expense);
            Utills.showToast("Expense Added successfully", this);
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    private boolean isValid() {
        String str = "";
        if (mParticularSelect == 0) {
            str = "Please select Project";
        } else if (binding.txtDate.getText().toString().trim().length() == 0) {
            str = "Please select Date";
        } else if (binding.editTextAmount.getText().toString().trim().length() == 0) {
            str = "Please enter Amount";
        } else if (binding.editTextDescription.getText().toString().trim().length() == 0) {
            str = "Please enter Description Of Tour";
        }
        if (str.length() != 0) {
            Utills.showToast(str, this);
            return false;
        }
        return true;
    }

    private static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinnerParticular:
                mParticularSelect = i;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
