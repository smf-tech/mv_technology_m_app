package com.mv.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.data.PieEntry;
import com.mv.Activity.HomeActivity;
import com.mv.Activity.ProcessApprovalActivity;
import com.mv.Activity.ProcessListApproval;
import com.mv.Activity.TeamManagementUserProfileListActivity;
import com.mv.Activity.UserApproveDetail;
import com.mv.Model.Task;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanostuffs on 11-01-2018.
 */

public class PichartMenuAdapter extends RecyclerView.Adapter<PichartMenuAdapter.MyViewHolder> {

    private List<PieEntry> teplateList;
    private Activity mContext;

    private PreferenceHelper preferenceHelper;
    List<Integer> colorList;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName,bacColor;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtTemplateName);
            bacColor = (TextView) view.findViewById(R.id.temp_color);
            layout = (LinearLayout) view.findViewById(R.id.layoutTemplate);

        }
    }


    public PichartMenuAdapter(List<PieEntry> moviesList, List<Integer> colorList, Activity context) {
        this.teplateList = moviesList;
        this.mContext = context;
        this.colorList=colorList;
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public PichartMenuAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_menu_color, parent, false);

        return new PichartMenuAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PichartMenuAdapter.MyViewHolder holder, int position) {
        holder.bacColor.setBackgroundColor(colorList.get(position));
        holder.txtCommunityName.setText( teplateList.get(position).getLabel());
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }








}