package com.mv.Adapter;

/**
 * Created by Rohit Gujar on 13-09-2017.
 */


import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.IndicatorTrainingFeedBackTaskList;

import com.mv.Activity.PiachartActivity;
import com.mv.Activity.ProcessApprovalActivity;
import com.mv.Activity.ProcessListActivity;
import com.mv.Activity.TeamManagementUserProfileListActivity;
import com.mv.Activity.TemplatesActivity;
import com.mv.ActivityMenu.ProgrammeManagmentFragment;
import com.mv.Model.Template;
import com.mv.R;

import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.List;


public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.MyViewHolder> {

    private List<Template> teplateList;
    private Activity mContext;

    private PreferenceHelper preferenceHelper;

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


    public TemplateAdapter(List<Template> moviesList, Activity context) {
        this.teplateList = moviesList;
        this.mContext = context;
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
        Template template = teplateList.get(position);
        holder.txtCommunityName.setText(template.getName());
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }


}