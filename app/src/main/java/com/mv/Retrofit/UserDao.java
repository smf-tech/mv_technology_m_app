package com.mv.Retrofit;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mv.Model.Community;
import com.mv.Model.Content;
import com.mv.Model.LocationModel;
import com.mv.Model.TaskContainerModel;
import com.mv.Model.Template;
import com.mv.Utils.Constants;

import java.util.List;

/**
 * Created by Rohit Gujar on 23-10-2017.
 */

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertTask(List<TaskContainerModel> tasks);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertLoaction(List<LocationModel> locationModels);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTask(TaskContainerModel task);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProcess(List<Template> tasks);

    @Update
    public void updateTask(TaskContainerModel... task);

    @Update
    public void updateCommunities(Community... communities);

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



    @Insert
    void insertChats(Content... contents);

    @Update
    public void updateChats(Content content);

    @Update
    void updateContent(Content... contents);

    @Query("SELECT * FROM " + Constants.TABLE_CONTAINER + " where MV_Process__c = :processId AND  TaskType = :questionType")
    List<TaskContainerModel> getTask(String processId, String questionType);


    @Query("SELECT * FROM " + Constants.TABLE_PROCESS)
    List<Template> getProcess();

    @Query("SELECT * FROM " + Constants.TABLE_LOCATION)
    List<LocationModel> getLocation();

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where CommunityId = :communityId order by CreatedDate desc")
    List<Content> getAllChats(String communityId);

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT)
    List<Content> getAllChats();

    //String strSQL = "UPDATE myTable SET Column1 = someValue WHERE columnId = "+ someValue;


    //String strSQL = "UPDATE myTable SET Column1 = someValue WHERE columnId = "+ someValue;

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where isBroadcast = 'true' order by CreatedDate desc")
    List<Content> getAllBroadcastChats();

    @Query("SELECT * FROM " + Constants.TABLE_COMMUNITY + " order by timestamp desc")
    List<Community> getAllCommunities();

    @Query("SELECT DISTINCT State FROM " + Constants.TABLE_LOCATION)
    List<String> getState();

    @Query("SELECT DISTINCT District FROM " + Constants.TABLE_LOCATION + " where State = :state")
    List<String> getDistrict(String state);

    @Query("SELECT DISTINCT Taluka FROM " + Constants.TABLE_LOCATION + " where State = :state AND District = :district")
    List<String> getTaluka(String state, String district);

    @Query("SELECT DISTINCT Cluster FROM " + Constants.TABLE_LOCATION + " where State = :state AND District = :district AND Taluka = :taluka")
    List<String> getCluster(String state, String district, String taluka);

    @Query("SELECT DISTINCT Village FROM " + Constants.TABLE_LOCATION + " where State = :state AND " +
            "District = :district AND Taluka = :taluka AND Cluster = :cluster")
    List<String> getVillage(String state, String district, String taluka, String cluster);

    @Query("SELECT DISTINCT SchoolName FROM " + Constants.TABLE_LOCATION + " where State = :state AND " +
            "District = :district AND Taluka = :taluka AND Cluster = :cluster AND Village = :village")
    List<String> getSchoolName(String state, String district, String taluka, String cluster, String village);

    @Query("SELECT DISTINCT SchoolCode FROM " + Constants.TABLE_LOCATION + " where State = :state AND " +
            "District = :district AND Taluka = :taluka AND Cluster = :cluster AND Village = :village" +
            " AND schoolName = :schoolname"
    )
    List<String> getSchoolCode(String state, String district, String taluka, String cluster, String village, String schoolname);


    @Query("DELETE FROM " + Constants.TABLE_COMMUNITY)
    public void clearTableCommunity();

    @Query("DELETE FROM " + Constants.TABLE_CONTENT)
    public void clearTableCotent();

    @Query("DELETE FROM " + Constants.TABLE_PROCESS)
    public void clearProcessTable();

    @Query("DELETE FROM " + Constants.TABLE_CONTAINER)
    public void clearTaskContainer();

    @Query("SELECT * FROM " + Constants.TABLE_CONTENT + " where synchStatus = '" + Constants.STATUS_LOCAL + "' order by CreatedDate desc")
    List<Content> getAllASynchChats();


}