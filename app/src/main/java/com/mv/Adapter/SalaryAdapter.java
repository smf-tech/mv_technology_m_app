

package com.mv.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mv.Activity.SalaryDetailActivity;
import com.mv.Model.Salary;
import com.mv.R;
import com.mv.Utils.Constants;

import java.util.List;

/**
 * Created by acer on 9/8/2016.
 */


public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.ViewHolder> {

    private Context mContext;
    private Resources resources;
    private List<Salary> mDataList;


    public SalaryAdapter(Context context, List<Salary> list) {
        mContext = context;
        resources = context.getResources();
        mDataList = list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_salary, parent, false);

        // create ViewHolder
        return new ViewHolder(itemLayoutView);
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSalaryAmountName, tvSalaryDateName, tvSalaryMonthName;
        CardView eachCard;

        public ViewHolder(View itemLayoutView) {

            super(itemLayoutView);
            eachCard = itemLayoutView.findViewById(R.id.eachCard);
            tvSalaryAmountName = itemLayoutView.findViewById(R.id.tvSalaryAmountName);
            tvSalaryDateName = itemLayoutView.findViewById(R.id.tvSalaryDateName);
            tvSalaryMonthName = itemLayoutView.findViewById(R.id.tvSalaryMonthName);
            eachCard.setOnClickListener(view -> {
                Intent intent;
                intent = new Intent(mContext, SalaryDetailActivity.class);
                intent.putExtra(Constants.SALARY, mDataList.get(getAdapterPosition()));
                mContext.startActivity(intent);
            });
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Salary salary = mDataList.get(position);
        holder.tvSalaryAmountName.setText(salary.getAmount());
        holder.tvSalaryDateName.setText(salary.getDate());
        holder.tvSalaryMonthName.setText(salary.getMonth());
    }

}

