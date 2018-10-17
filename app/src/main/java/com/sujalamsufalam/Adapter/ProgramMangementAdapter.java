package com.sujalamsufalam.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sujalamsufalam.Activity.IndicatorTrainingFeedBackTaskList;
import com.sujalamsufalam.Activity.PiachartActivity;
import com.sujalamsufalam.Activity.ProcessApprovalActivity;
import com.sujalamsufalam.Activity.ProcessListActivity;
import com.sujalamsufalam.Activity.TeamManagementUserProfileListActivity;
import com.sujalamsufalam.Activity.TemplatesActivity;
import com.sujalamsufalam.ActivityMenu.ProgrammeManagmentFragment;
import com.sujalamsufalam.Model.Template;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;

import java.util.List;

/**
 * Created by nanostuffs on 13-02-2018.
 */

public class ProgramMangementAdapter  extends RecyclerView.Adapter<ProgramMangementAdapter.MyViewHolder> {

    private List<Template> teplateList;
    private Activity mContext;

    private PreferenceHelper preferenceHelper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName,txt_targeted_date,txt_targeted_count,expectedCount,submittedCount;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtTemplateName);
            txt_targeted_date = (TextView) view.findViewById(R.id.txt_traget_date);
            expectedCount = (TextView) view.findViewById(R.id.txt_expected_count);
            submittedCount = (TextView) view.findViewById(R.id.txt_submmited_date);
            txt_targeted_count = (TextView) view.findViewById(R.id.txt_traget_count);
            layout = (LinearLayout) view.findViewById(R.id.layoutTemplate);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mContext instanceof TemplatesActivity)
                        ((TemplatesActivity) mContext).onLayoutTemplateClick(getAdapterPosition());

                    else if (mContext instanceof ProgrammeManagmentFragment) {


                        preferenceHelper.insertBoolean(Constants.IS_EDITABLE, teplateList.get(getAdapterPosition()).getIs_Editable__c());
                        preferenceHelper.insertBoolean(Constants.IS_LOCATION, teplateList.get(getAdapterPosition()).getLocation());
                        preferenceHelper.insertBoolean(Constants.IS_MULTIPLE, teplateList.get(getAdapterPosition()).getIs_Multiple_Entry_Allowed__c());
                        preferenceHelper.insertString(Constants.STATE_LOCATION_LEVEL, teplateList.get(getAdapterPosition()).getLocationLevel());

                        Intent openClass = new Intent(mContext, ProcessListActivity.class);
                        openClass.putExtra(Constants.PROCESS_ID, teplateList.get(getAdapterPosition()).getId());
                        openClass.putExtra(Constants.PROCESS_NAME, teplateList.get(getAdapterPosition()).getName());
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);

                    } else if (mContext instanceof IndicatorTrainingFeedBackTaskList) {

                        //my reports
                        Intent openClass = new Intent(mContext, PiachartActivity.class);
                        openClass.putExtra(Constants.TITLE, teplateList.get(getAdapterPosition()).getName());
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    } else if (mContext instanceof ProcessApprovalActivity) {
                        //PROCESS Approval
                        Intent openClass = new Intent(mContext, TeamManagementUserProfileListActivity.class);
                        openClass.putExtra(Constants.APPROVAL_TYPE, Constants.PROCESS_APPROVAL);
                        openClass.putExtra(Constants.TITLE, teplateList.get(getAdapterPosition()).getName());
                        openClass.putExtra(Constants.ID, teplateList.get(getAdapterPosition()).getId());
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }

                }
            });
        }
    }


    public ProgramMangementAdapter(List<Template> moviesList, Activity context) {
        this.teplateList = moviesList;
        this.mContext = context;
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_programe, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.d("Position",""+position);
        Template template = teplateList.get(position);
        holder.txtCommunityName.setText(template.getName());
        if(template.getTargated_Date__c()!=null)
        holder.txt_targeted_date.setText("Target Date : "+template.getTargated_Date__c());
        else
            holder.txt_targeted_date.setText("Target Date : "+"N/A");

        holder.txt_targeted_count.setText("Total Count : "+template.getAnswerCount());
        holder.submittedCount.setText("Submitted Count : "+template.getSubmittedCount());
        holder.expectedCount.setText("Expected Count : "+template.getExpectedCount());
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }


}