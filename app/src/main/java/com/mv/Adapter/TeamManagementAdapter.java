package com.mv.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.AttendanceApproval2Activity;
import com.mv.Activity.LeaveApprovalActivity;
import com.mv.Activity.MapsActivity;
import com.mv.Activity.ProcessApprovalActivity;
import com.mv.Activity.ProcessListApproval;
import com.mv.Activity.TeamManagementUserProfileListActivity;
import com.mv.Activity.UserApproveDetail;
import com.mv.Activity.VoucherListActivity;
import com.mv.ActivityMenu.TeamManagementFragment;
import com.mv.Model.Task;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanostuffs on 16-11-2017.
 */

public class TeamManagementAdapter extends RecyclerView.Adapter<TeamManagementAdapter.MyViewHolder> {

    private List<Template> teplateList;
    private Activity mContext;
    ArrayList<Task> programManagementProcessLists = new ArrayList<>();
    private PreferenceHelper preferenceHelper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName, textViewColor;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = view.findViewById(R.id.txtTemplateName);
            layout = view.findViewById(R.id.layoutTemplate);
            textViewColor = view.findViewById(R.id.temp_color);
            layout.setOnClickListener(view1 -> {


                if (mContext instanceof TeamManagementFragment) {
                    switch (getAdapterPosition()) {
                        case 0: {
                            //User Approval
                            Intent openClass = new Intent(mContext, TeamManagementUserProfileListActivity.class);
                            openClass.putExtra(Constants.ID, teplateList.get(getAdapterPosition()).getId());
                            openClass.putExtra(Constants.APPROVAL_TYPE, Constants.USER_APPROVAL);
                            mContext.startActivity(openClass);
                            mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            break;
                        }

                        case 1: {
                            //redirect to user Approval Process List
                            Intent openClass = new Intent(mContext, ProcessApprovalActivity.class);
                            openClass.putExtra(Constants.ID, teplateList.get(getAdapterPosition()).getId());
                            mContext.startActivity(openClass);
                            mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            break;
                        }

                        case 2: {
                            //redirect to user Approval Process List
                            Intent intent = new Intent(mContext, VoucherListActivity.class);
                            Constants.AccountTeamCode = "TeamManagement";//to identify the section
                            mContext.startActivity(intent);
                            break;
                        }

                        case 3: {
                            //redirect to user Approval Process List
                            Intent openClass = new Intent(mContext, LeaveApprovalActivity.class);
                            mContext.startActivity(openClass);
                            preferenceHelper.insertString(Constants.Leave, Constants.Leave_Approve);
                            mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            break;
                        }

                        //adding Attendance approval
                        case 4: {
                            //redirect to user Approval Process List
                            Intent openClass = new Intent(mContext, AttendanceApproval2Activity.class);
                            mContext.startActivity(openClass);
                            preferenceHelper.insertString(Constants.Leave, Constants.Leave_Approve);
                            mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            break;
                        }

                        //adding maps
                        case 5: {// shows the tc on map
                            Intent intent = new Intent(mContext, MapsActivity.class);
                            mContext.startActivity(intent);
                            break;
                        }
                    }
                } else if (mContext instanceof TeamManagementUserProfileListActivity) {
                    if (TeamManagementUserProfileListActivity.approvalType.equals(Constants.USER_APPROVAL)) {
                        Intent openClass = new Intent(mContext, UserApproveDetail.class);
                        openClass.putExtra(Constants.ID, teplateList.get(getAdapterPosition()).getId());
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    } else if (TeamManagementUserProfileListActivity.approvalType.equals(Constants.PROCESS_APPROVAL)) {
                        Intent openClass = new Intent(mContext, ProcessListApproval.class);
                        openClass.putExtra(Constants.ID, teplateList.get(getAdapterPosition()).getId());
                        openClass.putExtra(Constants.PROCESS_ID, TeamManagementUserProfileListActivity.id);
                        openClass.putExtra(Constants.PROCESS_NAME, TeamManagementUserProfileListActivity.processTitle);
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                }
            });
        }
    }

    public TeamManagementAdapter(List<Template> moviesList, Activity context) {
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

        if (mContext instanceof TeamManagementUserProfileListActivity) {
            if (TeamManagementUserProfileListActivity.approvalType.equals(Constants.USER_APPROVAL)) {

                if (template.getStatus() != null) {
                    switch (template.getStatus()) {
                        case "true":
                            holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.green));
                            holder.textViewColor.setVisibility(View.VISIBLE);
                            break;
                        case "false":
                            holder.textViewColor.setVisibility(View.VISIBLE);
                            holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.purple));
                            break;
                        case "Rejected":
                            holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.red));
                            holder.textViewColor.setVisibility(View.VISIBLE);
                            break;
                    }
                    //  else if (template.getStatus().equals("false"))
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }


}