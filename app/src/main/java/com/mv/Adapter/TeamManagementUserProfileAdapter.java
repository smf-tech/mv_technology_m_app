package com.mv.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.HomeActivity;
import com.mv.Activity.TeamManagementUserProfileActivity;
import com.mv.Activity.UserApproveDetail;
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

public class TeamManagementUserProfileAdapter extends RecyclerView.Adapter<TeamManagementUserProfileAdapter.MyViewHolder> {

    private List<Template> teplateList;
    private Activity mContext;
    ArrayList<Task> programManagementProcessLists = new ArrayList<>();
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


                    if(mContext instanceof HomeActivity)
                    {
                        Intent openClass = new Intent(mContext, TeamManagementUserProfileActivity.class);
                        openClass.putExtra(Constants.ID, teplateList.get(getAdapterPosition()).getId());
                        //  openClass.putExtra(Constants.PROCESS_ID, taskList);
                        // openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, Utills.convertStringToArrayList(taskContainerModel.getTaskListString()));
                        //  openClass.putExtra("stock_list", resultList.get(indicatortaskList()).get(0));
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }else if(mContext instanceof TeamManagementUserProfileActivity) {
                        Intent openClass = new Intent(mContext, UserApproveDetail.class);
                        openClass.putExtra(Constants.ID, teplateList.get(getAdapterPosition()).getId());
                        //  openClass.putExtra(Constants.PROCESS_ID, taskList);
                        // openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, Utills.convertStringToArrayList(taskContainerModel.getTaskListString()));
                        //  openClass.putExtra("stock_list", resultList.get(indicatortaskList()).get(0));
                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                }


            });
        }
    }


    public TeamManagementUserProfileAdapter(List<Template> moviesList, Activity context) {
        this.teplateList = moviesList;
        this.mContext = context;
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public TeamManagementUserProfileAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_template, parent, false);

        return new TeamManagementUserProfileAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TeamManagementUserProfileAdapter.MyViewHolder holder, int position) {
        Template template = teplateList.get(position);
        holder.txtCommunityName.setText(template.getName());
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }








}