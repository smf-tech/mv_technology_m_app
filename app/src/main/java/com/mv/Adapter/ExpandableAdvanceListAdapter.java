package com.mv.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.AdavanceListActivity;
import com.mv.Model.Adavance;
import com.mv.R;
import com.mv.Utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableAdvanceListAdapter extends BaseExpandableListAdapter {

    private AdavanceListActivity context;
    private List<String> listDataHeader;

    // child data in format of header title, child title
    private HashMap<String, ArrayList<Adavance>> listDataChild;
    private AdavanceListActivity activity;

    public ExpandableAdvanceListAdapter(Activity context, ArrayList<String> listDataHeader,
                                        HashMap<String, ArrayList<Adavance>> listChildData) {

        this.context = (AdavanceListActivity) context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        this.activity = (AdavanceListActivity) context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Adavance adavance = (Adavance) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater != null ? layoutInflater.inflate(R.layout.each_adavance, null) : null;
        }

        TextView tvProjectName, tvDateName, tvAmountName, tvAmount;
        ImageView imgEdit, imgDelete;
        RelativeLayout textLayout;
        View view;

        if (convertView != null) {
            imgEdit = convertView.findViewById(R.id.imgEdit);
            imgDelete = convertView.findViewById(R.id.imgDelete);
            view = convertView.findViewById(R.id.view1);
            tvProjectName = convertView.findViewById(R.id.tvProjectName);
            tvDateName = convertView.findViewById(R.id.tvDateName);
            tvAmountName = convertView.findViewById(R.id.tvAmountName);
            tvAmount = convertView.findViewById(R.id.tvAmount);
            textLayout = convertView.findViewById(R.id.textLayout);

            Log.e("Group", String.valueOf(groupPosition));

            if (groupPosition == 1 || groupPosition == 2) {
                imgEdit.setVisibility(View.GONE);
                imgDelete.setVisibility(View.GONE);
            }

            // hiding views for team mgmt section
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                imgEdit.setVisibility(View.GONE);
                imgDelete.setVisibility(View.GONE);
            }

            textLayout.setOnClickListener(view13 -> {
                if (context instanceof AdavanceListActivity)
                    activity.editAdavance(adavance);
            });

            imgEdit.setImageResource(R.drawable.ic_form);
            imgEdit.setOnClickListener(view12 -> {
                if (context instanceof AdavanceListActivity)
                    activity.editAdavance(adavance);
            });

            imgDelete.setImageResource(R.drawable.form_delete);
            imgDelete.setOnClickListener(view1 -> {
                if (context instanceof AdavanceListActivity)
                    showLogoutPopUp(adavance);
            });

            view.setVisibility(View.GONE);
            tvProjectName.setText(adavance.getDecription());
            tvDateName.setText(adavance.getDate());

            if (adavance.getStatus().equals("Approved")) {
                tvAmount.setText("Approved Amount: ");
                tvAmountName.setText(String.format("₹ %s", adavance.getApproved_Amount__c()));
            } else {
                tvAmount.setText("Amount: ");
                tvAmountName.setText(String.format("₹ %s", adavance.getAmount()));
            }
        }

        return convertView;
    }

    @SuppressWarnings("deprecation")
    private void showLogoutPopUp(Adavance adavance) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(context.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(context.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(context.getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
        });

        // Setting OK Button
        alertDialog.setButton(context.getString(android.R.string.ok), (dialog, which) -> activity.deleteAdavance(adavance));

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.listDataChild.get(this.listDataHeader.get(groupPosition)) != null) {
            return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater != null ? layoutInflater.inflate(R.layout.list_group, null) : null;
        }

        if (convertView != null) {
            ImageView imgGroup = convertView.findViewById(R.id.imgGroup);

            if (isExpanded) {
                if (imgGroup != null) {
                    imgGroup.setImageResource(R.drawable.downarrow);
                }
            } else {
                if (imgGroup != null) {
                    imgGroup.setImageResource(R.drawable.rightarrow);
                }
            }

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
}