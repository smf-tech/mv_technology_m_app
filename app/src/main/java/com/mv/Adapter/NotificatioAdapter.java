package com.mv.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Model.Notifications;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;

import java.util.List;

/**
 * Created by user on 7/25/2018.
 */

public class NotificatioAdapter extends RecyclerView.Adapter<NotificatioAdapter.MyViewHolder> {

    private Activity activity;
    private List<Notifications> notificationList;

    public NotificatioAdapter(Activity activity, List<Notifications> notificationList) {
        this.notificationList=notificationList;
        this.activity=activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_notification, parent, false);

        // create ViewHolder
        return new MyViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tvTitle.setText(notificationList.get(position).getTitle());
        holder.tvDetel.setText(notificationList.get(position).getDescription());
        //code for deleting the notification on swap left
        holder.layNotification.setOnTouchListener(new View.OnTouchListener() {
            int downX, upX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = (int) event.getX();
                    Log.i("event.getX()", " downX " + downX);
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    upX = (int) event.getX();
                    Log.i("event.getX()", " upX " + upX);
//                    if (upX - downX > 200) {
//                        // swipe right
//                    }
                //    else
                        if (downX - upX > 50) {
                        // swipe left
//                        Toast.makeText(activity,""+(downX - upX),Toast.LENGTH_SHORT).show();
                        AppDatabase.getAppDatabase(activity).userDao().deleteNotification(notificationList.get(position).getUniqueId());
                        notificationList.remove(position);
                        notifyDataSetChanged();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle,tvDetel;
        RelativeLayout layNotification;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvTitle= itemView.findViewById(R.id.tv_title);
            tvDetel= itemView.findViewById(R.id.tv_Detels);
            layNotification= itemView.findViewById(R.id.lay_notification);
        }
    }
}
