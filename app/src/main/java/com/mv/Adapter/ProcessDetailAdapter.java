package com.mv.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mv.Activity.LocationSelectionActity;
import com.mv.Activity.ProcessDeatailActivity;
import com.mv.Model.Asset;
import com.mv.Model.ImageData;
import com.mv.Model.Task;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.DecimalDigitsInputFilter;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessDetailAdapter extends RecyclerView.Adapter<ProcessDetailAdapter.MyViewHolder> {

    private ArrayList<Task> taskList;
    private Activity mContext;
    private ProcessDeatailActivity activity;
    private PreferenceHelper preferenceHelper;
    private ArrayList<String> myList, selectedLanList;
    private ArrayList<String> filteredPickList;
    private JSONArray pickListArray;

    private boolean[] mSelection = null;
    private String value = "";
    public static String state, village, taluka;
    private String selectedStructure = "";

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        Spinner spinnerResponse;
        EditText questionResponse, date;
        LinearLayout llLayout, llHeaderLay, llLocation, llCheck, llEditText, llDate, llPhoto;
        TextView question, header, locHeader, locText, checkText, dateHeader, editHeader, imgTitle;
        ImageView imgAdd;
        int selectedPosition = 0;

        @SuppressLint("ClickableViewAccessibility")
        public MyViewHolder(View view) {
            super(view);

            llLayout = view.findViewById(R.id.ll_spinner_layout);
            llEditText = view.findViewById(R.id.ll_edittext_layout);
            llDate = view.findViewById(R.id.ll_date_layout);
            llLocation = view.findViewById(R.id.ll_location_layout);
            llHeaderLay = view.findViewById(R.id.ll_headerr_lay);
            llCheck = view.findViewById(R.id.check_lay);
            llPhoto = view.findViewById(R.id.layout_photo);

            imgAdd = view.findViewById(R.id.img_add);
            question = view.findViewById(R.id.tv_pd_question);
            header = view.findViewById(R.id.tv_header);

            ///location layout
            locHeader = view.findViewById(R.id.tv_loc_hed);
            locText = view.findViewById(R.id.loc_text);
            imgTitle = view.findViewById(R.id.tv_img_hed);
            editHeader = view.findViewById(R.id.tv_edittext_question);
            spinnerResponse = view.findViewById(R.id.sp_response);

            //date  and timeLayout
            date = view.findViewById(R.id.et_process_detail_date);

            date.setOnClickListener(v -> {
                switch (taskList.get(getAdapterPosition()).getTask_type__c()) {
                    case Constants.DATE:
                    case Constants.EVENT_DATE:
                        if (taskList.get(getAdapterPosition()).getLimitValue() != null &&
                                taskList.get(getAdapterPosition()).getLimitValue().equals("Today")) {
                            showDateDialog(mContext, getAdapterPosition(), "CustomCalendar");
                        } else {
                            showDateDialog(mContext, getAdapterPosition(), "NormalCalendar");
                        }
                        break;

                    case Constants.MULTI_SELECT:
                        myList = new ArrayList<>(Arrays.asList(getColumnIndex(
                                (taskList.get(getAdapterPosition()).getPicklist_Value__c()).split(","))));

                        //added this code to enable marathi language in multi-select filed
                        selectedLanList = new ArrayList<>(Arrays.asList(
                                getColumnIndex((taskList.get(getAdapterPosition()).getPicklist_Value_Lan__c()).split(","))));

                        String selectedValues = taskList.get(getAdapterPosition()).getTask_Response__c();
                        if (myList.size() == selectedLanList.size()) {
                            showDialog(selectedLanList, getAdapterPosition(), selectedValues);
                        } else {
                            showDialog(myList, getAdapterPosition(), selectedValues);
                        }
                        break;

                    case Constants.TIME:
                        Calendar currentTime = Calendar.getInstance();
                        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                        int minute = currentTime.get(Calendar.MINUTE);

                        TimePickerDialog mTimePicker;
                        mTimePicker = new TimePickerDialog(mContext, (timePicker, selectedHour, selectedMinute) -> {
                            taskList.get(getAdapterPosition()).setTask_Response__c(updateTime(selectedHour, selectedMinute));
                            notifyItemChanged(getAdapterPosition());
                        }, hour, minute, false);//Yes 24 hour time

                        mTimePicker.setTitle("Select Time");
                        mTimePicker.show();
                        break;
                }
            });

            llLocation.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, LocationSelectionActity.class);
                intent.putExtra(Constants.LOCATION_TYPE, taskList.get(getAdapterPosition()).getTask_type__c());
                intent.putExtra(Constants.LOCATION, taskList.get(getAdapterPosition()).getLocationLevel());
                intent.putExtra(Constants.POSITION, getAdapterPosition());
                intent.putParcelableArrayListExtra(Constants.PROCESS_ID, taskList);
                mContext.startActivityForResult(intent, 1);
            });

            //text and multiline
            questionResponse = view.findViewById(R.id.et_process_detail);
            questionResponse.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d("positionEdit", "" + getAdapterPosition());
                    taskList.get(getAdapterPosition()).setTask_Response__c(s.toString());
                    ((ProcessDeatailActivity) mContext).saveDataToList(taskList.get(getAdapterPosition()), getAdapterPosition());
                    if (taskList.get(getAdapterPosition()).getTask_type__c().equalsIgnoreCase(Constants.TASK_MV_USER) && s.length() == 10) {
                        if (Utills.isConnected(mContext)) {
                            GetUSerName(s.toString(), getAdapterPosition());
                        } else {
                            Utills.showToast(mContext.getString(R.string.error_no_internet), mContext);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            spinnerResponse.setOnTouchListener((v, event) -> {
                if (taskList.get(getAdapterPosition()).getTask_type__c().equals(Constants.TASK_PICK_LIST)) {
                    filteredPickList = structureFilterPickList(taskList.get(getAdapterPosition()));
                    ArrayAdapter<String> dimen_adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, filteredPickList);
                    dimen_adapter.setDropDownViewResource(R.layout.spinnerlayout);
                    spinnerResponse.setPrompt(taskList.get(getAdapterPosition()).getTask_Text___Lan_c());
                    spinnerResponse.setAdapter(dimen_adapter);

                    if (filteredPickList.size() > selectedPosition) {
                        spinnerResponse.setSelection(selectedPosition);
                    }

                    if (taskList.get(getAdapterPosition()).getTask_Text__c().contains("Structure Code") ||
                            taskList.get(getAdapterPosition()).getTask_Text__c().contains("Name of the Structure")) {
                        selectedStructure = "";
                    }
                }
                return false;
            });

            //spinner
            spinnerResponse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        taskList.get(getAdapterPosition()).setTask_Response__c("");
                    } else {
                        if (taskList.get(getAdapterPosition()).getTask_type__c().equals(Constants.TASK_PICK_LIST)) {
                            myList = structureFilterPickList(taskList.get(getAdapterPosition()));
                            taskList.get(getAdapterPosition()).setTask_Response__c(myList.get(position));

                            if (taskList.get(getAdapterPosition()).getTask_Text__c().contains("Structure Code") ||
                                    taskList.get(getAdapterPosition()).getTask_Text__c().contains("Name of the Structure")) {
                                selectedStructure = myList.get(position);
                            }
                        } else {
                            myList = new ArrayList<>(Arrays.asList(getColumnIndex(("Select," +
                                    taskList.get(getAdapterPosition()).getPicklist_Value__c()).split(","))));
                            taskList.get(getAdapterPosition()).setTask_Response__c(myList.get(position));
                        }
                    }
                    selectedPosition = position;
                    ((ProcessDeatailActivity) mContext).saveDataToList(taskList.get(getAdapterPosition()), getAdapterPosition());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            dateHeader = view.findViewById(R.id.tv_date_question);
            checkText = view.findViewById(R.id.check_header);
            checkBox = view.findViewById(R.id.detail_chk);

            checkBox.setOnClickListener(v -> {
                if (((CheckBox) v).isChecked()) {
                    taskList.get(getAdapterPosition()).setTask_Response__c("true");
                    ((ProcessDeatailActivity) mContext).saveDataToList(taskList.get(getAdapterPosition()), getAdapterPosition());
                } else {
                    taskList.get(getAdapterPosition()).setTask_Response__c("false");
                    ((ProcessDeatailActivity) mContext).saveDataToList(taskList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            imgAdd.setOnClickListener(v -> {
                Long tsLong = System.currentTimeMillis();
                String imgName = tsLong.toString();
                taskList.get(getAdapterPosition()).setTask_Response__c(imgName);
                activity.sendToCamera(imgName, getAdapterPosition());
            });
        }
    }

    public ProcessDetailAdapter(Activity context, ArrayList<Task> taskList, String pickListApiFieldNames) {

        this.taskList = taskList;
        this.mContext = context;
        this.activity = (ProcessDeatailActivity) context;

        if (pickListApiFieldNames != null && pickListApiFieldNames.length() > 0) {
            try {
                pickListArray = new JSONArray(pickListApiFieldNames);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_process_detail, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Log.d("position", "" + position);
        Task task = taskList.get(position);

        if (task.getId() != null && !preferenceHelper.getBoolean(Constants.IS_EDITABLE)) {
            holder.questionResponse.setEnabled(false);
            holder.spinnerResponse.setEnabled(false);
            holder.llLocation.setEnabled(false);
            holder.checkBox.setEnabled(false);
            holder.llDate.setEnabled(false);
            holder.llPhoto.setEnabled(false);
            holder.date.setEnabled(false);
            holder.imgAdd.setEnabled(false);
        }

        ArrayAdapter<String> dimen_adapter;
        switch (task.getTask_type__c().trim()) {
            case Constants.TASK_TEXT:
                holder.llEditText.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.editHeader.setText(String.format("* %s", task.getTask_Text___Lan_c()));
                } else {
                    holder.editHeader.setText(task.getTask_Text___Lan_c());
                }

                holder.questionResponse.setText(task.getTask_Response__c());

                switch (task.getValidation()) {
                    case "Alphabets":
                        holder.questionResponse.setInputType(InputType.TYPE_CLASS_TEXT);
                        holder.questionResponse.setSingleLine(false);
                        break;

                    case "Number":
                        holder.questionResponse.setInputType(InputType.TYPE_CLASS_NUMBER);
                        holder.questionResponse.setSingleLine(true);

                        if (task.getValidationRule() != null && task.getValidationRule().equals("Length")) {
                            InputFilter[] filterArray = new InputFilter[1];
                            filterArray[0] = new InputFilter.LengthFilter(Integer.parseInt(task.getLimitValue()));
                            holder.questionResponse.setFilters(filterArray);
                        }
                        break;

                    case "Decimal":
                        holder.questionResponse.setRawInputType(InputType.TYPE_CLASS_NUMBER |
                                InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        holder.questionResponse.setSingleLine(true);

                        if (task.getValidationRule() != null && task.getValidationRule().equals("Range")) {
                            holder.questionResponse.setFilters(new InputFilter[]{
                                    new DecimalDigitsInputFilter(task.getMaxRange().length(), 2)});
                        }
                        break;

                    case "Text":
                        if (task.getValidationRule() != null && task.getValidationRule().equals("Range")) {
                            InputFilter[] filterArray = new InputFilter[1];
                            filterArray[0] = new InputFilter.LengthFilter(Integer.parseInt(task.getMaxRange()));
                            holder.questionResponse.setFilters(filterArray);
                        }
                        break;
                }
                break;

            case Constants.TASK_SELECTION:
                if (task.getIs_Response_Mnadetory__c()) {
                    holder.question.setText(String.format("*%s", task.getTask_Text___Lan_c()));
                } else {
                    holder.question.setText(task.getTask_Text___Lan_c());
                }

                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llEditText.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.VISIBLE);

                myList = new ArrayList<>(Arrays.asList(getColumnIndex((
                        "Select," + task.getPicklist_Value__c()).split(","))));

                selectedLanList = new ArrayList<>(Arrays.asList(getColumnIndex((
                        "Select," + task.getPicklist_Value_Lan__c()).split(","))));

                if (myList.size() == selectedLanList.size()) {
                    dimen_adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, selectedLanList);
                } else {
                    dimen_adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, myList);
                }

                dimen_adapter.setDropDownViewResource(R.layout.spinnerlayout);
                holder.spinnerResponse.setPrompt(task.getTask_Text___Lan_c());
                holder.spinnerResponse.setAdapter(dimen_adapter);

                if (myList.indexOf(task.getTask_Response__c().trim()) >= 0) {
                    holder.spinnerResponse.setSelection(myList.indexOf(task.getTask_Response__c().trim()));
                }
                break;

            case Constants.TASK_PICK_LIST:
                if (task.getIs_Response_Mnadetory__c()) {
                    holder.question.setText(String.format("*%s", task.getTask_Text___Lan_c()));
                } else {
                    holder.question.setText(task.getTask_Text___Lan_c());
                }

                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llEditText.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.VISIBLE);

                filteredPickList = structureFilterPickList(task);

                dimen_adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, filteredPickList);
                dimen_adapter.setDropDownViewResource(R.layout.spinnerlayout);

                holder.spinnerResponse.setPrompt(task.getTask_Text___Lan_c());
                holder.spinnerResponse.setAdapter(dimen_adapter);

                if (filteredPickList.indexOf(task.getTask_Response__c().trim()) >= 0) {
                    selectedStructure = task.getTask_Response__c().trim();
                    holder.spinnerResponse.setSelection(filteredPickList.indexOf(task.getTask_Response__c().trim()));
                } else {
                    filteredPickList.add(0, task.getTask_Response__c());
                }
                break;

            case Constants.MULTI_LINE:
                holder.llEditText.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);

                holder.llDate.setVisibility(View.GONE);
                if (task.getIs_Response_Mnadetory__c()) {
                    holder.editHeader.setText(String.format("* %s", task.getTask_Text___Lan_c()));
                } else {
                    holder.editHeader.setText(task.getTask_Text___Lan_c());
                }

                holder.questionResponse.setLines(3);
                holder.questionResponse.setGravity(Gravity.START);
                holder.questionResponse.setText(task.getTask_Response__c());
                holder.questionResponse.setSingleLine(false);
                break;

            case Constants.HEADER:
                holder.llHeaderLay.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llEditText.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.header.setText(task.getTask_Text___Lan_c());
                break;

            case Constants.LOCATION:
                if (preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL).equalsIgnoreCase(task.getTask_Text__c())) {
                    holder.llHeaderLay.setVisibility(View.GONE);
                    holder.llEditText.setVisibility(View.GONE);
                    holder.llLayout.setVisibility(View.GONE);
                    holder.llCheck.setVisibility(View.GONE);
                    holder.llDate.setVisibility(View.GONE);
                    holder.llLocation.setVisibility(View.VISIBLE);

                    if (task.getIs_Response_Mnadetory__c()) {
                        holder.locHeader.setText(String.format("* %s", task.getTask_Text___Lan_c()));
                    } else {
                        holder.locHeader.setText(task.getTask_Text___Lan_c());
                    }

                    if (task.getTask_Response__c().equals("")) {
                        holder.locText.setText("Select");
                    } else {
                        holder.locText.setText(task.getTask_Response__c());
                    }

                    if (task.getIsEditable__c().equals("false")) {
                        holder.llLocation.setEnabled(false);
                    } else {
                        if (task.getId() != null && !preferenceHelper.getBoolean(Constants.IS_EDITABLE)) {
                            holder.llLocation.setEnabled(false);
                        } else {
                            holder.llLocation.setEnabled(true);
                        }
                    }
                } else {
                    holder.llHeaderLay.setVisibility(View.GONE);
                    holder.llEditText.setVisibility(View.GONE);
                    holder.llLocation.setVisibility(View.GONE);
                    holder.llLayout.setVisibility(View.GONE);
                    holder.llDate.setVisibility(View.GONE);
                    holder.llCheck.setVisibility(View.GONE);
                }
                break;

            case Constants.LOCATION_TASK:
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llEditText.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.VISIBLE);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.locHeader.setText(String.format("* %s", task.getTask_Text___Lan_c()));
                } else {
                    holder.locHeader.setText(task.getTask_Text___Lan_c());
                }

                if (task.getTask_Response__c().equals("")) {
                    holder.locText.setText("Select");
                } else {
                    holder.locText.setText(task.getTask_Response__c());
                }
                break;

            case Constants.DATE:
                holder.llEditText.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.VISIBLE);
                holder.date.setHint(R.string.please_enter_date_time);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.dateHeader.setText(String.format("*%s", task.getTask_Text___Lan_c()));
                } else {
                    holder.dateHeader.setText(task.getTask_Text___Lan_c());
                }

                holder.date.setText(task.getTask_Response__c());
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);
                break;

            case Constants.TIME:
                holder.llEditText.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.VISIBLE);
                holder.date.setHint(R.string.please_enter_date_time);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.dateHeader.setText(String.format("*%s", task.getTask_Text___Lan_c()));
                } else {
                    holder.dateHeader.setText(task.getTask_Text___Lan_c());
                }

                holder.date.setText(task.getTask_Response__c());
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);
                break;

            case Constants.CHECK_BOX:
                holder.llEditText.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.VISIBLE);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.checkText.setText(String.format("*%s", task.getTask_Text___Lan_c()));
                } else {
                    holder.checkText.setText(task.getTask_Text___Lan_c());
                }
                holder.checkBox.setChecked(Boolean.valueOf(task.getTask_Response__c()));
                break;

            case Constants.MULTI_SELECT:
                holder.llEditText.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.VISIBLE);
                holder.llCheck.setVisibility(View.GONE);
                holder.questionResponse.setHint(task.getTask_Text___Lan_c());

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.dateHeader.setText(String.format("*%s", task.getTask_Text___Lan_c()));
                } else {
                    holder.dateHeader.setText(task.getTask_Text___Lan_c());
                }

                if (task.getTask_Response__c() != null && task.getTask_Response__c().length() > 0 && taskList.size() > position) {
                    String answerStr = "";
                    ArrayList<String> myList1 = new ArrayList<>(Arrays.asList(getColumnIndex(
                            (taskList.get(position).getPicklist_Value__c()).split(","))));

                    ArrayList<String> selectedLanList1 = new ArrayList<>(Arrays.asList(getColumnIndex(
                            (taskList.get(position).getPicklist_Value_Lan__c()).split(","))));

                    ArrayList<String> answer = new ArrayList<>(Arrays.asList(getColumnIndex(
                            (task.getTask_Response__c()).split(","))));

                    for (String strAns : answer) {
                        if (myList1.indexOf(strAns) > -1 && selectedLanList1.size() > myList1.indexOf(strAns)) {
                            answerStr = answerStr.concat((selectedLanList1.get(myList1.indexOf(strAns))).concat(","));
                        }
                    }

                    if (answerStr.contains(",")) {
                        holder.date.setText(answerStr.substring(0, answerStr.length() - 1));
                    }
                } else {
                    holder.date.setText("Select");
                }

                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);
                break;

            case Constants.EVENT_MOBILE:
                holder.llEditText.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.questionResponse.setSingleLine(true);
                holder.llDate.setVisibility(View.GONE);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.editHeader.setText(String.format("* %s", task.getTask_Text___Lan_c()));
                } else {
                    holder.editHeader.setText(task.getTask_Text___Lan_c());
                }
                holder.questionResponse.setText(task.getTask_Response__c());

                if (task.getValidation().equals("Alphabets")) {
                    holder.questionResponse.setInputType(InputType.TYPE_CLASS_TEXT);
                } else if (task.getValidation().equals("Number")) {
                    holder.questionResponse.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                break;

            case Constants.EVENT_DESCRIPTION:
                holder.llEditText.setVisibility(View.VISIBLE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.editHeader.setText(String.format("*%s", task.getTask_Text___Lan_c()));
                } else {
                    holder.editHeader.setText(task.getTask_Text___Lan_c());
                }

                holder.questionResponse.setSingleLine(false);
                holder.questionResponse.setMinLines(3);
                holder.questionResponse.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                holder.questionResponse.setGravity(Gravity.START | Gravity.TOP);
                holder.questionResponse.setText(task.getTask_Response__c());
                break;

            case Constants.EVENT_DATE:
                holder.llEditText.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.VISIBLE);
                holder.date.setHint(R.string.please_enter_date_time);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.dateHeader.setText(String.format("*%s", task.getTask_Text___Lan_c()));
                } else {
                    holder.dateHeader.setText(task.getTask_Text___Lan_c());
                }

                holder.date.setText(task.getTask_Response__c());
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);
                break;

            case Constants.TASK_MV_USER:
                holder.llEditText.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.editHeader.setText(String.format("* %s", task.getTask_Text___Lan_c()));
                } else {
                    holder.editHeader.setText(task.getTask_Text___Lan_c());
                }
                holder.questionResponse.setText(task.getTask_Response__c());
                holder.questionResponse.setInputType(InputType.TYPE_CLASS_NUMBER);
                holder.questionResponse.setSingleLine(true);
                break;

            case Constants.TASK_MV_USER_ANSWER:
                holder.llEditText.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);

                if (task.getIs_Response_Mnadetory__c()) {
                    holder.editHeader.setText(String.format("* %s", task.getTask_Text___Lan_c()));
                } else {
                    holder.editHeader.setText(task.getTask_Text___Lan_c());
                }
                if(task.getTask_Response__c()!=null && task.getTask_Response__c().length()>0 && task.getTask_Response__c().contains("(")){
                    holder.questionResponse.setText(task.getTask_Response__c().substring(0,task.getTask_Response__c().indexOf("(")));
                } else {
                    holder.questionResponse.setText(task.getTask_Response__c());
                }
//                holder.questionResponse.setInputType(InputType.TYPE_CLASS_NUMBER);
                holder.questionResponse.setSingleLine(true);
                holder.questionResponse.setHint("");
                if (task.getIsEditable__c().equals("false")) {
                    holder.questionResponse.setEnabled(false);
                }
                break;

            case Constants.IMAGE:
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llEditText.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llPhoto.setVisibility(View.VISIBLE);
                holder.imgTitle.setText(taskList.get(position).getTask_Text___Lan_c());

                if (!taskList.get(position).getTask_Response__c().equals("")) {
                    String imgName = taskList.get(position).getTask_Response__c();
                    String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Image/" + imgName + ".jpg";
                    File imageFile = new File(imageFilePath);

                    if (imageFile.exists()) {
                        holder.imgAdd.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
                        Uri finalUri = Uri.fromFile(imageFile);
                        ImageData id = new ImageData();
                        id.setPosition(position);
                        id.setImageUri(finalUri);
                        ((ProcessDeatailActivity)mContext).imageDataList.add(id);
                    } else {
                        Glide.with(mContext)
                                .load(Constants.IMAGEURL + taskList.get(position).getTask_Response__c() + ".png")
                                .placeholder(mContext.getResources().getDrawable(R.drawable.ic_add_photo))
                                .into(holder.imgAdd);
                    }
                }
                break;

            default:
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llEditText.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
        }
    }

    private ArrayList<String> structureFilterPickList(Task task) {
        ArrayList<String> filterList = new ArrayList<>();
        filterList.add("Select");
        try {
            if (pickListArray != null) {
                HashMap<String, String> filterValues = new HashMap<>();
                String[] filterArray = task.getFilterFields().split(",");
                boolean flag = false;

                for (String filter : filterArray) {
                    for (Task tempTask : taskList) {
                        String apiField = tempTask.getaPIFieldName();
                        if (apiField.equalsIgnoreCase(filter)) {
                            String taskResponse = tempTask.getTask_Response__c();
                            filterValues.put(filter, taskResponse.isEmpty() ? selectedStructure : taskResponse);
                            break;
                        }
                    }
                }

                if (task.getTask_Text__c().contains("Machine Code") || task.getTask_Text__c().contains("Machine code")) {
                    if (filterValues.containsKey("taskAnswer1__c")) {
                        filterValues.put("taskAnswer1__c", selectedStructure);
                    }
                }

                for (int i = 0; i < pickListArray.length(); i++) {
                    JSONObject pickListJsonObj = pickListArray.getJSONObject(i);
                    String referenceField = task.getReferenceField();

                    if (pickListJsonObj.has(referenceField) && pickListJsonObj.get("taskId__c").equals(task.getMV_Task__c_Id())) {
                        for (String filter : filterArray) {
                            String strFilter = filterValues.get(filter);
                            if (strFilter != null && strFilter.equalsIgnoreCase(pickListJsonObj.getString(filter))) {
                                flag = true;
                            } else {
                                flag = false;
                                break;
                            }
                        }

                        if (flag) {
                            filterList.add(pickListJsonObj.getString(referenceField));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filterList;
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private static String[] getColumnIndex(String[] value) {
        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void GetUSerName(String number, int position) {
        Utills.showProgressDialog(mContext, "Sending", mContext.getString(R.string.progress_please_wait));
        ServiceRequest apiService = ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
        String url;
        url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetUserThroughMobileNo + "?mobileNo=" + number.trim();

        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                String data;
                try {
                    if (response.body() != null) {
                        data = response.body().string();
                        if (data.length() > 0) {
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            Asset asset = gson.fromJson(data, Asset.class);
                            String Id = asset.getAsset_id();
                            String Fname = asset.getName();
                            String Lname = asset.getLast_Name__c();
                            for(int i=0;i<taskList.size();i++){
                                if(taskList.get(i).getTask_type__c().equalsIgnoreCase(Constants.TASK_MV_USER_ANSWER)) {
                                    if (Lname != null)
                                        taskList.get(i).setTask_Response__c(Fname + " " + Lname + "(" + Id + ")");
                                    else {
                                        taskList.get(i).setTask_Response__c(Fname + "(" + Id + ")");
                                    }
                                    notifyItemChanged(i);
                                }
                            }

                        }
                    } else {
                        Toast.makeText(mContext,mContext.getResources()
                                .getString(R.string.enter_moblie_no),Toast.LENGTH_SHORT).show();
                        for(int i=0;i<taskList.size();i++){
                            if (taskList.get(i).getTask_type__c().equalsIgnoreCase(Constants.TASK_MV_USER_ANSWER)) {
                                taskList.get(i).setTask_Response__c("");
                                notifyItemChanged(i);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(mContext,"Something went wrong",Toast.LENGTH_SHORT).show();
                Utills.hideProgressDialog();
            }
        });
    }

    public void showDateDialog(Context context, final int Position, String CalendarType) {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(context,
                (view, year, monthOfYear, dayOfMonth) -> {
                    taskList.get(Position).setTask_Response__c(
                            getTwoDigit(dayOfMonth) + "/" + getTwoDigit(monthOfYear + 1) + "/" + year);
                    notifyItemChanged(Position);
                }, mYear, mMonth, mDay);

        if (CalendarType.equals("CustomCalendar")) {
            dpd.getDatePicker().setMaxDate(System.currentTimeMillis() - 10000);
        }
        dpd.show();
    }

    public static String getTwoDigit(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return "" + i;
    }

    private void showDialog(ArrayList<String> arrayList, final int pos, String selectedItems) {
        final String[] items = arrayList.toArray(new String[arrayList.size()]);
        final String[] defaultLangItems = myList.toArray(new String[myList.size()]);
        mSelection = new boolean[items.length];
        Arrays.fill(mSelection, false);

        value = "";
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            if (selectedItems != null && selectedItems.contains(item)) {
                mSelection[i] = true;
                value = value.concat(item + ",");
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(taskList.get(pos).getTask_Text___Lan_c())
                .setMultiChoiceItems(items, mSelection, (dialog13, which, isChecked) -> {
                    if (mSelection != null && which < mSelection.length) {
                        mSelection[which] = isChecked;
                        value = buildSelectedItemString(defaultLangItems);
                    } else {
                        throw new IllegalArgumentException(
                                "Argument 'which' is out of bounds.");
                    }
                })
                .setPositiveButton(mContext.getString(R.string.ok), (dialog12, id) -> {
                    taskList.get(pos).setTask_Response__c(value);
                    notifyItemChanged(pos);
                }).setNegativeButton(mContext.getString(R.string.cancel), (dialog1, id) -> {
                    //  Your code when user clicked on Cancel
                }).create();

        dialog.show();
    }

    private String buildSelectedItemString(String[] items) {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(items[i]);
            }
        }
        return sb.toString();
    }

    private String updateTime(int hours, int mins) {
        String timeSet;
        if (hours > 12) {
            hours -= 12;
            timeSet = "PM";
        } else if (hours == 0) {
            hours += 12;
            timeSet = "AM";
        } else if (hours == 12) {
            timeSet = "PM";
        } else {
            timeSet = "AM";
        }

        String minutes;
        if (mins < 10) {
            minutes = "0" + mins;
        } else {
            minutes = String.valueOf(mins);
        }

        // Append in a StringBuilder
        return String.valueOf(hours) + ':' + minutes + " " + timeSet;
    }
}