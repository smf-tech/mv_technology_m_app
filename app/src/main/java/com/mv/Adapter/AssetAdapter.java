package com.mv.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.AssetAllocatedListActivity;
import com.mv.Activity.AssetAllocation_Activity;
import com.mv.Activity.AssetApprovalActivity;
import com.mv.Model.Asset;
import com.mv.Model.Content;
import com.mv.Model.Template;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Utils.PreferenceHelper;

import java.util.List;

/**
 * Created by user on 3/15/2018.
 */

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {
    private List<Asset> assetList;
    private Activity mContext;

    public AssetAdapter(List<Asset> assetList, Activity context) {
        this.assetList = assetList;
        this.mContext = context;


    }
    @Override
    public AssetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_asset, parent, false);

        // create ViewHolder
        AssetAdapter.ViewHolder viewHolder = new AssetAdapter.ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (assetList.get(position).getAssetModel().equalsIgnoreCase("null")){
            holder.txt_asset_id.setVisibility(View.GONE);
        }else {

            holder.txt_asset_id.setText(assetList.get(position).getAssetModel());
            holder.txt_asset_id.setVisibility(View.VISIBLE);
        }



        holder.txt_asset_issue_date.setText(assetList.get(position).getExpectedIssueDate());
        holder.txt_asset_name.setText(assetList.get(position).getAssetName());
        if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Requested")){
            holder.txt_status.setBackgroundColor(mContext.getResources().getColor(R.color.purple));
       //     Intent intent = new Intent(mContext)
        }else if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Accepted")){
            holder.txt_status.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        }else if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Allocated")){
            holder.txt_status.setBackgroundColor(mContext.getResources().getColor(R.color.orrange2));
        }else if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Rejected")){
            holder.txt_status.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        }



    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView card_view;
        public  TextView txt_asset_name, txt_asset_id,txt_asset_issue_date,txt_status;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            card_view = (CardView) itemLayoutView.findViewById(R.id.card_view);
            txt_asset_name = (TextView) itemLayoutView.findViewById(R.id.txt_asset_name);
            txt_asset_id = (TextView) itemLayoutView.findViewById(R.id.txt_asset_id);
            txt_asset_issue_date = (TextView) itemLayoutView.findViewById(R.id.txt_asset_issue_date);
            txt_status = (TextView) itemLayoutView.findViewById(R.id.txt_status);
            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (assetList.get(getAdapterPosition()).getAllocationStatus().equalsIgnoreCase("Allocated")){
                        Intent intent = new Intent(mContext, AssetApprovalActivity.class);
                        intent.putExtra("Assets", assetList.get(getAdapterPosition()));
                        mContext.startActivity(intent);
                    }else if (assetList.get(getAdapterPosition()).getAllocationStatus().equalsIgnoreCase("Requested")){
                            Intent intent = new Intent(mContext, AssetAllocation_Activity.class);
                            intent.putExtra("Assets", assetList.get(getAdapterPosition()));
                            mContext.startActivity(intent);

                    }
                }
            });



        }

    }
}