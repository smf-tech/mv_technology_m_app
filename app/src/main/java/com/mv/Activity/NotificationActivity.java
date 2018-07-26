package com.mv.Activity;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.Adapter.CommentAdapter;
import com.mv.Adapter.NotificatioAdapter;
import com.mv.Model.Notifications;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.databinding.ActivityNotificationBinding;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_notification);
        binding.setActivity(this);


        List<Notifications> notificationList= AppDatabase.getAppDatabase(this).userDao().getAllNotification();

        NotificatioAdapter adapter = new NotificatioAdapter(this, notificationList);
        binding.rvNotification.setAdapter(adapter);
        binding.rvNotification.setHasFixedSize(true);
        binding.rvNotification.setLayoutManager(new LinearLayoutManager(this));

        //mark all notification as read onens open this activity
        List<Notifications> unRearNotificationList=AppDatabase.getAppDatabase(this).userDao().getUnRearNotifications("unread");
        for(Notifications obj:unRearNotificationList){
            obj.setStatus("read");
            AppDatabase.getAppDatabase(this).userDao().updateNotification(obj);
        }

        TextView title=(TextView)findViewById(R.id.toolbar_title);
        title.setText("Notification");
        ImageView back=(ImageView)findViewById(R.id.img_back);
        ImageView img=(ImageView)findViewById(R.id.img_logout);
        img.setVisibility(View.GONE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
