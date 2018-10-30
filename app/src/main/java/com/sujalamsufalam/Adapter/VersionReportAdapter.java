package com.sujalamsufalam.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sujalamsufalam.Model.Template;
import com.sujalamsufalam.R;

import java.util.List;

/**
 * Created by nanostuffs on 07-03-2018.
 */

public class VersionReportAdapter extends RecyclerView.Adapter<VersionReportAdapter.MyViewHolder> {

    private List<Template> teplateList;
    private Activity mContext;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtUserName,txtVersionNumber;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtUserName = view.findViewById(R.id.tv_version_name);
            txtVersionNumber = view.findViewById(R.id.tv_version_number);
        }
    }


    public VersionReportAdapter(List<Template> moviesList, Activity context) {
        this.teplateList = moviesList;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_version_report, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Template template = teplateList.get(position);

        holder.txtUserName.setText(template.getName());
        holder.txtVersionNumber.setText(template.getUser_Mobile_App_Version__c());
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }





}