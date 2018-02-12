package com.mv.Utils;

import android.content.Context;
import android.net.Uri;

/**
 * Created by User on 6/1/2017.
 */

public class Constants {

    /* Production*/
  /*  public static String LOGIN_URL = "https://login.salesforce.com/services/oauth2/token";
    public static String USERNAME = "mulyavardhan.smf@gmail.com";
    public static String REPORTID = "a1L7F000000YNK4";
    public static String ISSUEID = "a1L7F000000YNJz";*/
    /* Sandbox*/
    public static String LOGIN_URL = "https://test.salesforce.com/services/oauth2/token";
    public static String USERNAME = "mulyavardhan.smf@gmail.com.dev";
    public static String REPORTID = "a1G0k000000522K";
    public static String ISSUEID = "a1G0k000000522F";



    public static int SELECT_AUDIO = 501;
    public static Uri shareUri = null;
    public static final int ISROLECHANGE = 1;
    public static String ID = "ID";
    public static String PASSWORD = "Nano4545";
    public static String CLIENT_ID = "3MVG9d8..z.hDcPJhCvdazzxmwecKJ839UtvRRCnGEbq5p_PT49tZaftCOG4eti.6aI2v98zkYM0KQvaOWmTP";
    public static String CLIENT_SECRET = "2027871201908212165";
    public static String GRANT_TYPE = "password";
    public static String RESPONSE_TYPE = "token";
    public static String URL = "url";
    public static String TITLE = "title";
    public static String LIST = "list";
    public static String CONTENT = "content";

    public static final String TABLE_CALANDER = "table_calender";
    public static final String TABLE_TASK = "table_task";
    public static final String TABLE_PROCESS = "table_process";
    public static final String TABLE_CONTAINER = "table_container";
    public static final String TABLE_LOCATION = "table_location";
    public static String ACTION = "action";
    public static String ACTION_ADD = "add";
    public static String ACTION_EDIT = "edit";
    public static final String TABLE_TEMPLATE = "table_template";
    public static final String TABLE_COMMUNITY = "table_community";
    public static final String TABLE_CONTENT = "table_content";


    public static Integer CHOOSE_IMAGE_FROM_CAMERA = 100;
    public static Integer CHOOSE_IMAGE_FROM_GALLERY = 101;
    public static Integer CHOOSE_VIDEO_FROM_CAMERA = 102;
    public static Integer CHOOSE_VIDEO_FROM_GALLERY = 103;

    public static String TEMPLATE_REPORT = "Report";
    public static String PROCESS_ID = "process_id";
    public static String NEW_PROCESS = "new_process";
    public static String PROCESS_NAME = "PRocessName";
    public static String TEMPLATE_ISSUE = "Issue";
    public static String IS_EDITABLE = "isEditable";
    public static String IS_LOCATION = "isEditableLocation";
    public static String IS_MULTIPLE = "isMultiple";
    public static String STATE_LOCATION_LEVEL = "locationTest";


    public static final String TASK_TEXT = "Text";
    public static final String TASK_SELECTION = "Selection";
    public static final String MULTI_LINE = "Multi-Lines";
    public static final String HEADER = "Header";
    public static final String LOCATION = "Location";
    public static final String POSITION = "position";
    public static final String DATE = "Date";
    public static final String TIME = "Time Picker";
    public static final String CHECK_BOX = "Checkbox";
    public static final String MULTI_SELECT = "Multi-select";
    public static final String EVENT_MOBILE = "Event Mobile";
    public static final String EVENT_DATE = "Event Date";
    public static final String EVENT_DESCRIPTION = "Event Description";
    public static final String IMAGE = "Image";

    public static final String State = "State";
    public static final String DISTRICT = "District";
    public static final String TALUKA = "Taluka";
    public static final String CLUSTER = "Cluster";
    public static final String VILlAGE = "Village";
    public static final String SCHOOL = "School";

    public static String TASK_ANSWER = "answer";
    public static String TASK_QUESTION = "question";
    public static final String STATUS_LOCAL = "status_local";
    public static final String TEMPLATES = "templates";
    public static final String UNIQUE = "UNIQUE";

    public static String INDICATOR_TASK = "indicator_task";
    public static String INDICATOR_TASK_ROLE = "indicator_task_role";

    public static final String PROCESS_STATE_SAVE = "true";
    public static final String PROCESS_STATE_SUBMIT = "false";
    public static final String PROCESS_STATE_MODIFIED = "modified";

    public static final String APPROVAL_TYPE = "approval_type";
    public static final String USER_APPROVAL = "user_approval";
    public static final String PROCESS_APPROVAL = "process_approval";

    public static final String PROCESS_TYPE = "process_type";
    public static final String MANGEMENT_PROCESS = "managenment_approval";
    public static final String APPROVAL_PROCESS = "approval_process";

    public static final String Thet_Sanvad = "Thet Sanvad";
    public static final String Broadcast = "Broadcast";
    public static final String My_Community = "My Community";
    public static final String Programme_Management = "Programme Management";
    public static final String Training_Content = "Training Content";
    public static final String Team_Management = "Team Management";
    public static final String My_Reports = "My Reports";
    public static final String My_Calendar = "My Calendar";

    public static final String IMAGEURL = "http://13.58.218.106/images/";
    public static final String InsertContentUrl = "/services/apexrest/insertContent";
    public static final String DeletePostUrl = "/services/apexrest/DeletePost/";
    public  static final String InsertBroadcastPostUrl ="/services/apexrest/InsertBroadcastPost";
    public static final String MV_Role__c_URL = "/services/data/v36.0/query/?q=select+Name+from+MV_Role__c";
    public static final String GetOrganizationUrl = "/services/apexrest/getOrganization";
    public static final String GetUserDataForCalnder = "/services/apexrest/getUserDataForCalnder";
    public static final String InsertEventcalender_Url = "/services/apexrest/InsertEventcalender";
    public static final String Userdetails_Url ="/services/apexrest/userdetails";
    public static final String MV_GetCommunities_c_Url="/services/apexrest/MV_GetCommunities_c";
    public static final String DoLogout_url ="/services/apexrest/doLogout/";
    public static final String GetUserData_url ="/services/apexrest/getUserData";
    public static final String MapParametersUrl="/services/apexrest/MapParameters";
    public static final String GetSessionDatademo_Url  ="/services/apexrest/getSessionDatademo/";
    public static final String Upload_Url  =  "http://13.58.218.106/upload.php";
    public static final String GetLoginOTP_url  =  "/services/apexrest/getLoginOTP";
    public static final String GetchartDatademoNew  =  "/services/apexrest/getchartDatademoNew";
    public static final String GetDashboardDatademoUrl  =  "/services/apexrest/getDashboardDatademo";
    public static final String GetApprovalProcessUrl  ="/services/apexrest/getApprovalProcess";
    public static final String InsertAnswerForProcessAnswerUrl  = "/services/apexrest/InsertAnswerForProcessAnswer";
    public static final String New_upload_phpUrl  ="http://13.58.218.106/new_upload.php";
    public static final String GetprocessAnswerDataUrl = "/services/apexrest/getprocessAnswerData";
    public static final String GetprocessTaskUrl ="/services/apexrest/getprocessTask";
    public static final String DeleteTaskAnswerUrl =  "/services/apexrest/DeleteTaskAnswer/";
    public static final String ApproveCommentforProcessUrl = "/services/apexrest/ApproveCommentforProcess";
    public static final String GetprocessAnswerTaskfoApprovalUrl = "/services/apexrest/getprocessAnswerTaskfoApproval";
    public static final String GetProjectDataUrl ="/services/apexrest/getProjectData";
    public static final String MV_RoleUrl = "/services/data/v36.0/query/?q=select+Id,Juridictions__c,Name+from+MV_Role__c+where+Organisation__c='";
    public static final String MTRegisterUrl = "/services/apexrest/MTRegister";
    public static final String GetApprovalDataUrl =     "/services/apexrest/getApprovalData";
    public  static final String WS_getProcessAprovalUserUrl ="/services/apexrest/WS_getProcessAprovalUser";
    public  static final String MV_GeTemplates_cUrl = "/services/apexrest/MV_GeTemplates_c";
    public  static final String ApproveCommentUrl = "/services/apexrest/ApproveComment";
    public  static final String SharedRecordsUrl =   "/services/apexrest/sharedRecords";
    public  static final String RemoveLikeUrl = "/services/apexrest/removeLike";
    public  static final String InsertLikeUrl = "/services/apexrest/InsertLike";
}
