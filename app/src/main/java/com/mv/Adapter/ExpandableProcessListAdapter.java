package com.mv.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.ProcessListActivity;
import com.mv.ActivityMenu.ProgrammeManagmentFragment;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableProcessListAdapter extends BaseExpandableListAdapter {
    private PreferenceHelper preferenceHelper;
    private ProgrammeManagmentFragment _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Template>> _listDataChild;

    public ExpandableProcessListAdapter(Activity context, ArrayList<String> listDataHeader,
                                        HashMap<String, List<Template>> listChildData) {
        this._context = (ProgrammeManagmentFragment) context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;

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

        final Template template = (Template) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater != null ? infalInflater.inflate(R.layout.each_programe, null) : null;
        }


        TextView txtCommunityName, txt_targeted_date, txt_targeted_count, expectedCount, submittedCount;
        LinearLayout layout;

        txtCommunityName = convertView.findViewById(R.id.txtTemplateName);
        txt_targeted_date = convertView.findViewById(R.id.txt_traget_date);
        expectedCount = convertView.findViewById(R.id.txt_expected_count);
        submittedCount = convertView.findViewById(R.id.txt_submmited_date);
        txt_targeted_count = convertView.findViewById(R.id.txt_traget_count);
        layout = convertView.findViewById(R.id.layoutTemplate);
        layout.setOnClickListener(view -> {

            preferenceHelper.insertBoolean(Constants.IS_EDITABLE, template.getIs_Editable__c());
            preferenceHelper.insertBoolean(Constants.IS_LOCATION, template.getLocation());
            preferenceHelper.insertBoolean(Constants.IS_MULTIPLE, template.getIs_Multiple_Entry_Allowed__c());
            preferenceHelper.insertString(Constants.STATE_LOCATION_LEVEL, template.getLocationLevel());

            Intent openClass = new Intent(_context, ProcessListActivity.class);
            openClass.putExtra(Constants.PROCESS_ID, template.getId());
            openClass.putExtra(Constants.PROCESS_NAME, template.getName());
            _context.startActivity(openClass);
            _context.overridePendingTransition(R.anim.right_in, R.anim.left_out);

        });


        txtCommunityName.setText(template.getName());
        if (template.getTargated_Date__c() != null)
            txt_targeted_date.setText("Target Date : " + template.getTargated_Date__c());
        else
            txt_targeted_date.setText("Target Date : " + "N/A");

        txt_targeted_count.setText("Total Count : " + template.getAnswerCount());
        submittedCount.setText("Submitted Count : " + template.getSubmittedCount());
        expectedCount.setText("Expected Count : " + template.getExpectedCount());
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this._listDataChild.get(this._listDataHeader.get(groupPosition)) != null)
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .size();
        else
            return 0;
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
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater != null ? infalInflater.inflate(R.layout.list_group, null) : null;
        }
        ImageView imgGroup = convertView.findViewById(R.id.imgGroup);

        if (isExpanded) {
            imgGroup.setImageResource(R.drawable.downarrow);
        } else {
            imgGroup.setImageResource(R.drawable.rightarrow);
        }
        TextView txtName = convertView
                .findViewById(R.id.txtName);
        // date.setTypeface(null, Typeface.BOLD);
        txtName.setText(headerTitle);
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
