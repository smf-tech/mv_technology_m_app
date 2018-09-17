package com.mv.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.AttendanceApproval2Activity;
import com.mv.Activity.AttendanceApproveDetailActivity;
import com.mv.Activity.TeamManagementUserProfileListActivity;
import com.mv.Activity.UserApproveDetail;
import com.mv.Model.AttendanceApproval;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.List;

/**
 * Created by user on 9/3/2018.
 */

public class UserApprovalAdapter extends RecyclerView.Adapter<UserApprovalAdapter.ViewHolder>{
    private final Context mContext;
    private PreferenceHelper preferenceHelper;
    private TeamManagementUserProfileListActivity mActivity;
    private List<Template> mDataList;

    public UserApprovalAdapter(Context context, List<Template> chatList) {
        Resources resources = context.getResources();
        mActivity = (TeamManagementUserProfileListActivity) context;
        mContext = context;
        preferenceHelper = new PreferenceHelper(mContext);
        this.mDataList = chatList;
    }
    @Override
    public UserApprovalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_child_leave_application, parent, false);

        // create ViewHolder
        UserApprovalAdapter.ViewHolder viewHolder = new UserApprovalAdapter.ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Template userApproval = (Template) mDataList.get(position);
        if(userApproval.getName()!=null)
            holder.txtName.setText(userApproval.getName()+"");
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtName;
        RelativeLayout layoutMain;
        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            layoutMain = (RelativeLayout) itemLayoutView.findViewById(R.id.layoutMain);
            txtName = (TextView) itemLayoutView.findViewById(R.id.txtName);

            layoutMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, UserApproveDetail.class);
                    intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                    mContext.startActivity(intent);
                }
            });
        }

    }
}
