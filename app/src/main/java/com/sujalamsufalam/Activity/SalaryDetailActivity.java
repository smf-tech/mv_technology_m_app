package com.sujalamsufalam.Activity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sujalamsufalam.Model.Salary;
import com.sujalamsufalam.Model.User;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.LocaleManager;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.databinding.ActivitySalaryDetailBinding;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class SalaryDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private PreferenceHelper preferenceHelper;
    private ActivitySalaryDetailBinding binding;
    private Salary salary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_salary_detail);
        binding.setActivity(this);
        salary = (Salary) getIntent().getSerializableExtra(Constants.SALARY);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setActionbar("Month :- " + salary.getMonth());
        preferenceHelper = new PreferenceHelper(this);
        initView();
    }

    private void initView() {

        binding.txtTelephone.setText(salary.getTelephone_Expense__c());
        binding.txtNetSalary.setText(salary.getNet_Salary__c());

        binding.txtEmployeeNo.setText(User.getCurrentUser(this).getExtendedUser().getEmployee_Id__c());
        binding.txtBankAcc.setText(User.getCurrentUser(this).getExtendedUser().getBank_Account_Number__c());
        binding.txtPfNo.setText(User.getCurrentUser(this).getExtendedUser().getPF_Number__c());
        binding.txtUAN.setText(User.getCurrentUser(this).getExtendedUser().getUAN_Number__c());

        binding.txtAbsentDays.setText(salary.getAbsent_Days__c());
        binding.txtArrears.setText(salary.getArrears__c());

        binding.txtSecurity.setText(salary.getSecurity_Fund__c());
        binding.txtSpecial.setText(salary.getSpecial_Allowance__c());
        binding.txtTDS.setText(salary.getTDS__c());
        binding.txtTotalPresenntDays.setText(salary.getPresent_Days__c());

        binding.txtUnpaidLeave.setText(salary.getUnpaid_Leaves__c());

        binding.txtDeductions.setText(salary.getTotal_Deductions__c());
        binding.txtGross.setText(salary.getGross_Earning_for_the_Month__c());
        binding.txtMedical.setText(salary.getMedical_Allowance__c());
        binding.txtName.setText(salary.getName());
        binding.txtHRA.setText(salary.getHRA__c());

        binding.txtOther.setText(salary.getOther_Deductions__c());
        binding.txtPaidLeave.setText(salary.getPaid_Leaves__c());
        binding.txtPerks.setText(salary.getPerks__c());
        binding.txtPF.setText(salary.getProvident_Fund__c());
        binding.txtProfession.setText(salary.getProfession_Tax__c());
        binding.txtSalaryAdvance.setText(salary.getSalary_Advance__c());

        binding.txtConsolidated.setText(salary.getConsolidated_Basic__c());
        binding.txtConveyance.setText(salary.getConveyance_Allowance__c());

        binding.txtHouserent.setText(salary.getHouse_Rent__c());
        binding.txtTravelExp.setText(salary.getTravelling_Exp__c());
        binding.txtTeleExp.setText(salary.getTelephone_Expense__Allowance());
        binding.txtsecurityfund.setText(salary.getSecurity_Fund__Allowance());
        if(salary.getSecurity_Fund__Allowance()==null)
            binding.txtsecurityfund.setText("0.0");
        else
           binding.txtsecurityfund.setText(salary.getSecurity_Fund__Allowance());
        binding.txtAppointAllow.setText(salary.getAppontment_Allowance__c());
        if(salary.getAny_other__c()==null)
            binding.txtAnyOthers.setText("0.0");
         else
            binding.txtAnyOthers.setText(salary.getAny_other__c());
        binding.txtTotalreimbursement.setText(salary.getTotal_Reimbursement__c());
        binding.txtNetBankAmount.setText(salary.getTotal_Amount_to_Bank_Net_Salary_Reimbur__c());
        binding.txtLeaveDeductionAmt.setText(salary.getTotal_Leave_Deduction__c());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
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

    @Override
    protected void onResume() {
        super.onResume();
    }


}
