package com.mv.Adapter;

/**
 * Created by Rohit Gujar on 13-09-2017.
 */


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.ActivityMenu.GroupsFragment;
import com.mv.Model.Community;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {

    private List<Community> communitiesList;
    private Context mContext;
    private GroupsFragment fragment;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName, txtCount;
        public LinearLayout layout;
        public ImageView imgNextArrow;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtCommunityName);
            txtCommunityName.setSelected(true);
            imgNextArrow = (ImageView) view.findViewById(R.id.imgNextArrow);
            txtCount = (TextView) view.findViewById(R.id.txtCount);
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
        holder.txtCount.setText("" + AppDatabase.getAppDatabase(mContext).userDao().getCommunitySize(community.getId()));
        holder.txtCount.setVisibility(View.VISIBLE);
        holder.imgNextArrow.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return communitiesList.size();
    }
}