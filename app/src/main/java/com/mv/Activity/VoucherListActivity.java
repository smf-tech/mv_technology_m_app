package com.mv.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Adapter.VoucherAdapter;
import com.mv.Model.User;
import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityVoucherListBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private PreferenceHelper preferenceHelper;
    //added this code for sorting vouchers by username
    private ArrayList<Voucher> voucherUsersFliter = new ArrayList<>();
    private VoucherAdapter mAdapter;
    private ArrayList<Voucher> voucherDateFliter = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_voucher_list);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setActionbar(getString(R.string.voucher_list));
        preferenceHelper = new PreferenceHelper(this);

        if (Constants.AccountTeamCode.equals("TeamManagement")) {
            binding.fabAddProcess.setVisibility(View.GONE);
            binding.sortLayout.setVisibility(View.VISIBLE);
            binding.sortButton.setOnClickListener(this);
            binding.editTextEmail.addTextChangedListener(watch);
            binding.calendarSortButton.setOnClickListener(this);
            binding.txtDateFrom.setOnClickListener(this);
            binding.txtDateTo.setOnClickListener(this);
        }else{
            binding.rvVoucher.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (dy < -5 && binding.fabAddProcess.getVisibility() != View.VISIBLE) {
                        binding.fabAddProcess.show();
                    } else if (dy > 5 && binding.fabAddProcess.getVisibility() == View.VISIBLE) {
                        binding.fabAddProcess.hide();
                    }
                }
            });
        }

        if (Utills.isConnected(this)) {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                getUserVoucherDataForTeam();
            } else {
                getUserVoucherData();
            }
        }
    }

    //Seperate call for Team Management
    private void getUserVoucherDataForTeam() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetPendingVoucherData + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            if (Arrays.asList(gson.fromJson(str, Voucher[].class)).size()>0) {
//                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().deleteAllVoucher();
//                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().insertVoucher();
                                mList = Arrays.asList(gson.fromJson(str, Voucher[].class));
                                setRecyclerViewForTeam();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    private void getUserVoucherData() {
        Utills.showProgressDialog(this, "Loading Data", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserVoucherData + "?userId=" + User.getCurrentUser(getApplicationContext()).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.length() > 0) {
                            if (Arrays.asList(gson.fromJson(str, Voucher[].class)).size()>0) {
                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().deleteAllVoucher();
                                AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().insertVoucher(Arrays.asList(gson.fromJson(str, Voucher[].class)));
                                setRecyclerView();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }
    //added this code for sorting vouchers by username
    private TextWatcher watch = new TextWatcher() {

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
        List<Voucher> list = new ArrayList<>();

        voucherUsersFliter.clear();
        voucherUsersFliter.addAll(mList);
        list.clear();
        for (int i = 0; i < voucherUsersFliter.size(); i++) {
            if (voucherUsersFliter.get(i).getUserName()!=null && voucherUsersFliter.get(i).getUserName().toLowerCase().contains(s.toLowerCase())) {
                list.add(voucherUsersFliter.get(i));
            }
        }

        mAdapter = new VoucherAdapter(VoucherListActivity.this,list);
        binding.rvVoucher.setAdapter(mAdapter);

    }


    private void setRecyclerView() {
        mList = AppDatabase.getAppDatabase(this).userDao().getAllVoucher();
        adapter = new VoucherAdapter(this, mList);
        binding.rvVoucher.setAdapter(adapter);
        binding.rvVoucher.setHasFixedSize(true);
        binding.rvVoucher.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setRecyclerViewForTeam() {
        if(mList.size()>0){
            binding.txtNodata.setVisibility(View.GONE);
            adapter = new VoucherAdapter(this, mList);
            binding.rvVoucher.setAdapter(adapter);
            binding.rvVoucher.setHasFixedSize(true);
            binding.rvVoucher.setLayoutManager(new LinearLayoutManager(this));
        } else {
            binding.sortLayout.setVisibility(View.GONE);
            binding.txtNodata.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;

            case R.id.calendar_sort_button:
                if(binding.voucherDateFilterLayout.getVisibility()==View.GONE)
                    binding.voucherDateFilterLayout.setVisibility(View.VISIBLE);
                else if(binding.voucherDateFilterLayout.getVisibility()==View.VISIBLE)
                    binding.voucherDateFilterLayout.setVisibility(View.GONE);
                break;

            case R.id.txtDateFrom:
                showDateDialog(binding.txtDateFrom);
                break;
            case R.id.txtDateTo:
                showDateDialog(binding.txtDateTo);
                break;

            case R.id.sort_button:
                if(binding.txtDateFrom.getText().toString().trim().length()>0 && binding.txtDateTo.getText().toString().trim().length()>0)
                    FilterVouchers_withDate(binding.txtDateFrom.getText().toString().trim(),binding.txtDateTo.getText().toString().trim());
                else
                    Utills.showToast("Enter proper date range.", VoucherListActivity.this);

                break;

        }
    }
    //compare voucher-date in between from and to dates
    private void FilterVouchers_withDate(String fromDate, String toDate) {
        //firstly convert string to date and check if dates are valid
        Date datefrom = ConvertStringToDate(fromDate);
        Date dateto = ConvertStringToDate(toDate);
        voucherDateFliter.clear();
        if (datefrom.before(dateto)||datefrom.equals(dateto)) {
            for (Voucher v : mList) {
                Date voucherdate = ConvertStringToDate(v.getDate());
                //   boolean b = datefrom.compareTo(voucherdate) * voucherdate.compareTo(dateto) >= 0;
                if (datefrom.compareTo(voucherdate) * voucherdate.compareTo(dateto) >= 0) {
                    voucherDateFliter.add(v);
                }
            }
            if(voucherDateFliter.size()>0) {
                mAdapter = new VoucherAdapter(VoucherListActivity.this, voucherDateFliter);
                binding.rvVoucher.setAdapter(mAdapter);
            }else{
                Toast.makeText(this, "No voucher available.",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, R.string.text_proper_date, Toast.LENGTH_LONG).show();
        }

    }

    private Date ConvertStringToDate(String stringDate){
        Date parsedDate  = null;
        try {
            if(stringDate!=null) {
                parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
                System.out.println(parsedDate);
                return parsedDate;
            }
        }catch (Exception e){
            Log.e("MYAPP", "exception", e);
        }
        return parsedDate;
    }


    //adding date picker dialog for voucher date filteration
    private void showDateDialog(TextView textView) {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> textView.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth)), mYear, mMonth, mDay);
        dpd.show();
    }
    //returns two digit number of month
    private static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
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

        if (Constants.AccountTeamCode.equals("TeamManagement")) {
            getUserVoucherDataForTeam();
            binding.fabAddProcess.setVisibility(View.GONE);
        } else {
            getUserVoucherData();
        }

        setRecyclerView();
    }

    public void editVoucher(Voucher voucher) {
        Intent intent;
        intent = new Intent(this, VoucherNewActivity.class);
        intent.putExtra(Constants.ACTION, Constants.ACTION_EDIT);
        intent.putExtra(Constants.VOUCHER, voucher);
        startActivity(intent);
    }

    public void deleteVoucher(int position) {
        if (Utills.isConnected(this)) {
            deleteRecord(position);
        } else {
            Utills.showInternetPopUp(this);
        }

    }

    private void deleteRecord(int position) {
        Utills.showProgressDialog(this, "Deleting Voucher", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.DeleteAccountData + "?Id=" + mList.get(position).getId() + "&Object=Voucher";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                try {
                    if (response != null && response.isSuccess()) {
                        String str = response.body().string();
                        if (str.contains("deleted")) {
                            AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().deleteVoucher(mList.get(position));
                            AppDatabase.getAppDatabase(VoucherListActivity.this).userDao().deleteExpense(mList.get(position).getId());
                            mList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Utills.showToast("Voucher Deleted Successfully", VoucherListActivity.this);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }
}
