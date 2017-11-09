package com.mv.Adapter;

/**
 * Created by Rohit Gujar on 13-09-2017.
 */


import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.GroupsActivity;
import com.mv.Activity.HomeActivity;
import com.mv.Fragment.GroupsFragment;
import com.mv.Model.Community;
import com.mv.R;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {

    private List<Community> communitiesList;
    private Context mContext;
    private GroupsFragment fragment;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtCommunityName);
            txtCommunityName.setSelected(true);
            layout = (LinearLayout) view.findViewById(R.id.layoutGroup);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.onLayoutGroupClick(getAdapterPosition());
                }
            });
        }
    }


    public GroupAdapter(List<Community> moviesList, Context context, GroupsFragment fragment) {
        this.communitiesList = moviesList;
        this.mContext = context;
        this.fragment = fragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_group, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Community community = communitiesList.get(position);
        holder.txtCommunityName.setText(community.getName());

    }

    @Override
    public int getItemCount() {
        return communitiesList.size();
    }
}