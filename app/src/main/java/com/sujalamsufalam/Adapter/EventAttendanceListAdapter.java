package com.sujalamsufalam.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sujalamsufalam.Activity.EventUserAttendanceActivity;
import com.sujalamsufalam.Model.EventUser;
import com.sujalamsufalam.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 7/3/2018.
 */

public class EventAttendanceListAdapter extends RecyclerView.Adapter<EventAttendanceListAdapter.MyViewHolder> {

    private List<EventUser> eventAttendanceList;
    private Activity mContext;

    public EventAttendanceListAdapter(List<EventUser> ebentUserList, Activity context) {
        this.eventAttendanceList = ebentUserList;
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

        holder.eventUserName.setText(eventAttendanceList.get(position).getUserName());
        holder.eventUserRole.setText(eventAttendanceList.get(position).getRole());
        holder.checkBox.setChecked(eventAttendanceList.get(position).getUserSelected());


        if(eventAttendanceList.get(position).getUserSelected()){
            holder.checkBox.setChecked(true);
            ((EventUserAttendanceActivity) mContext).saveDataToList(eventAttendanceList.get(position), true);
        } else {
            holder.checkBox.setChecked(false);
            ((EventUserAttendanceActivity) mContext).saveDataToList(eventAttendanceList.get(position), false);
        }
    }

    @Override
    public int getItemCount() {
        return eventAttendanceList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView eventUserName, eventUserRole;
        public CheckBox checkBox;

        public MyViewHolder(View view) {
            super(view);
            eventUserName = view.findViewById(R.id.tv_event_user_name);
            eventUserRole = view.findViewById(R.id.tv_event_user_role);
            checkBox = view.findViewById(R.id.cb_event_user_cb);
            checkBox.setOnClickListener(v -> {
                if (((CheckBox) v).isChecked()) {
                    ((EventUserAttendanceActivity) mContext).saveDataToList(eventAttendanceList.get(getAdapterPosition()), true);
                    eventAttendanceList.get(getAdapterPosition()).setUserSelected(true);
                    ((EventUserAttendanceActivity) mContext).checkAllSelected((ArrayList<EventUser>) eventAttendanceList);
                } else {
                    ((EventUserAttendanceActivity) mContext).saveDataToList(eventAttendanceList.get(getAdapterPosition()), false);
                    eventAttendanceList.get(getAdapterPosition()).setUserSelected(false);
                    ((EventUserAttendanceActivity) mContext).checkAllDeSelected();
                }
            });
        }
    }

}