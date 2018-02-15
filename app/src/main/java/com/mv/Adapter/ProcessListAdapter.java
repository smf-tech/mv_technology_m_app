package com.mv.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.mv.Activity.ProcessDeatailActivity;
import com.mv.Activity.ProcessListActivity;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanostuffs on 26-09-2017.
 */

public class ProcessListAdapter extends RecyclerView.Adapter<ProcessListAdapter.MyViewHolder> {

    private final List<TaskContainerModel> resultList;
    ArrayList<ArrayList<Task>> taskArrayList = new ArrayList<>();
    PreferenceHelper preferenceHelper;
    private Activity mContext;

    Gson gson;
    Type listType;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName, textViewColor;
        public LinearLayout layout, deleteLay;
        ImageView deleteRecord;


        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtTemplateName);
            textViewColor = (TextView) view.findViewById(R.id.temp_color);
            layout = (LinearLayout) view.findViewById(R.id.layoutTemplate);
            deleteLay = (LinearLayout) view.findViewById(R.id.lay_delete);
            deleteRecord = (ImageView) view.findViewById(R.id.row_img);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (taskArrayList.size() > 0) {
                        preferenceHelper.insertString(Constants.UNIQUE, resultList.get(getAdapterPosition()).getUnique_Id());
                        if (preferenceHelper.getBoolean(Constants.IS_LOCATION)) {
                            preferenceHelper.insertBoolean(Constants.NEW_PROCESS, false);
                            Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskArrayList.get(getAdapterPosition()));
                            //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                            mContext.startActivity(openClass);
                            mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        } else {
                            preferenceHelper.insertBoolean(Constants.NEW_PROCESS, false);
                            Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskArrayList.get(getAdapterPosition()));
                            //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                            mContext.startActivity(openClass);
                            mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        }

                    } else {


                        Utills.showToast("No Task Available ", mContext);
                    }

                }
            });


        }
    }


    public ProcessListAdapter(List<TaskContainerModel> resultList, Activity context) {
        this.resultList = resultList;

            this.mContext =  context;
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

        //Task template = tasks.get(0);
        Log.d("pos", String.valueOf(position));
        holder.txtCommunityName.setText(Utills.getDate(Long.valueOf(tasks.get(0).getTimestamp__c()), "dd/MM/yyyy hh:mm:ss.SSS"));
        if (tasks.get(0).getIsSave().equals("false")) {
            if(tasks.get(0).getIsApproved__c().equals("false"))
            holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            else
                holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.green));
            holder.deleteRecord.setImageResource(R.drawable.arrow);
        } else {
            holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.red));
            holder.deleteRecord.setImageResource(R.drawable.form_delete);
            holder.deleteLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showLogoutPopUp(position);
                }
            });
        }
        //  holder.eventUserName.setText(String.valueOf(position));
    }

    @Override
    public int getItemCount() {
        return resultList.size();
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
            }
        });
        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                AppDatabase.getAppDatabase(mContext).userDao().deleteSingleTask(resultList.get(postion).getUnique_Id(), resultList.get(postion).getMV_Process__c());
                ( (ProcessListActivity) mContext).getAllProcessData();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}