package com.sujalamsufalam.Retrofit;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.sujalamsufalam.Model.Adavance;
import com.sujalamsufalam.Model.Attendance;
import com.sujalamsufalam.Model.CalenderEvent;
import com.sujalamsufalam.Model.Community;
import com.sujalamsufalam.Model.Content;
import com.sujalamsufalam.Model.DownloadContent;
import com.sujalamsufalam.Model.Expense;
import com.sujalamsufalam.Model.HolidayListModel;
import com.sujalamsufalam.Model.LeavesModel;
import com.sujalamsufalam.Model.LocationModel;
import com.sujalamsufalam.Model.Notifications;
import com.sujalamsufalam.Model.Salary;
import com.sujalamsufalam.Model.TaskContainerModel;
import com.sujalamsufalam.Model.Template;
import com.sujalamsufalam.Model.Voucher;


/**
 * Created by Rohit Gujar on 23-10-2017.
 */

@Database(entities = {Community.class, Content.class, Template.class, TaskContainerModel.class,
        LocationModel.class, CalenderEvent.class, DownloadContent.class, Voucher.class,
        Expense.class, Adavance.class, Salary.class, Attendance.class, HolidayListModel.class, Notifications.class, LeavesModel.class}, version = 36)


public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "user-database")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}