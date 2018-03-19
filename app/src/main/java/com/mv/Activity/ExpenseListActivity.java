package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.ExpenseAdapter;
import com.mv.Model.Expense;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityExpenseListBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class ExpenseListActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityExpenseListBinding binding;
    private ExpenseAdapter adapter;
    private List<Expense> mList = new ArrayList<>();
    private int voucherId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_expense_list);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setActionbar(getString(R.string.expense_list));
        voucherId = getIntent().getExtras().getInt(Constants.VOUCHERID);
    }

    private void setRecyclerView() {
        Log.i("Count", "" + AppDatabase.getAppDatabase(this).userDao().getAllExpense());
        mList = AppDatabase.getAppDatabase(this).userDao().getAllExpense(voucherId);
        adapter = new ExpenseAdapter(this, mList);
        binding.rvExpense.setAdapter(adapter);
        binding.rvExpense.setHasFixedSize(true);
        binding.rvExpense.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    public void onAddClick() {
        Intent intent;
        intent = new Intent(this, ExpenseNewActivity.class);
        intent.putExtra(Constants.VOUCHERID, voucherId);
        intent.putExtra(Constants.ACTION, Constants.ACTION_ADD);
        startActivity(intent);
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
    protected void onResume() {
        super.onResume();
        setRecyclerView();
    }

    public void editExpense(int adapterPosition) {
        Log.i("voucherId", "" + voucherId);
        Intent intent;
        intent = new Intent(this, ExpenseNewActivity.class);
        intent.putExtra(Constants.VOUCHERID, voucherId);
        intent.putExtra(Constants.EXPENSE, mList.get(adapterPosition));
        intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
        startActivity(intent);
    }

    public void deleteExpense(int position) {
        AppDatabase.getAppDatabase(this).userDao().deleteExpense(mList.get(position));
        mList.remove(position);
        adapter.notifyItemRemoved(position);
        Utills.showToast("Expense Deleted Successfully", this);
    }
}
