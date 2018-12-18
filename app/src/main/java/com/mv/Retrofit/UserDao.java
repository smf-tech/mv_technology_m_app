package com.mv.Retrofit;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;
import android.arch.persistence.room.Update;

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
import com.mv.Utils.Constants;

import java.util.List;

/**
 * Created by Rohit Gujar on 23-10-2017.
 */

@Dao
public interface UserDao {


    @Query("SELECT * FROM " + Constants.TABLE_HOLIDAY)
    List<HolidayListModel> getAllHolidayList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAllHolidayList(List<HolidayListModel> holidayListModels);


    @Query("DELETE FROM " + Constants.TABLE_HOLIDAY)
    void deleteHolidayList();

    @Query("SELECT * FROM " + Constants.TABLE_ATTENDANCE)
    List<Attendance> getAllAttendance();

    @Query("SELECT * FROM " + Constants.TABLE_ATTENDANCE + " where status__c = :status ")
    List<Attendance> getRequiredAttendance(String status);

    @Query("SELECT * FROM " + Constants.TABLE_ATTENDANCE + " where Synch = 'false' ORDER BY unique_Id DESC")
    Attendance getUnSynchAttendance();

    @Delete
    void deleteAttendance(Attendance... attendance);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAllAttendance(List<Attendance> attendances);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAttendance(Attendance... attendance);

    @Query("DELETE FROM " + Constants.TABLE_ATTENDANCE)
    void deleteAllAttendance();

    @Update
    void updateAttendance(Attendance... attendance);

    @Query("SELECT * FROM " + Constants.TABLE_VOUCHER)
    List<Voucher> getAllVoucher();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertVoucher(Voucher... vouchers);

    @Delete
    void deleteVoucher(Voucher... vouchers);

    @Query("SELECT * FROM " + Constants.TABLE_ADAVANCE)
    List<Adavance> getAllAdavance();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAdavance(Adavance... adavances);

    @Delete
    void deleteAdavance(Adavance... adavances);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAdavance(List<Adavance> adavances);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertVoucher(List<Voucher> vouchers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertExpense(List<Expense> expenses);

    @Query("DELETE FROM " + Constants.TABLE_ADAVANCE)
    void deleteAllAdavance();

    @Query("DELETE FROM " + Constants.TABLE_VOUCHER)
    void deleteAllVoucher();

    @Query("SELECT * FROM " + Constants.TABLE_SALARY)
    List<Salary> getAllSalary();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertSalary(List<Salary> salaries);

    @Query("DELETE FROM " + Constants.TABLE_SALARY)
    void deleteAllSalary();

    @Query("SELECT * FROM " + Constants.TABLE_EXPENSE)
    List<Expense> getAllExpense();

    @Query("SELECT * FROM " + Constants.TABLE_EXPENSE + " where voucherId = :voucherId")
    List<Expense> getAllExpense(String voucherId);

    @Query("SELECT * FROM " + Constants.TABLE_EXPENSE + " where voucherId = :voucherId AND Status = :status")
    List<Expense> getAllExpense(String voucherId, String status);

    @Query("SELECT * FROM " + Constants.TABLE_ADAVANCE + " where voucherId = :voucherId AND Status = :status")
    List<Adavance> getAllAdvance(String voucherId, String status);

    @Query("SELECT unique_Id FROM " + Constants.TABLE_VOUCHER + " ORDER BY unique_Id DESC LIMIT 1")
    int getIdofLastVoucher();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertExpense(Expense... expenses);

    @Query("DELETE FROM " + Constants.TABLE_EXPENSE + " where voucherId = :voucherId")
    int deleteExpense(String voucherId);

    @Delete
    void deleteExpense(Expense... expenses);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertCalendr(List<CalenderEvent> tasks);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertTask(List<TaskContainerModel> tasks);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertLoaction(List<LocationModel> locationModels);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLocation(LocationModel task);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTask(TaskContainerModel task);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProcess(List<Template> tasks);

    @Update
    void updateTask(TaskContainerModel... task);

    @Update
    void updateCommunities(Community... communities);

    @Query("SELECT * FROM " + Constants.TABLE_DOWNLOAD_CONTENT + " where Lang = :lang")
    List<DownloadContent> getDownloadContent(String lang);

    @Query("SELECT DISTINCT Section FROM " + Constants.TABLE_DOWNLOAD_CONTENT + " where Lang = :lang")
    List<String> getDistinctDownloadContent(String lang);


    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT DISTINCT District,State FROM " + Constants.TABLE_LOCATION + " where Taluka != '' AND State != ''")
    List<LocationModel> getDistinctDistrict();

    @Query("SELECT *  FROM " + Constants.TABLE_DOWNLOAD_CONTENT + " where Section = :section AND Lang = :lang")
    List<DownloadContent> getDownloadContent(String section, String lang);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertDownloadContent(List<DownloadContent> tasks);

    @Insert
    void insertCommunities(Community... communities);

    @Query("SELECT * FROM " + Constants.TABLE_CONTAINER + " where  MV_Process__c = :processId AND TaskType = :questionType")
    TaskContainerModel getQuestion(String processId, String questionType);

    @Query("DELETE FROM  " + Constants.TABLE_CONTAINER + " where  MV_Process__c = :processId AND TaskType = :questionType")
    int deleteQuestion(String processId, String questionType);

    @Query("DELETE FROM " + Constants.TABLE_CONTAINER + " where  MV_Process__c = :processId AND isSave = :issave")
    void deleteTask(String issave, String processId);

    @Query("DELETE FROM " + Constants.TABLE_CONTAINER + " where  unique_Id = :uniqueId AND  MV_Process__c = :processId")
    void deleteSingleTask(String uniqueId, String processId);

    @Query("DELETE FROM " + Constants.TABLE_PROCESS)
    void deleteTable();


    @Query("DELETE FROM  " + Constants.TABLE_CONTENT + " where  Id = :postId")
    int deletePost(String postId);

    @Insert
    long[] insertChats(Content... contents);

    @Update
    void updateChats(Content content);

    @Update
    void updateContent(Content... contents);

    @Delete
    void DeleteContent(Content... contents);


    @Query("SELECT * FROM " + Constants.TABLE_CONTAINER + " where MV_Process__c = :processId AND  TaskType = :questionType ORDER BY taskTimeStamp DESC")
    List<TaskContainerModel> getTask(String processId, String questionType);

    @Query("SELECT * FROM " + Constants.TABLE_CONTAINER + " where unique_Id = :uniqueId ")
    TaskContainerModel getTaskByUniqueId(String uniqueId);

    @Query("SELECT count(*) FROM " + Constants.TABLE_CONTAINER + " where TaskType = :questionType AND  isSave = :isSave")
    int getOfflineTaskCount(String questionType, String isSave);

    @Query("SELECT * FROM " + Constants.TABLE_PROCESS)
    List<Template> getProcess();

    @Query("SELECT * FROM " + Constants.TABLE_PROCESS + " where Category__c = :catagory")
    List<Template> getProcessCatagry(String catagory);

    @Query("SELECT * FROM " + Constants.TABLE_LOCATION)
    List<LocationModel> getLocation();

    @Query("SELECT * FROM " + Constants.TABLE_LOCATION + " where District = :district")
    List<LocationModel> getLocationOfDistrict(String district);

    @Query("SELECT * FROM " + Constants.TABLE_CALANDER + " where Date__c = :date")
    List<CalenderEvent> getCalenderList(String date);

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId AND  (isActive = :flag AND isDelete =:deleteflag) order by CreatedDate desc")
    List<Content> getAllChats(String communityId, Boolean flag, Boolean deleteflag);

    /* @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId  order by CreatedDate desc")
     List<Content> getAllChats(String communityId);
   @Query("SELECT * FROM " + Constants.TABLE_CONTENT)
     List<Content> getAllChats();*/
    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId  order by CreatedDate desc")
    List<Content> getAllChats(String communityId);
    //String strSQL = "UPDATE myTable SET Column1 = someValue WHERE columnId = "+ someValue;

    @Query("SELECT CreatedDate FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId  order by CreatedDate asc")
    String getLastChatTime(String communityId);

    @Query("SELECT CreatedDate FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId AND UserId = :userid order by CreatedDate asc")
    String getLastMyChatTime(String communityId,String userid);

    //String strSQL = "UPDATE myTable SET Column1 = someValue WHERE columnId = "+ someValue;

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where isBroadcast = 'true' AND  isActive = :flag order by CreatedDate desc")
    List<Content> getAllBroadcastChats(Boolean flag);

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where isBroadcast = 'true'  order by CreatedDate desc")
    List<Content> getAllBroadcastChats();

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where isTheatMessage = 'true'  order by CreatedDate desc")
    List<Content> getThetSavandChats();

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where isTheatMessage = 'true' AND  (isActive = :flag AND isDelete =:deleteflag) order by CreatedDate desc")
    List<Content> getThetSavandChats(Boolean flag, Boolean deleteflag);

    @Query("SELECT * FROM " + Constants.TABLE_COMMUNITY + " order by timestamp desc")
    List<Community> getAllCommunities();

    @Query("SELECT * FROM " + Constants.TABLE_COMMUNITY + " where community_id = :communityId")
    Community getCommunityForMute(String communityId);

    @Update
    void updateCommunityForMute(Community... community);

    @Query("SELECT count(*) FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId")
    int getCommunitySize(String communityId);

    @Query("SELECT DISTINCT State FROM " + Constants.TABLE_LOCATION)
    List<String> getState();

    @Query("SELECT DISTINCT District FROM " + Constants.TABLE_LOCATION + " where State = :state  ORDER BY District ASC")
    List<String> getDistrict(String state);

    @Query("SELECT DISTINCT Taluka FROM " + Constants.TABLE_LOCATION + " where State = :state AND District = :district ORDER BY Taluka ASC")
    List<String> getTaluka(String state, String district);

    @Query("SELECT DISTINCT Cluster FROM " + Constants.TABLE_LOCATION + " where State = :state AND District = :district AND Taluka = :taluka  ORDER BY Cluster ASC")
    List<String> getCluster(String state, String district, String taluka);

    @Query("SELECT DISTINCT Village FROM " + Constants.TABLE_LOCATION + " where State = :state AND " +
            "District = :district AND Taluka = :taluka AND Cluster = :cluster ORDER BY Village ASC")
    List<String> getVillage(String state, String district, String taluka, String cluster);

    @Query("SELECT DISTINCT SchoolName FROM " + Constants.TABLE_LOCATION + " where State = :state AND " +
            "District = :district AND Taluka = :taluka AND Cluster = :cluster AND Village = :village")
    List<String> getSchoolName(String state, String district, String taluka, String cluster, String village);

    @Query("SELECT DISTINCT SchoolCode FROM " + Constants.TABLE_LOCATION + " where State = :state AND " +
            "District = :district AND Taluka = :taluka AND Cluster = :cluster AND Village = :village" +
            " AND schoolName = :schoolname"
    )
    List<String> getSchoolCode(String state, String district, String taluka, String cluster, String village, String schoolname);

    @Query("DELETE FROM " + Constants.TABLE_CALANDER)
    void deleteCalender();

    @Query("DELETE FROM " + Constants.TABLE_COMMUNITY)
    void clearTableCommunity();

    @Query("DELETE FROM " + Constants.TABLE_CONTENT)
    void clearTableCotent();

    @Query("DELETE FROM " + Constants.TABLE_PROCESS)
    void clearProcessTable();

    @Query("DELETE FROM " + Constants.TABLE_CONTAINER)
    void clearTaskContainer();

    @Query("DELETE FROM " + Constants.TABLE_DOWNLOAD_CONTENT)
    void clearDownloadContent();

    @Query("DELETE FROM " + Constants.TABLE_LOCATION)
    void clearLocation();

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where synchStatus = '" + Constants.STATUS_LOCAL + "' order by CreatedDate desc")
    List<Content> getAllASynchChats();

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId  and ( Priority = :str or  Report_Type =:str or Issue_Type =:str) order by CreatedDate desc")
    List<Content> getAllChatsfilter(String communityId, String str);

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId  and Issue_Type =:str order by CreatedDate desc")
    List<Content> getHoChatsfilter(String communityId, String str);

    /* @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId  and ( Priority = :str or  Issue_Type =:str) and (UserId = :UserId) order by CreatedDate desc")
     List<Content> getMyChatsfilter(String communityId, String str, String UserId);
     */
    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId  and (UserId = :UserId) and ( Priority = :str or  Report_Type =:str) order by CreatedDate desc")
    List<Content> getMyChatsfilter(String communityId, String UserId, String str);


    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId  and ( Priority = :str or  Report_Type =:str) and (taluka = :taluka) order by CreatedDate desc")
    List<Content> getMyLocationChatsfilter(String communityId, String str, String taluka);

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId  and (Report_Type =:str or Priority = :str) and (taluka != :taluka) order by CreatedDate desc")
    List<Content> getOtherChatsfilter(String communityId, String str, String taluka);

    @Query("DELETE FROM " + Constants.TABLE_CONTENT + " where Id = :contentId and isActive = :value")
    int spampost(String contentId, Boolean value);

    @Query("DELETE FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId AND  (isActive = :flag OR isDelete =:deleteflag)")
    int deletepost(String communityId, Boolean flag, Boolean deleteflag);

    @Query("DELETE FROM " + Constants.TABLE_CALANDER + " where Id = :calenderId")
    int deleteCalenderEvent(String calenderId);

    //Opretions on Notification table

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNotification(Notifications notification);

    @Query("SELECT * FROM " + Constants.TABLE_NOTIFICATION )
    List<Notifications> getAllNotification();

    @Query("SELECT * FROM " + Constants.TABLE_NOTIFICATION + " WHERE Status = :status")
    List<Notifications> getUnRearNotifications(String status);

    @Query("SELECT COUNT(Status) FROM " + Constants.TABLE_NOTIFICATION + " WHERE Status = :status")
    int getUnRearNotificationsCount(String status);

    @Update
    void updateNotification(Notifications notification);

    @Query("DELETE FROM " + Constants.TABLE_NOTIFICATION + " where unique_Id = :unique_Id")
    void deleteNotification(int unique_Id);

    @Query("DELETE FROM " + Constants.TABLE_NOTIFICATION)
    void clearNotification();

    //Opretions on Leaves table

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLeaves(List<LeavesModel> leaves);

    @Query("SELECT * FROM " + Constants.TABLE_LEAVES )
    List<LeavesModel> getAllLeaves();

    @Query("SELECT * FROM " + Constants.TABLE_LEAVES + " where Status__c = :status ")
    List<LeavesModel> getApprovedLeaves(String status);

    @Query("DELETE FROM " + Constants.TABLE_LEAVES)
    void deleteAllLeaves();

}