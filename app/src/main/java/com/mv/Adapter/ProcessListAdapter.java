package com.mv.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mv.Activity.ProcessDeatailActivity;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.R;
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

    private final List<TaskContainerModel>resultList;
    ArrayList<ArrayList<Task>> taskArrayList=new ArrayList<>();
    PreferenceHelper preferenceHelper;
    private Activity mContext;
    Gson gson;
    Type listType;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName,textViewColor;
        public LinearLayout layout;


        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtTemplateName);
            textViewColor = (TextView) view.findViewById(R.id.temp_color);
            layout = (LinearLayout) view.findViewById(R.id.layoutTemplate);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(taskArrayList.size()>0) {
                        preferenceHelper.insertString(Constants.UNIQUE,resultList.get(getAdapterPosition()).getUnique_Id());
                        if(preferenceHelper.getBoolean(Constants.IS_LOCATION)) {
                            preferenceHelper.insertBoolean(Constants.NEW_PROCESS, false);
                            Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskArrayList.get(getAdapterPosition()));
                            //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                            mContext.startActivity(openClass);
                            mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        }
                        else
                        {
                            preferenceHelper.insertBoolean(Constants.NEW_PROCESS, false);
                            Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                            openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskArrayList.get(getAdapterPosition()));
                            //  openClass.putExtra("stock_list", resultList.get(getAdapterPosition()).get(0));
                            mContext.startActivity(openClass);
                            mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        }

                    }
                    else
                    {
                        Utills.showToast("No Task Available ",mContext);
                    }

                }
            });
        }
    }


    public ProcessListAdapter(List<TaskContainerModel>  resultList, Activity context) {
        this.resultList = resultList;
        this.mContext = context;
        this.preferenceHelper = new PreferenceHelper(context);
         gson = new Gson();
     listType = new TypeToken<ArrayList<Task>>() {}.getType();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_template, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        ArrayList<Task> tasks = gson.fromJson(resultList.get(position).getTaskListString(), listType);
        taskArrayList.add(tasks);

        //Task template = tasks.get(0);
        Log.d("pos",String.valueOf(position));
        holder.txtCommunityName.setText(Utills.getDate(Long.valueOf(tasks.get(0).getTimestamp__c()),"dd/MM/yyyy hh:mm:ss.SSS"));
      if(tasks.get(0).getIsSave().equals("false"))
       holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.green));
         else
             holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.red));
      //  holder.txtCommunityName.setText(String.valueOf(position));
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }
}