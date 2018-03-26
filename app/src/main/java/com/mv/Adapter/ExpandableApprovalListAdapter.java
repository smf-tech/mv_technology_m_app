package com.mv.Adapter;


import android.app.Activity;
import android.content.Context;

import android.graphics.Typeface;
import android.os.Environment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.mv.Activity.ExpandableListActivity;
import com.mv.Activity.LeaveApprovalActivity;
import com.mv.Model.DownloadContent;
import com.mv.Model.LeavesModel;
import com.mv.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nanostuffs on 19-03-2018.
 */

public class ExpandableApprovalListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, ArrayList<LeavesModel>> _listDataChild;
    private LeaveApprovalActivity _activity;

    public ExpandableApprovalListAdapter(Activity context, ArrayList<String> listDataHeader,
                                         HashMap<String, ArrayList<LeavesModel>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._activity = (LeaveApprovalActivity) context;
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

        final LeavesModel content = (LeavesModel) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.each_trainging, null);
        }

        ImageView imgDownload, imgshare;
        TextView txtCount, txtName;
        RelativeLayout layoutMain;


        layoutMain = (RelativeLayout) convertView.findViewById(R.id.layoutMain);
        txtCount = (TextView) convertView.findViewById(R.id.txtCount);
        layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        imgDownload = (ImageView) convertView.findViewById(R.id.imgDownload);
        imgshare = (ImageView) convertView.findViewById(R.id.imgshare);


        txtName = (TextView) convertView.findViewById(R.id.txtName);

        txtCount.setVisibility(View.GONE);
        txtName.setText(content.getFromDate()+" : " +content.getToDate());

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
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
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
        ImageView imgGroup = (ImageView) convertView.findViewById(R.id.imgGroup);
        if (isExpanded) {
            imgGroup.setImageResource(R.drawable.downarrow);
        } else {
            imgGroup.setImageResource(R.drawable.rightarrow);
        }
        TextView txtName = (TextView) convertView
                .findViewById(R.id.txtName);
        txtName.setTypeface(null, Typeface.BOLD);
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
