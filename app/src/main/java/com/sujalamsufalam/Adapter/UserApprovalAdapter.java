package com.sujalamsufalam.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sujalamsufalam.Activity.TeamManagementUserProfileListActivity;
import com.sujalamsufalam.Activity.UserApproveDetail;
import com.sujalamsufalam.Model.Template;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;

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
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Template userApproval = mDataList.get(position);
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
            layoutMain = itemLayoutView.findViewById(R.id.layoutMain);
            txtName = itemLayoutView.findViewById(R.id.txtName);

            layoutMain.setOnClickListener(v -> {
                Intent intent=new Intent(mContext, UserApproveDetail.class);
                intent.putExtra(Constants.ID, mDataList.get(getAdapterPosition()).getId());
                mContext.startActivity(intent);
            });
        }

    }
}
