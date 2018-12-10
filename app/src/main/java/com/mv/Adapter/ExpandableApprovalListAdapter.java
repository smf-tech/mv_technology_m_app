package com.mv.Adapter;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.LeaveApprovalActivity;
import com.mv.Activity.LeaveDetailActivity;
import com.mv.Model.LeavesModel;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nanostuffs on 19-03-2018.
 */

public class ExpandableApprovalListAdapter extends BaseExpandableListAdapter {
    private PreferenceHelper preferenceHelper;
    private LeaveApprovalActivity _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, ArrayList<LeavesModel>> _listDataChild;

    public ExpandableApprovalListAdapter(Activity context, ArrayList<String> listDataHeader,
                                         HashMap<String, ArrayList<LeavesModel>> listChildData, String tabName) {
        this._context = (LeaveApprovalActivity) context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        LeaveApprovalActivity _activity = (LeaveApprovalActivity) context;
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final LeavesModel leavesModel = (LeavesModel) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater != null ? infalInflater.inflate(R.layout.each_child_leave_application, null) : null;
        }

        TextView txtCount, txtName;
        RelativeLayout layoutMain;

        if (convertView != null) {
            layoutMain = convertView.findViewById(R.id.layoutMain);
            txtCount = convertView.findViewById(R.id.txtCount);

            layoutMain.setOnClickListener(v -> {
                Intent intent = new Intent(_context, LeaveDetailActivity.class);
                intent.putExtra(Constants.Leave, leavesModel);
                _context.startActivity(intent);
            });

            ImageView imgDownload = convertView.findViewById(R.id.imgDownload);
            if (groupPosition == 0 && !preferenceHelper.getString(Constants.Leave).equals(Constants.Leave_Approve)) {
                imgDownload.setVisibility(View.VISIBLE);
                imgDownload.setOnClickListener(view -> showDeleteDialog(leavesModel.getId()));
            } else {
                imgDownload.setVisibility(View.GONE);
            }

            txtName = convertView.findViewById(R.id.txtName);
            txtCount.setVisibility(View.GONE);

            if (leavesModel.getRequested_User_Name__c() != null) {
                txtName.setText(String.format("%s(%s : %s)", leavesModel.getRequested_User_Name__c(),
                        leavesModel.getFromDate(), leavesModel.getToDate()));
            } else {
                txtName.setText(String.format("%s : %s : %s", leavesModel.getFromDate(),
                        leavesModel.getToDate(), leavesModel.getTypeOfLeaves()));
            }
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this._listDataChild.get(this._listDataHeader.get(groupPosition)) != null) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
        } else {
            return 0;
        }
    }



    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater != null ? infalInflater.inflate(R.layout.list_group, null) : null;
        }

        if (convertView != null) {

            ImageView imgGroup = convertView.findViewById(R.id.imgGroup);
            if (imgGroup != null) {
                if (isExpanded) {
                    imgGroup.setImageResource(R.drawable.downarrow);
                } else {
                    imgGroup.setImageResource(R.drawable.rightarrow);
                }
            }

            String headerTitle = (String) getGroup(groupPosition);
            TextView txtName = convertView.findViewById(R.id.txtName);
            txtName.setText(headerTitle);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private void showDeleteDialog(String id) {
        final AlertDialog alertDialog = new AlertDialog.Builder(_context).create();

        // Setting Dialog Title
        alertDialog.setTitle(_context.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(_context.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting CANCEL Button
        alertDialog.setButton2(_context.getString(R.string.cancel), (dialog, which) -> alertDialog.dismiss());
        // Setting OK Button
        alertDialog.setButton(_context.getString(R.string.ok), (dialog, which) -> _context.deleteLeave(id));

        // Showing Alert Message
        alertDialog.show();
    }


}
