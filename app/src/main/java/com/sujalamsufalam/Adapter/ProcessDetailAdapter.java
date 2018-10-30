package com.sujalamsufalam.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bumptech.glide.Glide;
import com.sujalamsufalam.Activity.LocationSelectionActity;
import com.sujalamsufalam.Activity.ProcessDeatailActivity;
import com.sujalamsufalam.Model.Task;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


/**
 * Created by nanostuffs on 26-09-2017.
 */

public class ProcessDetailAdapter extends RecyclerView.Adapter<ProcessDetailAdapter.MyViewHolder> {

    private ArrayList<Task> taskList;
    private Activity mContext;
    PreferenceHelper preferenceHelper;
    ArrayList<String> myList, selectedLanList;
    ArrayAdapter<String> dimen_adapter;
    boolean[] mSelection = null;
    final String[] items = null;
    String value;
    public static String state, village, taluka;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llLayout, llHeaderLay, llLocation, llCheck, llMutiselect, llEdittext, llDate;
        EditText questionResponse, date;
        TextView question, header, locHeader, locText, checkText, dateHeader, editHeader;
        Spinner spinnerResponse;

        CheckBox checkBox;

        public MyViewHolder(View view) {
            super(view);

            llLayout = view.findViewById(R.id.ll_spinner_layout);
            llEdittext = view.findViewById(R.id.ll_edittext_layout);
            llDate = view.findViewById(R.id.ll_date_layout);
            llLocation = view.findViewById(R.id.ll_location_layout);
            llHeaderLay = view.findViewById(R.id.ll_headerr_lay);
            llCheck = view.findViewById(R.id.check_lay);
            //   llMutiselect = (LinearLayout) view.findViewById(R.id.ll_multispinner_lay);

            question = view.findViewById(R.id.tv_pd_question);
            header = view.findViewById(R.id.tv_header);
            ///location layout
            locHeader = view.findViewById(R.id.tv_loc_hed);
            locText = view.findViewById(R.id.loc_text);
            editHeader = view.findViewById(R.id.tv_edittext_question);
            spinnerResponse = view.findViewById(R.id.sp_response);
            //    multiSelect = (MultiSelectionSpinner) view.findViewById(R.id.multi_spinner);
            //date  and timelayout

            date = view.findViewById(R.id.et_process_detail_date);
            date.setOnClickListener(v -> {
                if (taskList.get(getAdapterPosition()).getTask_type__c().equals(Constants.DATE) || taskList.get(getAdapterPosition()).getTask_type__c().equals(Constants.EVENT_DATE))
                    showDateDialog(mContext, getAdapterPosition());
                else if (taskList.get(getAdapterPosition()).getTask_type__c().equals(Constants.MULTI_SELECT)) {
                    myList = new ArrayList<String>(Arrays.asList(getColumnIdex((taskList.get(getAdapterPosition()).getPicklist_Value__c()).split(","))));
                    //added this code to enable marathi language in multiselect filed
                    selectedLanList = new ArrayList<String>(Arrays.asList(getColumnIdex((taskList.get(getAdapterPosition()).getPicklist_Value_Lan__c()).split(","))));
                   // taskList.get(getAdapterPosition()).setTask_Response__c(myList.get(getAdapterPosition()));
                    if (myList.size() == selectedLanList.size())
                        showDialog(selectedLanList, getAdapterPosition());
                    else
                        showDialog(myList, getAdapterPosition());

                } else if (taskList.get(getAdapterPosition()).getTask_type__c().equals(Constants.TIME)) {
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(mContext, (timePicker, selectedHour, selectedMinute) -> {
                        taskList.get(getAdapterPosition()).setTask_Response__c(updateTime(selectedHour, selectedMinute));
                        notifyItemChanged(getAdapterPosition());
                    }, hour, minute, false);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

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
            // inputLayout = (TextInputLayout) view.findViewById(R.id.input_content);
            questionResponse = view.findViewById(R.id.et_process_detail);
            questionResponse.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d("positionEdit", "" + getAdapterPosition());
                    //  taskList.get(getAdapterPosition()).setTask_Response__c(s.toString());
                    taskList.get(getAdapterPosition()).setTask_Response__c(s.toString());
                    ((ProcessDeatailActivity) mContext).saveDataToList(taskList.get(getAdapterPosition()), getAdapterPosition());

                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            //spinner
            spinnerResponse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (position == 0)
                        taskList.get(getAdapterPosition()).setTask_Response__c("");
                    else {
                        myList = new ArrayList<>(Arrays.asList(getColumnIdex(("Select," + taskList.get(getAdapterPosition()).getPicklist_Value__c()).split(","))));
                        taskList.get(getAdapterPosition()).setTask_Response__c(myList.get(position));
                    }
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


        }
    }


    public ProcessDetailAdapter(Activity context, ArrayList<Task> taskList) {
        this.taskList = taskList;
        this.mContext = context;
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
        if (!preferenceHelper.getBoolean(Constants.IS_EDITABLE)) {
            holder.questionResponse.setEnabled(false);
            holder.spinnerResponse.setEnabled(false);
            holder.llLocation.setEnabled(false);
            holder.date.setEnabled(false);
            holder.checkBox.setEnabled(false);
            holder.llDate.setEnabled(false);
        }

        switch (task.getTask_type__c().trim()) {

            case Constants.TASK_TEXT:
                //      holder.inputLayout.setVisibility(View.);
                holder.llEdittext.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);

                holder.llDate.setVisibility(View.GONE);
                // holder.questionResponse.setHint(task.getTask_Text___Lan_c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.editHeader.setText("* " + task.getTask_Text___Lan_c());
                else
                    holder.editHeader.setText(task.getTask_Text___Lan_c());

                holder.questionResponse.setText(task.getTask_Response__c());
                if (task.getValidation().equals("Alphabets")) {
                    //  holder.questionResponse.setInputType();
                    holder.questionResponse.setInputType(InputType.TYPE_CLASS_TEXT);
                } else if (task.getValidation().equals("Number")) {
                    holder.questionResponse.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                holder.questionResponse.setSingleLine(true);
                break;
            case Constants.TASK_SELECTION:

                if (task.getIs_Response_Mnadetory__c())
                    holder.question.setText("*" + task.getTask_Text___Lan_c());
                else
                    holder.question.setText(task.getTask_Text___Lan_c());

                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llEdittext.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.VISIBLE);

                myList = new ArrayList<>(Arrays.asList(getColumnIdex(("Select," + task.getPicklist_Value__c()).split(","))));
                selectedLanList = new ArrayList<>(Arrays.asList(getColumnIdex(("Select," + task.getPicklist_Value_Lan__c()).split(","))));
                if (myList.size() == selectedLanList.size())
                    dimen_adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, selectedLanList);
                else
                    dimen_adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, myList);
                dimen_adapter.setDropDownViewResource(R.layout.spinnerlayout);
                holder.spinnerResponse.setPrompt(task.getTask_Text___Lan_c());
                holder.spinnerResponse.setAdapter(dimen_adapter);
                //      if (!preferenceHelper.getBoolean(Constants.NEW_PROCESS))
                if (myList.indexOf(task.getTask_Response__c().trim()) >= 0)
                    holder.spinnerResponse.setSelection(myList.indexOf(task.getTask_Response__c().trim()));

                break;
            case Constants.MULTI_LINE:
                holder.llEdittext.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);

                holder.llDate.setVisibility(View.GONE);
                // holder.questionResponse.setHint(task.getTask_Text___Lan_c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.editHeader.setText("* " + task.getTask_Text___Lan_c());
                else
                    holder.editHeader.setText(task.getTask_Text___Lan_c());

                holder.questionResponse.setLines(3);
                //   holder.questionResponse.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                holder.questionResponse.setGravity(Gravity.LEFT);
                holder.questionResponse.setText(task.getTask_Response__c());
                holder.questionResponse.setSingleLine(false);

                break;
            case Constants.HEADER:
                holder.llHeaderLay.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llEdittext.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.header.setText(task.getTask_Text___Lan_c());
                break;
            case Constants.LOCATION:

                if (preferenceHelper.getString(Constants.STATE_LOCATION_LEVEL).equalsIgnoreCase(task.getTask_Text__c())) {
                    holder.llHeaderLay.setVisibility(View.GONE);
                    holder.llEdittext.setVisibility(View.GONE);
                    holder.llLayout.setVisibility(View.GONE);
                    holder.llCheck.setVisibility(View.GONE);
                    holder.llDate.setVisibility(View.GONE);
                    holder.llLocation.setVisibility(View.VISIBLE);
                    if (task.getIs_Response_Mnadetory__c())
                        holder.locHeader.setText("* " + task.getTask_Text___Lan_c());
                    else
                        holder.locHeader.setText(task.getTask_Text___Lan_c());
                    if (task.getTask_Response__c().equals(""))
                        holder.locText.setText("Select");
                    else
                        holder.locText.setText(task.getTask_Response__c());

                    if (task.getIsEditable__c().equals("false"))
                        holder.llLocation.setEnabled(false);
                    else
                        holder.llLocation.setEnabled(true);
                } else {
                    holder.llHeaderLay.setVisibility(View.GONE);
                    holder.llEdittext.setVisibility(View.GONE);
                    holder.llLocation.setVisibility(View.GONE);
                    holder.llLayout.setVisibility(View.GONE);
                    holder.llDate.setVisibility(View.GONE);
                    holder.llCheck.setVisibility(View.GONE);
                }

                break;

            case Constants.LOCATION_TASK:
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llEdittext.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.VISIBLE);
                if (task.getIs_Response_Mnadetory__c())
                    holder.locHeader.setText("* " + task.getTask_Text___Lan_c());
                else
                    holder.locHeader.setText(task.getTask_Text___Lan_c());
                if (task.getTask_Response__c().equals(""))
                    holder.locText.setText("Select");
                else
                    holder.locText.setText(task.getTask_Response__c());
                break;
            case Constants.DATE:
                holder.llEdittext.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.VISIBLE);
                holder.date.setHint(R.string.please_enter_date_time);
                // holder.questionResponse.setHint(task.getTask_Text___Lan_c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.dateHeader.setText("*" + task.getTask_Text___Lan_c());
                else
                    holder.dateHeader.setText(task.getTask_Text___Lan_c());
                holder.date.setText(task.getTask_Response__c());
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);

                break;
            case Constants.TIME:
                holder.llEdittext.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.VISIBLE);
                holder.date.setHint(R.string.please_enter_date_time);
                // holder.questionResponse.setHint(task.getTask_Text___Lan_c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.dateHeader.setText("*" + task.getTask_Text___Lan_c());
                else
                    holder.dateHeader.setText(task.getTask_Text___Lan_c());
                holder.date.setText(task.getTask_Response__c());
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);
                break;
            case Constants.CHECK_BOX:

                holder.llEdittext.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.VISIBLE);

                if (task.getIs_Response_Mnadetory__c())
                    holder.checkText.setText("*" + task.getTask_Text___Lan_c());
                else
                    holder.checkText.setText(task.getTask_Text___Lan_c());
                holder.checkBox.setChecked(Boolean.valueOf(task.getTask_Response__c()));

                break;
            case Constants.MULTI_SELECT:
                holder.llEdittext.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.VISIBLE);
                holder.llCheck.setVisibility(View.GONE);
                //  holder.date.setHint("");
                 holder.questionResponse.setHint(task.getTask_Text___Lan_c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.dateHeader.setText("*" + task.getTask_Text___Lan_c());
                else
                    holder.dateHeader.setText(task.getTask_Text___Lan_c());

                if (task.getTask_Response__c() != null && task.getTask_Response__c().length() > 0) {
                    String answerStr="";
                    ArrayList<String> myList1 = new ArrayList<>(Arrays.asList(getColumnIdex((taskList.get(position).getPicklist_Value__c()).split(","))));
                    ArrayList<String> selectedLanList1 = new ArrayList<>(Arrays.asList(getColumnIdex((taskList.get(position).getPicklist_Value_Lan__c()).split(","))));
                    ArrayList<String> answer = new ArrayList<>(Arrays.asList(getColumnIdex((task.getTask_Response__c()).split(","))));
                        for(String strAns:answer){
                            answerStr=answerStr.concat((selectedLanList1.get(myList1.indexOf(strAns))).concat(","));
                        }
                        holder.date.setText(answerStr);
                } else
                    holder.date.setText("Select");
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);
                break;

            case Constants.EVENT_MOBILE:
                holder.llEdittext.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.questionResponse.setSingleLine(true);
                holder.llDate.setVisibility(View.GONE);
                // holder.questionResponse.setHint(task.getTask_Text___Lan_c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.editHeader.setText("* " + task.getTask_Text___Lan_c());
                else
                    holder.editHeader.setText(task.getTask_Text___Lan_c());

                holder.questionResponse.setText(task.getTask_Response__c());
                if (task.getValidation().equals("Alphabets")) {
                    //  holder.questionResponse.setInputType();
                    holder.questionResponse.setInputType(InputType.TYPE_CLASS_TEXT);
                } else if (task.getValidation().equals("Number")) {
                    holder.questionResponse.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                break;
            case Constants.EVENT_DESCRIPTION:
                holder.llEdittext.setVisibility(View.VISIBLE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);

                // holder.questionResponse.setHint(task.getTask_Text___Lan_c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.editHeader.setText("*" + task.getTask_Text___Lan_c());
                else
                    holder.editHeader.setText(task.getTask_Text___Lan_c());
                holder.questionResponse.setSingleLine(false);
                holder.questionResponse.setMinLines(3);
                holder.questionResponse.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                holder.questionResponse.setGravity(Gravity.LEFT | Gravity.TOP);
                //          if (!preferenceHelper.getBoolean(Constants.NEW_PROCESS))
                holder.questionResponse.setText(task.getTask_Response__c());
                break;
            case Constants.EVENT_DATE:
                holder.llEdittext.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.VISIBLE);
                holder.date.setHint(R.string.please_enter_date_time);
                // holder.questionResponse.setHint(task.getTask_Text___Lan_c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.dateHeader.setText("*" + task.getTask_Text___Lan_c());
                else
                    holder.dateHeader.setText(task.getTask_Text___Lan_c());
                holder.date.setText(task.getTask_Response__c());
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);
                break;
            case Constants.IMAGE:
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llEdittext.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                break;
            default:
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llEdittext.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llDate.setVisibility(View.GONE);
                //    holder.llMutiselect.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static String[] getColumnIdex(String[] value) {

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


    public void showDateDialog(Context context, final int Position) {


        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(context,
                (view, year, monthOfYear, dayOfMonth) -> {
                    taskList.get(Position).setTask_Response__c(getTwoDigit(dayOfMonth) + "/" + getTwoDigit(monthOfYear + 1) + "/" + year);
                    notifyItemChanged(Position);

                }, mYear, mMonth, mDay);
        dpd.show();
    }

    public static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
    }

    private void showDialog(ArrayList<String> arrayList, final int pos) {
        final String[] items = arrayList.toArray(new String[arrayList.size()]);

         final String[] deafultLangitems = myList.toArray(new String[myList.size()]);
//      mSelection = new boolean[deafultLangitems.length];
//      Arrays.fill(mSelection, false);

        mSelection = new boolean[items.length];
        Arrays.fill(mSelection, false);

//      arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(taskList.get(pos).getTask_Text___Lan_c())
                .setMultiChoiceItems(items, mSelection, (dialog13, which, isChecked) -> {
                    if (mSelection != null && which < mSelection.length) {
                        mSelection[which] = isChecked;
//                            value = buildSelectedItemString(items);
                        value = buildSelectedItemString(deafultLangitems);
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

        String timeSet ;
        if (hours > 12) {
            hours -= 12;
            timeSet = "PM";
        } else if (hours == 0) {
            hours += 12;
            timeSet = "AM";
        } else if (hours == 12)
            timeSet = "PM";
        else
            timeSet = "AM";


        String minutes ;
        if (mins < 10)
            minutes = "0" + mins;
        else
            minutes = String.valueOf(mins);

        // Append in a StringBuilder
        return String.valueOf(hours) + ':' + minutes + " " + timeSet;
    }
}