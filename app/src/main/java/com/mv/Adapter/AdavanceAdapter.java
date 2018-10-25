

package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Activity.AdavanceListActivity;
import com.mv.Activity.UserAdavanceListActivity;
import com.mv.Model.Adavance;
import com.mv.R;

import java.util.List;

/**
 * Created by acer on 9/8/2016.
 */


public class AdavanceAdapter extends RecyclerView.Adapter<AdavanceAdapter.ViewHolder> {

    private Context mContext;
    private Resources resources;
    private List<Adavance> mDataList;
    private AdavanceListActivity mActivity;
    private UserAdavanceListActivity userAdavanceListActivity;

    public AdavanceAdapter(Context context, List<Adavance> list) {
        mContext = context;
        resources = context.getResources();
        mDataList = list;
        if (context instanceof AdavanceListActivity)
            mActivity = (AdavanceListActivity) context;
        else if (context instanceof UserAdavanceListActivity)
            userAdavanceListActivity = (UserAdavanceListActivity) context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_adavance, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvProjectName, tvDateName, tvAmountName;
        ImageView imgEdit, imgDelete;
        View view;

        public ViewHolder(View itemLayoutView) {

            super(itemLayoutView);

            imgEdit = itemLayoutView.findViewById(R.id.imgEdit);
            imgDelete = itemLayoutView.findViewById(R.id.imgDelete);
            view = itemLayoutView.findViewById(R.id.view1);
            tvProjectName = itemLayoutView.findViewById(R.id.tvProjectName);
            tvDateName = itemLayoutView.findViewById(R.id.tvDateName);
            tvAmountName = itemLayoutView.findViewById(R.id.tvAmountName);
            imgEdit.setOnClickListener(view -> {
                if (mContext instanceof AdavanceListActivity)
                    mActivity.editAdavance(mDataList.get(getAdapterPosition()));
                else if (mContext instanceof UserAdavanceListActivity)
                    userAdavanceListActivity.changeStatus(getAdapterPosition(), userAdavanceListActivity.mAction);
            });
            imgDelete.setOnClickListener(view -> {
                    if (mContext instanceof AdavanceListActivity)
                        showLogoutPopUp(getAdapterPosition());
                    else if (mContext instanceof UserAdavanceListActivity)
                        userAdavanceListActivity.changeStatus(getAdapterPosition(), "Rejected");
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
        alertDialog.setButton2(mContext.getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
            // Write your code here to execute after dialog closed
          /*  listOfWrongQuestions.add(mPosition);
            prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
        });
        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), (dialog, which) -> {
                mActivity.deleteAdavance(mDataList.get(postion));
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Adavance adavance = mDataList.get(position);
        holder.tvProjectName.setText(adavance.getDecription());
        holder.tvDateName.setText(adavance.getDate());
        holder.tvAmountName.setText("₹ " + adavance.getAmount());
        if (adavance.getStatus().equalsIgnoreCase("Pending")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.purple));
        } else if (adavance.getStatus().equalsIgnoreCase("Approved")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        } else if (adavance.getStatus().equalsIgnoreCase("Verified")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
        } else if (adavance.getStatus().equalsIgnoreCase("Rejected")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        } else if (adavance.getStatus().equalsIgnoreCase("Paid")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.colorPink));
        }
        if (mContext instanceof AdavanceListActivity) {
            holder.imgDelete.setImageResource(R.drawable.form_delete);
            holder.imgEdit.setImageResource(R.drawable.ic_form);
        } else {
            holder.imgDelete.setImageResource(R.drawable.ic_reject);
            holder.imgEdit.setImageResource(R.drawable.ic_approve);
        }
    }

}

