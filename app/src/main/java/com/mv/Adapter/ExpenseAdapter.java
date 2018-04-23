

package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Activity.ExpenseListActivity;
import com.mv.Activity.UserExpenseListActivity;
import com.mv.Model.Expense;
import com.mv.R;

import java.util.List;

/**
 * Created by acer on 9/8/2016.
 */


public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private Context mContext;
    private Resources resources;
    private List<Expense> mDataList;
    private ExpenseListActivity mActivity;

    public ExpenseAdapter(Context context, List<Expense> list) {
        mContext = context;
        resources = context.getResources();
        mDataList = list;
        if (context instanceof ExpenseListActivity)
            mActivity = (ExpenseListActivity) context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_expense, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvProjectName, tvDateName, tvNoOfPeopleName;
        ImageView imgEdit, imgDelete;
        View view;
        CardView cardView;

        public ViewHolder(View itemLayoutView) {

            super(itemLayoutView);

            imgEdit = (ImageView) itemLayoutView.findViewById(R.id.imgEdit);
            imgDelete = (ImageView) itemLayoutView.findViewById(R.id.imgDelete);
            tvProjectName = (TextView) itemLayoutView.findViewById(R.id.tvProjectName);
            tvDateName = (TextView) itemLayoutView.findViewById(R.id.tvDateName);
            tvNoOfPeopleName = (TextView) itemLayoutView.findViewById(R.id.tvNoOfPeopleName);
            imgEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mContext instanceof ExpenseListActivity)
                        mActivity.editExpense(getAdapterPosition());

                }
            });
            view = itemLayoutView.findViewById(R.id.view1);
            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mContext instanceof ExpenseListActivity)
                        showLogoutPopUp(getAdapterPosition());
                }
            });
            cardView = (CardView) itemLayoutView.findViewById(R.id.cardView);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mContext instanceof UserExpenseListActivity) {

                    }
                }
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
        alertDialog.setButton2(mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                // Write your code here to execute after dialog closed
              /*  listOfWrongQuestions.add(mPosition);
                prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
            }
        });
        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mActivity.deleteExpense(postion);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Expense expense = mDataList.get(position);
        holder.tvProjectName.setText(expense.getPartuculars());
        holder.tvDateName.setText(expense.getDate());
        holder.tvNoOfPeopleName.setText("₹ "+expense.getAmount());
        if (expense.getStatus().equalsIgnoreCase("Pending")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.purple));
        } else if (expense.getStatus().equalsIgnoreCase("Approved")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        } else if (expense.getStatus().equalsIgnoreCase("Verified")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.orrange2));
        } else if (expense.getStatus().equalsIgnoreCase("Rejected")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        } else if (expense.getStatus().equalsIgnoreCase("Paid")) {
            holder.view.setBackgroundColor(mContext.getResources().getColor(R.color.colorPink));
        }
        if (mContext instanceof ExpenseListActivity) {
            holder.imgDelete.setImageResource(R.drawable.form_delete);
            holder.imgEdit.setImageResource(R.drawable.ic_form);
        } else {
            holder.imgDelete.setImageResource(R.drawable.ic_reject);
            holder.imgEdit.setImageResource(R.drawable.ic_approve);
        }
    }

}

