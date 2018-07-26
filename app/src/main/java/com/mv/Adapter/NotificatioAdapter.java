package com.mv.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mv.Activity.NotificationActivity;
import com.mv.Model.Notifications;
import com.mv.R;

import java.util.List;

/**
 * Created by user on 7/25/2018.
 */

public class NotificatioAdapter extends RecyclerView.Adapter<NotificatioAdapter.MyViewHolder> {

    Activity activity;
    List<Notifications> notificationList;

    public NotificatioAdapter(Activity activity, List<Notifications> notificationList) {
        this.notificationList=notificationList;
        this.activity=activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_notification, parent, false);

        // create ViewHolder
        NotificatioAdapter.MyViewHolder viewHolder = new NotificatioAdapter.MyViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tvTitle.setText(notificationList.get(position).getTitle());
        holder.tvDetel.setText(notificationList.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle,tvDetel;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvTitle=(TextView)itemView.findViewById(R.id.tv_title);
            tvDetel=(TextView)itemView.findViewById(R.id.tv_Detels);
        }
    }
}
