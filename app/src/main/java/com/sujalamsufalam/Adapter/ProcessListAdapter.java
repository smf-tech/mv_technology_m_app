package com.sujalamsufalam.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sujalamsufalam.Activity.ProcessDeatailActivity;
import com.sujalamsufalam.Activity.ProcessListActivity;
import com.sujalamsufalam.Model.Task;
import com.sujalamsufalam.Model.TaskContainerModel;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.AppDatabase;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanostuffs on 26-09-2017.
 */
public class ProcessListAdapter extends RecyclerView.Adapter<ProcessListAdapter.MyViewHolder> {

    private final List<TaskContainerModel> resultList;
    private ArrayList<ArrayList<Task>> taskArrayList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
    private ProcessListActivity mContext;

    private Gson gson;
    private Type listType;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtCommunityName, textViewColor;
        LinearLayout layout;
        LinearLayout arrowLay;
        ImageView arrowImg;

        public MyViewHolder(View view) {
            super(view);

            txtCommunityName = view.findViewById(R.id.txtTemplateName);
            textViewColor = view.findViewById(R.id.temp_color);
            layout = view.findViewById(R.id.layoutTemplate);
            arrowLay = view.findViewById(R.id.lay_delete);
            arrowImg = view.findViewById(R.id.row_img);

            layout.setOnClickListener(view1 -> {
                if (taskArrayList.size() > 0) {
                    preferenceHelper.insertString(Constants.UNIQUE, resultList.size() > getAdapterPosition() ?
                            resultList.get(getAdapterPosition()).getUnique_Id() : "");

                    if (preferenceHelper.getBoolean(Constants.IS_LOCATION)) {
                        preferenceHelper.insertBoolean(Constants.NEW_PROCESS, false);

                        Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                        openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskArrayList.get(getAdapterPosition()));

                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    } else {
                        preferenceHelper.insertBoolean(Constants.NEW_PROCESS, false);

                        Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                        openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskArrayList.get(getAdapterPosition()));

                        mContext.startActivity(openClass);
                        mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    }
                } else {
                    Utills.showToast("No Task Available ", mContext);
                }
            });
        }
    }

    public ProcessListAdapter(List<TaskContainerModel> resultList, Activity context) {
        this.resultList = resultList;
        this.mContext = (ProcessListActivity) context;
        this.preferenceHelper = new PreferenceHelper(context);

        gson = new Gson();

        listType = new TypeToken<ArrayList<Task>>() {
        }.getType();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_template, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        ArrayList<Task> tasks = gson.fromJson(resultList.get(position).getTaskListString(), listType);
        taskArrayList.add(tasks);

        Log.d("pos", String.valueOf(position));
        if (resultList.get(position).getHeaderPosition().equals("")) {
            holder.txtCommunityName.setText(Utills.getDate(Long.valueOf(tasks.get(0).getTimestamp__c()), "dd/MM/yyyy hh:mm:ss.SSS"));
        } else {
            holder.txtCommunityName.setText(resultList.get(position).getHeaderPosition());
        }

        if (tasks.get(0).getIsSave().equals("false")) {
            if (tasks.get(0).getIsApproved__c().equals("false")) {
                holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            } else {
                if (tasks.get(0).getStatus__c() != null && tasks.get(0).getStatus__c().equalsIgnoreCase("Expected")) {
                    holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.purple));
                } else {
                    holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.green));
                }
            }

            if (!resultList.get(position).getIsDeleteAllow()) {
                holder.arrowLay.setVisibility(View.GONE);
            } else {
                holder.arrowImg.setBackgroundResource(R.drawable.form_delete);
                holder.arrowLay.setOnClickListener(v -> showFormDeletePopUp(position));
            }
        } else {
            holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.red));
            holder.arrowImg.setBackgroundResource(R.drawable.form_delete);
            holder.arrowLay.setOnClickListener(v -> showFormDeletePopUp(position));
        }
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    @SuppressWarnings("deprecation")
    private void showFormDeletePopUp(final int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_launcher);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(android.R.string.cancel), (dialog, which) -> alertDialog.dismiss());

        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), (dialog, which) -> {
            if (resultList != null && resultList.size() > 0) {
                if (resultList.get(0).getIsSave().equals("false")) {
                    mContext.deleteForm(resultList.get(position));
                } else {
                    AppDatabase.getAppDatabase(mContext).userDao().deleteSingleTask(resultList.get(position).getUnique_Id(),
                            resultList.get(position).getMV_Process__c());
                    mContext.getAllProcessData();
                }
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}