package com.mv.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.OverallReportActivity;
import com.mv.Activity.PiachartActivity;
import com.mv.Model.DashaBoardListModel;
import com.mv.Model.Task;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.List;

public class IndicatorTaskAdapter extends RecyclerView.Adapter<IndicatorTaskAdapter.MyViewHolder> {

    private Activity mContext;
    private List<Task> indicatorTaskList;
    private PreferenceHelper preferenceHelper;
    private DashaBoardListModel moviesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView txtCommunityName;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);

            txtCommunityName = view.findViewById(R.id.txtTemplateName);
            layout = view.findViewById(R.id.layoutTemplate);
            layout.setOnClickListener(view1 -> {
                if (getAdapterPosition() == 0) {
                    Intent openClass = new Intent(mContext, OverallReportActivity.class);
                    openClass.putExtra(Constants.INDICATOR_TASK_ROLE, moviesList.getMultiple_Role__c());
                    openClass.putExtra(Constants.TITLE, indicatorTaskList.get(getAdapterPosition()).getSection_Name__c());
                    openClass.putExtra(Constants.INDICATOR_TASK, indicatorTaskList.get(getAdapterPosition()));
                    openClass.putExtra(Constants.PROCESS_ID, moviesList.getId());

                    preferenceHelper.insertString(Constants.RoleList, moviesList.getMultiple_Role__c());
                    mContext.startActivity(openClass);
                    mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                } else {
                    Intent openClass = new Intent(mContext, PiachartActivity.class);
                    openClass.putExtra(Constants.TITLE, indicatorTaskList.get(getAdapterPosition()).getSection_Name__c());
                    openClass.putExtra(Constants.INDICATOR_TASK, indicatorTaskList.get(getAdapterPosition()));
                    openClass.putExtra(Constants.INDICATOR_TASK_ROLE, moviesList.getMultiple_Role__c());

                    preferenceHelper.insertString(Constants.RoleList, moviesList.getMultiple_Role__c());
                    mContext.startActivity(openClass);
                    mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
            });
        }
    }

    public IndicatorTaskAdapter(DashaBoardListModel moviesList, Activity context) {
        this.indicatorTaskList = moviesList.getTasksList();
        this.moviesList = moviesList;
        this.mContext = context;
        this.preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_template, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.txtCommunityName.setText(indicatorTaskList.get(position).getSection_Name__c());
    }

    @Override
    public int getItemCount() {
        return indicatorTaskList.size();
    }
}