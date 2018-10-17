package com.sujalamsufalam.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sujalamsufalam.Activity.OverallReportActivity;
import com.sujalamsufalam.Activity.PiachartActivity;
import com.sujalamsufalam.Model.DashaBoardListModel;
import com.sujalamsufalam.Model.LocationModel;
import com.sujalamsufalam.Model.Task;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanostuffs on 29-11-2017.
 */

public class IndicatortaskAdapter extends RecyclerView.Adapter<IndicatortaskAdapter.MyViewHolder> {


    private Activity mContext;
    List<Task> indicatortaskList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
    private DashaBoardListModel moviesList;
    LocationModel locationModel;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtTemplateName);
            layout = (LinearLayout) view.findViewById(R.id.layoutTemplate);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAdapterPosition()==0)
                    {
                        Intent openClass = new Intent(mContext, OverallReportActivity.class);
                        openClass.putExtra(Constants.INDICATOR_TASK_ROLE, moviesList.getMultiple_Role__c());
                        preferenceHelper.insertString(Constants.RoleList,moviesList.getMultiple_Role__c());
                        openClass.putExtra(Constants.TITLE, indicatortaskList.get(getAdapterPosition()).getSection_Name__c());
                        openClass.putExtra(Constants.INDICATOR_TASK, indicatortaskList.get(getAdapterPosition()));
                        openClass.putExtra(Constants.PROCESS_ID, moviesList.getId());
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }else {
                        Intent openClass = new Intent(mContext, PiachartActivity.class);
                        openClass.putExtra(Constants.TITLE, indicatortaskList.get(getAdapterPosition()).getSection_Name__c());
                        openClass.putExtra(Constants.INDICATOR_TASK, indicatortaskList.get(getAdapterPosition()));
                        openClass.putExtra(Constants.INDICATOR_TASK_ROLE, moviesList.getMultiple_Role__c());
                        preferenceHelper.insertString(Constants.RoleList,moviesList.getMultiple_Role__c());
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                }
            });
        }
    }


    public IndicatortaskAdapter(DashaBoardListModel moviesList, Activity context) {
        this.indicatortaskList = moviesList.getTasksList();
        this.moviesList=moviesList;
        this.mContext = context;
        this.locationModel=locationModel;
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_template, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.txtCommunityName.setText(indicatortaskList.get(position).getSection_Name__c());
    }

    @Override
    public int getItemCount() {
        return indicatortaskList.size();
    }








}