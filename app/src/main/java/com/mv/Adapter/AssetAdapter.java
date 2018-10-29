package com.mv.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.AssetAllocatedListActivity;
import com.mv.Activity.AssetAllocation_Activity;
import com.mv.Activity.AssetApprovalActivity;
import com.mv.Model.Asset;
import com.mv.Model.User;
import com.mv.R;

import java.util.List;

/**
 * Created by user on 3/15/2018.
 */

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {
    private List<Asset> assetList;
    private Activity mContext;
    private AssetAllocatedListActivity activity;

    public AssetAdapter(List<Asset> assetList, Activity context) {
        this.assetList = assetList;
        this.mContext = context;
        if (context instanceof AssetAllocatedListActivity)
            activity = (AssetAllocatedListActivity) context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_asset, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (assetList.get(position).getAssetModel().equalsIgnoreCase("null")) {
            holder.tvProjectDateTitle.setVisibility(View.GONE);
            holder.txt_asset_id.setVisibility(View.GONE);
        } else {
            holder.txt_asset_id.setText(assetList.get(position).getAssetModel());
            holder.txt_asset_id.setVisibility(View.VISIBLE);
            holder.tvProjectDateTitle.setVisibility(View.VISIBLE);
        }
        holder.txt_asset_issue_date.setText(assetList.get(position).getExpectedIssueDate());
        holder.txt_asset_name.setText(assetList.get(position).getAssetName());
        if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Requested")) {
            holder.view1.setBackgroundColor(mContext.getResources().getColor(R.color.purple));
            //     Intent intent = new Intent(mContext)
        } else if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Accepted")) {
            holder.view1.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        } else if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Allocated")) {
            holder.view1.setBackgroundColor(mContext.getResources().getColor(R.color.orrange2));
        } else if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Rejected")) {
            holder.view1.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        } else if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Released")) {
            holder.view1.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
        }
        if (assetList.get(position).getAllocationStatus().equalsIgnoreCase("Requested")
                && !(User.getCurrentUser(mContext).getMvUser().getRoll().equalsIgnoreCase("Asset Manager"))) {
            holder.imgLayout.setVisibility(View.VISIBLE);
        } else {
            holder.imgLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView tvProjectDateTitle, txt_asset_name, txt_asset_id, txt_asset_issue_date;
        public View view1;
        public LinearLayout imgLayout;
        ImageView imgEdit, imgDelete;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            imgEdit = itemLayoutView.findViewById(R.id.imgEdit);
            imgDelete = itemLayoutView.findViewById(R.id.imgDelete);
            cardView = itemLayoutView.findViewById(R.id.cardView);
            txt_asset_name = itemLayoutView.findViewById(R.id.txt_asset_name);
            tvProjectDateTitle = itemLayoutView.findViewById(R.id.tvProjectDateTitle);
            txt_asset_id = itemLayoutView.findViewById(R.id.txt_asset_id);
            txt_asset_issue_date = itemLayoutView.findViewById(R.id.txt_asset_issue_date);
            view1 = itemLayoutView.findViewById(R.id.view1);
            imgLayout = itemLayoutView.findViewById(R.id.imgLayout);
            cardView.setOnClickListener((view)-> {
                    if (assetList.get(getAdapterPosition()).getAllocationStatus().equalsIgnoreCase("Allocated")
                            && !(User.getCurrentUser(mContext).getMvUser().getRoll().equalsIgnoreCase("Asset Manager"))) {
                        Intent intent = new Intent(mContext, AssetApprovalActivity.class);
                        intent.putExtra("Assets", assetList.get(getAdapterPosition()));
                        mContext.startActivity(intent);
                    } else if (assetList.get(getAdapterPosition()).getAllocationStatus().equalsIgnoreCase("Requested")
                            && User.getCurrentUser(mContext).getMvUser().getRoll().equalsIgnoreCase("Asset Manager")) {
                        Intent intent = new Intent(mContext, AssetAllocation_Activity.class);
                        intent.putExtra("Assets", assetList.get(getAdapterPosition()));
                        mContext.startActivity(intent);
                    } else if (assetList.get(getAdapterPosition()).getAllocationStatus().equalsIgnoreCase("Accepted")
                            && !(User.getCurrentUser(mContext).getMvUser().getRoll().equalsIgnoreCase("Asset Manager"))
                            ) {
                        Intent intent = new Intent(mContext, AssetAllocation_Activity.class);
                        intent.putExtra("Assets", assetList.get(getAdapterPosition()));
                        mContext.startActivity(intent);
                    }
            });
            imgDelete.setOnClickListener((view)-> {
                    if (activity != null)
                        showLogoutPopUp(getAdapterPosition());
            });
            imgEdit.setOnClickListener((view)-> {
                    if (activity != null)
                        activity.editExpense(assetList.get(getAdapterPosition()));
            });
        }

    }

    private void showLogoutPopUp(final int postion) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(android.R.string.cancel),(dialog, which)-> {
                alertDialog.dismiss();
                // Write your code here to execute after dialog closed
              /*  listOfWrongQuestions.add(mPosition);
                prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
        });
        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), (dialog, which)-> {
                activity.deleteExpense(assetList.get(postion));
        });

        // Showing Alert Message
        alertDialog.show();
    }
}