package com.mv.Adapter;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mv.Activity.AdavanceListActivity;
import com.mv.Model.Adavance;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nanostuffs on 19-03-2018.
 */

public class ExpandableAdvanceListAdapter extends BaseExpandableListAdapter {
    private PreferenceHelper preferenceHelper;
    private AdavanceListActivity _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, ArrayList<Adavance>> _listDataChild;
    private AdavanceListActivity _activity;

    public ExpandableAdvanceListAdapter(Activity context, ArrayList<String> listDataHeader,
                                        HashMap<String, ArrayList<Adavance>> listChildData) {
        this._context = (AdavanceListActivity) context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._activity = (AdavanceListActivity) context;
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

        final Adavance adavance = (Adavance) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.each_adavance, null);
        }
        TextView tvProjectName, tvDateName, tvAmountName;
        ImageView imgEdit, imgDelete;
        RelativeLayout textLayout;
        View view;

        imgEdit = (ImageView) convertView.findViewById(R.id.imgEdit);
        imgDelete = (ImageView) convertView.findViewById(R.id.imgDelete);
        view = convertView.findViewById(R.id.view1);
        tvProjectName = (TextView) convertView.findViewById(R.id.tvProjectName);
        tvDateName = (TextView) convertView.findViewById(R.id.tvDateName);
        tvAmountName = (TextView) convertView.findViewById(R.id.tvAmountName);
        textLayout = (RelativeLayout) convertView.findViewById(R.id.textLayout);

        Log.e("Group", String.valueOf(groupPosition));

        if(groupPosition==1|| groupPosition==2){
            imgEdit.setVisibility(View.GONE);
            imgDelete.setVisibility(View.GONE);
        }

        // hiding views for team mgmt section
        if(Constants.AccountTeamCode.equals("TeamManagement")) {
            imgEdit.setVisibility(View.GONE);
            imgDelete.setVisibility(View.GONE);
        }

            textLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (_context instanceof AdavanceListActivity)
                        _activity.editAdavance(adavance);
                }
            });




        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_context instanceof AdavanceListActivity)
                    _activity.editAdavance(adavance);
            }
        });
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_context instanceof AdavanceListActivity)
                    showLogoutPopUp(adavance);
            }
        });

        imgDelete.setImageResource(R.drawable.form_delete);
        imgEdit.setImageResource(R.drawable.ic_form);
        view.setVisibility(View.GONE);
        tvProjectName.setText(adavance.getDecription());
        tvDateName.setText(adavance.getDate());
        tvAmountName.setText("₹ " + adavance.getAmount());
        return convertView;
    }

    private void showLogoutPopUp(Adavance adavance) {
        final AlertDialog alertDialog = new AlertDialog.Builder(_context).create();

        // Setting Dialog Title
        alertDialog.setTitle(_context.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(_context.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(_context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                // Write your code here to execute after dialog closed
              /*  listOfWrongQuestions.add(mPosition);
                prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
            }
        });
        // Setting OK Button
        alertDialog.setButton(_context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                _activity.deleteAdavance(adavance);
            }
        });

        // Showing Alert Message
        alertDialog.show();
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
