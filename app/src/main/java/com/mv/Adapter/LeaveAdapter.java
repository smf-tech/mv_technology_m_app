package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.AttendanceApproval2Activity;
import com.mv.Activity.AttendanceApproveDetailActivity;
import com.mv.Activity.LeaveApprovalActivity;
import com.mv.Activity.LeaveDetailActivity;
import com.mv.Model.AttendanceApproval;
import com.mv.Model.LeavesModel;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.List;

public class LeaveAdapter extends RecyclerView.Adapter<LeaveAdapter.ViewHolder>{

    private Context mContext;
    private List<LeavesModel> mDataList;
    PreferenceHelper preferenceHelper;
    private LeaveApprovalActivity _context;

    public LeaveAdapter(Context context, List<LeavesModel> chatList) {
        this._context = (LeaveApprovalActivity) context;
        Resources resources = context.getResources();
        LeaveApprovalActivity mActivity = (LeaveApprovalActivity) context;
        mContext = context;
        preferenceHelper = new PreferenceHelper(mContext);
        this.mDataList = chatList;
    }
    @Override
    public LeaveAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_child_leave_application, parent, false);

        // create ViewHolder
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(LeaveAdapter.ViewHolder holder, int position) {

        final LeavesModel leavesModel = mDataList.get(position);
        if(leavesModel.getRequested_User_Name__c()!=null)
            holder.txtName.setText(String.format("%s(%s : %s)", leavesModel.getRequested_User_Name__c(),
                leavesModel.getFromDate(), leavesModel.getToDate()));
        else
            holder.txtName.setText(String.format("%s : %s : %s", leavesModel.getFromDate(),
                    leavesModel.getToDate(), leavesModel.getTypeOfLeaves()));
        if(leavesModel.getStatus().equalsIgnoreCase("Rejected")||
                leavesModel.getStatus().equalsIgnoreCase("Approved")){
            holder.imgDownload.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtName;
        RelativeLayout layoutMain;
        ImageView imgDownload;
        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            layoutMain = itemLayoutView.findViewById(R.id.layoutMain);
            txtName = itemLayoutView.findViewById(R.id.txtName);
            imgDownload = itemLayoutView.findViewById(R.id.imgDownload);

            layoutMain.setOnClickListener((View v)-> {
                Intent intent = new Intent(mContext, LeaveDetailActivity.class);
                intent.putExtra(Constants.Leave, mDataList.get(getAdapterPosition()));
                mContext.startActivity(intent);
            });

            if (!preferenceHelper.getString(Constants.Leave).equals(Constants.Leave_Approve)) {
                imgDownload.setVisibility(View.VISIBLE);
                imgDownload.setOnClickListener(view -> showDeleteDialog(mDataList.get(getAdapterPosition()).getId()));
            } else {
                imgDownload.setVisibility(View.GONE);
            }
        }

    }

    private void showDeleteDialog(String id) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(R.string.cancel), (dialog, which) -> alertDialog.dismiss());
        // Setting OK Button
        alertDialog.setButton(mContext.getString(R.string.ok), (dialog, which) -> _context.deleteLeave(id));

        // Showing Alert Message
        alertDialog.show();
    }

    }

