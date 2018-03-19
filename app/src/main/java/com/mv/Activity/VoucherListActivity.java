package com.mv.Activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Adapter.VoucherAdapter;
import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityVoucherListBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class VoucherListActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityVoucherListBinding binding;
    private VoucherAdapter adapter;
    private List<Voucher> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_voucher_list);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setActionbar(getString(R.string.voucher_list));
    }

    private void setRecyclerView() {
        mList = AppDatabase.getAppDatabase(this).userDao().getAllVoucher();
        adapter = new VoucherAdapter(this, mList);
        binding.rvVoucher.setAdapter(adapter);
        binding.rvVoucher.setHasFixedSize(true);
        binding.rvVoucher.setLayoutManager(new LinearLayoutManager(this));
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
        intent = new Intent(this, VoucherNewActivity.class);
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

    public void editVoucher(int position) {
        Intent intent;
        intent = new Intent(this, VoucherNewActivity.class);
        intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
        intent.putExtra(Constants.VOUCHER, mList.get(position));
        startActivity(intent);
    }

    public void deleteVoucher(int position) {
        AppDatabase.getAppDatabase(this).userDao().deleteVoucher(mList.get(position));
        AppDatabase.getAppDatabase(this).userDao().deleteExpense(mList.get(position).getUniqueId());
        mList.remove(position);
        adapter.notifyItemRemoved(position);
        Utills.showToast("Voucher Deleted Successfully", this);
    }
}
