package com.sujalamsufalam.Activity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sujalamsufalam.Adapter.NotificatioAdapter;
import com.sujalamsufalam.Model.Notifications;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.AppDatabase;
import com.sujalamsufalam.Utils.LocaleManager;
import com.sujalamsufalam.databinding.ActivityNotificationBinding;

import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_notification);
        binding.setActivity(this);


        List<Notifications> notificationList= AppDatabase.getAppDatabase(this).userDao().getAllNotification();

//        Notifications temp=new Notifications();
//        temp.setStatus("");
//        temp.setTitle("staticc");
//        temp.setDescription("xaxyysyyehjfb");
//        temp.setId("123");
//        notificationList.add(temp);
        Collections.reverse(notificationList);

        if(!(notificationList.size()>0)){
            binding.tvNoData.setVisibility(View.VISIBLE);
            binding.tvNoData.setText(getString(R.string.no_data_available));
        } else {
            binding.tvNoData.setVisibility(View.GONE);
        }

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
        title.setText(getResources().getString(R.string.title_notification));
        ImageView back=(ImageView)findViewById(R.id.img_back);
        ImageView img=(ImageView)findViewById(R.id.img_logout);
        img.setVisibility(View.GONE);
        back.setOnClickListener(v -> finish());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
}
