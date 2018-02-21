package com.mv.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mv.Activity.EventUserListActivity;
import com.mv.Activity.ProcessDeatailActivity;
import com.mv.Model.EventUser;
import com.mv.R;

import java.util.List;

/**
 * Created by nanostuffs on 03-02-2018.
 */

public class EventUserListAdapter extends RecyclerView.Adapter<EventUserListAdapter.MyViewHolder> {

    private List<EventUser> eventUserList;
    private Activity mContext;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView eventUserName,eventUserRole;
        public CheckBox checkBox;


        public MyViewHolder(View view) {
            super(view);
            eventUserName = (TextView) view.findViewById(R.id.tv_event_user_name);
            eventUserRole = (TextView) view.findViewById(R.id.tv_event_user_role);
            checkBox= (CheckBox) view.findViewById(R.id.cb_event_user_cb);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                      //  taskList.get(getAdapterPosition()).setTask_Response__c("true");
                        eventUserList.get(getAdapterPosition()).setUserSelected(true);

                     ((EventUserListActivity) mContext).saveDataToList(eventUserList.get(getAdapterPosition()),true);
                    } else {
                      //  taskList.get(getAdapterPosition()).setTask_Response__c("false");
                        eventUserList.get(getAdapterPosition()).setUserSelected(false);
                        ((EventUserListActivity) mContext).saveDataToList(eventUserList.get(getAdapterPosition()),false);
                    }
                }
            });
        }
    }


    public EventUserListAdapter(List<EventUser> ebentUserList, Activity context) {
        this.eventUserList = ebentUserList;
        this.mContext = context;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_event_user, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.eventUserName.setText(eventUserList.get(position).getUserName());
        holder.eventUserRole.setText(eventUserList.get(position).getRole());
        holder.checkBox.setChecked(eventUserList.get(position).getUserSelected());
    }

    @Override
    public int getItemCount() {
        return eventUserList.size();
    }








}