

package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.AdavanceListActivity;
import com.mv.Activity.ExpenseListActivity;
import com.mv.Activity.VoucherListActivity;
import com.mv.Model.Adavance;
import com.mv.Model.Expense;
import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;

import java.util.List;

/**
 * Created by acer on 9/8/2016.
 */


public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    private Context mContext;
    private Resources resources;
    private List<Voucher> mDataList;
    private VoucherListActivity mActivity;

    public VoucherAdapter(Context context, List<Voucher> list) {
        mContext = context;
        resources = context.getResources();
        mDataList = list;
        mActivity = (VoucherListActivity) context;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Voucher voucher = mDataList.get(position);
        List<Expense> expenses = AppDatabase.getAppDatabase(mContext).userDao().getAllExpense(voucher.getId());
        double amount = 0;
        for (Expense temp : expenses) {
            if (temp.getAmount() != null && temp.getAmount().length() > 0)
                amount += Double.parseDouble(temp.getAmount());
        }
        List<Adavance> adavances = AppDatabase.getAppDatabase(mContext).userDao().getAllAdvance(voucher.getId(),"Approved" );
        double adavanceAmount = 0;
        for (Adavance temp : adavances) {
            if (temp.getAmount() != null && temp.getAmount().length() > 0)
                adavanceAmount += Double.parseDouble(temp.getAmount());
        }

        holder.tvProjectName.setText(voucher.getProject());
        holder.tvDateName.setText(voucher.getDate());
        holder.tvNoOfPeopleName.setText(voucher.getPlace());
        holder.tvTotalExpenseName.setText("₹ " + amount);
        holder.tvTotalAdvance.setText("₹ " + adavanceAmount);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_voucher, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvProjectName, tvDateName, tvNoOfPeopleName, tvTotalExpenseName,tvTotalAdvance;
        ImageView imgEdit, imgDelete, imgExpense;
        LinearLayout layout_expense, layout_adavance;

        public ViewHolder(View itemLayoutView) {

            super(itemLayoutView);

            imgEdit = (ImageView) itemLayoutView.findViewById(R.id.imgEdit);
            imgDelete = (ImageView) itemLayoutView.findViewById(R.id.imgDelete);

            tvProjectName = (TextView) itemLayoutView.findViewById(R.id.tvProjectName);
            tvDateName = (TextView) itemLayoutView.findViewById(R.id.tvDateName);
            tvNoOfPeopleName = (TextView) itemLayoutView.findViewById(R.id.tvNoOfPeopleName);
            tvTotalExpenseName = (TextView) itemLayoutView.findViewById(R.id.tvTotalExpenseName);
            tvTotalAdvance = (TextView) itemLayoutView.findViewById(R.id.tvTotalAdvance);
            layout_expense = (LinearLayout) itemLayoutView.findViewById(R.id.layout_expense);
            layout_adavance = (LinearLayout) itemLayoutView.findViewById(R.id.layout_adavance);

            // hiding views for team mgmt section
            if(Constants.AccountTeamCode.equals("TeamManagement")){
                imgEdit.setVisibility(View.GONE);
                imgDelete.setVisibility(View.GONE);
            }

            imgEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.editVoucher(getAdapterPosition());
                }
            });
            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLogoutPopUp(getAdapterPosition());
                }
            });
            layout_expense.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent;
                    intent = new Intent(mContext, ExpenseListActivity.class);
                    intent.putExtra(Constants.VOUCHER, mDataList.get(getAdapterPosition()));
                    mActivity.startActivity(intent);
                }
            });
            layout_adavance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent;
                    intent = new Intent(mContext, AdavanceListActivity.class);
                    intent.putExtra(Constants.VOUCHER, mDataList.get(getAdapterPosition()));
                    mActivity.startActivity(intent);
                }
            });

        }
    }

    private void showLogoutPopUp(final int postion) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                // Write your code here to execute after dialog closed
              /*  listOfWrongQuestions.add(mPosition);
                prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
            }
        });
        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mActivity.deleteVoucher(postion);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


}

