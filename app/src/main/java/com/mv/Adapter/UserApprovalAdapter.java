package com.mv.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.ProcessListApproval;
import com.mv.Activity.TeamManagementUserProfileListActivity;
import com.mv.Activity.UserApproveDetail;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Utils.Constants;

import java.util.List;

/**
 * Created by user on 9/3/2018.
 */
public class UserApprovalAdapter extends RecyclerView.Adapter<UserApprovalAdapter.ViewHolder> {

    private final Context mContext;
    private List<Template> mDataList;
    private String approval_type;

    public UserApprovalAdapter(Context context, List<Template> chatList, String approval_type) {
        mContext = context;
        this.mDataList = chatList;
        this.approval_type = approval_type;
    }

    @Override
    public UserApprovalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_child_leave_application, parent, false);

        // create ViewHolder
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Template userApproval = mDataList.get(position);
        if (userApproval.getName() != null)
            holder.txtName.setText(String.format("%s", userApproval.getName()));
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
            layoutMain = itemLayoutView.findViewById(R.id.layoutMain);
            txtName = itemLayoutView.findViewById(R.id.txtName);

            layoutMain.setOnClickListener(v -> {
                if (approval_type.equals(Constants.USER_APPROVAL)) {
                    Intent intent = new Intent(mContext, UserApproveDetail.class);
                    intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                    mContext.startActivity(intent);
                } else if (approval_type.equals(Constants.PROCESS_APPROVAL)) {
                    Intent intent = new Intent(mContext, ProcessListApproval.class);
                    intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                    intent.putExtra(Constants.PROCESS_ID, TeamManagementUserProfileListActivity.id);
                    intent.putExtra(Constants.PROCESS_NAME, TeamManagementUserProfileListActivity.processTitle);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
