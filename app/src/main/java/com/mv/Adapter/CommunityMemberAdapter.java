package com.mv.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mv.R;

import java.util.ArrayList;

/**
 * Created by Nanostuffs on 08-12-2017.
 */

public class CommunityMemberAdapter extends RecyclerView.Adapter<CommunityMemberAdapter.ViewHolder> {
    private static final int LENGTH = 7;

    private ArrayList<String> CommunityMemberList;

    public CommunityMemberAdapter(Context context, ArrayList<String> CommunityMemberList) {
        this.CommunityMemberList = CommunityMemberList;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_community_member, parent, false);

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


        holder.txtCommunityMember.setText((position+1) + " " +CommunityMemberList.get(position));


    }




    @Override
    public int getItemCount() {
        return CommunityMemberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtCommunityMember;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txtCommunityMember = itemLayoutView.findViewById(R.id.txtCommunityMember);

        }


    }


}


