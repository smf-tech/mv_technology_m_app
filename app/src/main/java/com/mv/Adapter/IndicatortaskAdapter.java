package com.mv.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.PiachartActivity;
import com.mv.Model.Task;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanostuffs on 29-11-2017.
 */

public class IndicatortaskAdapter extends RecyclerView.Adapter<IndicatortaskAdapter.MyViewHolder> {


    private Activity mContext;
    List<Task> indicatortaskList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtTemplateName);
            layout = (LinearLayout) view.findViewById(R.id.layoutTemplate);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent openClass = new Intent(mContext, PiachartActivity.class);
                    openClass.putExtra(Constants.TITLE,indicatortaskList.get(getAdapterPosition()).getTask_Text__c());
                    openClass.putExtra(Constants.INDICATOR_TASK,indicatortaskList.get(getAdapterPosition()));
                    mContext.startActivity(openClass);
                    mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
            });
        }
    }


    public IndicatortaskAdapter(List<Task> moviesList, Activity context) {
        this.indicatortaskList = moviesList;
        this.mContext = context;
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public IndicatortaskAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_template, parent, false);

        return new IndicatortaskAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(IndicatortaskAdapter.MyViewHolder holder, int position) {

        holder.txtCommunityName.setText(indicatortaskList.get(position).getTask_Text__c());
    }

    @Override
    public int getItemCount() {
        return indicatortaskList.size();
    }








}