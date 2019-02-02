package com.mv.Retrofit;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.mv.Model.Adavance;
import com.mv.Model.Attendance;
import com.mv.Model.CalenderEvent;
import com.mv.Model.Community;
import com.mv.Model.Content;
import com.mv.Model.DownloadContent;
import com.mv.Model.Expense;
import com.mv.Model.HolidayListModel;
import com.mv.Model.LeavesModel;
import com.mv.Model.LocationModel;
import com.mv.Model.Notifications;
import com.mv.Model.Salary;
import com.mv.Model.TaskContainerModel;
import com.mv.Model.Template;
import com.mv.Model.Voucher;

@Database(entities = {Community.class, Content.class, Template.class, TaskContainerModel.class,
        LocationModel.class, CalenderEvent.class, DownloadContent.class, Voucher.class,
        Expense.class, Adavance.class, Salary.class, Attendance.class, HolidayListModel.class,
        Notifications.class, LeavesModel.class}, version = 2)


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