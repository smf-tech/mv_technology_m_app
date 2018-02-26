package com.mv.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.IndicatorTrainingFeedBackTaskList;
import com.mv.Activity.PiachartActivity;
import com.mv.Activity.ProcessApprovalActivity;
import com.mv.Activity.ProcessListActivity;
import com.mv.Activity.TeamManagementUserProfileListActivity;
import com.mv.Activity.TemplatesActivity;
import com.mv.ActivityMenu.ProgrammeManagmentFragment;
import com.mv.Model.CalenderEvent;
import com.mv.Model.OverAllModel;
import com.mv.Model.PiaChartModel;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.List;

/**
 * Created by nanostuffs on 23-02-2018.
 */

public class OverallReportAdapter extends RecyclerView.Adapter<OverallReportAdapter.MyViewHolder> {

    private List<OverAllModel> teplateList;
    private Activity mContext;

    private PreferenceHelper preferenceHelper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName,txt_targeted_date,txt_targeted_count;
        public LinearLayout layout;
        ImageView arraow;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtTemplateName);
            txt_targeted_date = (TextView) view.findViewById(R.id.txt_traget_date);
            txt_targeted_count = (TextView) view.findViewById(R.id.txt_traget_count);
            arraow = (ImageView) view.findViewById(R.id.row_img);
            layout = (LinearLayout) view.findViewById(R.id.layoutTemplate);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
           /*         if (mContext instanceof TemplatesActivity)
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
                    }*/

                }
            });
        }
    }


    public OverallReportAdapter(List<OverAllModel> moviesList, Activity context) {
        this.teplateList = moviesList;
        this.mContext = context;
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public OverallReportAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_programe, parent, false);

        return new OverallReportAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OverallReportAdapter.MyViewHolder holder, int position) {
        Log.d("Position",""+position);
        OverAllModel template = teplateList.get(position);
        holder.arraow.setVisibility(View.GONE);
        holder.txtCommunityName.setText(template.getTalukaName());
        if(template.getExpectedCount()!=null)
            holder.txt_targeted_date.setText("Expected Count : "+template.getExpectedCount());
        else
            holder.txt_targeted_date.setText("Expected Count : "+"N/A");

        holder.txt_targeted_count.setText("Submitted Count : "+template.getSubmittedCount());
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }


}