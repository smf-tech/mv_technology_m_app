package com.mv.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

import com.mv.Activity.ProcessDeatailActivity;
import com.mv.Model.Task;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Widgets.MultiSelectionSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by nanostuffs on 26-09-2017.
 */

public class ProcessDetailAdapter extends RecyclerView.Adapter<ProcessDetailAdapter.MyViewHolder> {

    private List<Task> taskList;
    private Activity mContext;
    PreferenceHelper preferenceHelper;
    ArrayList<String> myList;
    ArrayAdapter<String> dimen_adapter;
    boolean[] mSelection = null;
    final String[] items = null;
    String value;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextInputLayout inputLayout, dateInpute;
        LinearLayout llLayout, llHeaderLay, llLocation, llCheck, llMutiselect;
        EditText questionResponse, date;
        TextView question, header, locHeader, locText, checkText, multispsinnerText;
        Spinner spinnerResponse;
        MultiSelectionSpinner multiSelect;
        CheckBox checkBox;

        public MyViewHolder(View view) {
            super(view);

            llLayout = (LinearLayout) view.findViewById(R.id.ll_spinner_layout);
            llLocation = (LinearLayout) view.findViewById(R.id.ll_location_layout);
            llHeaderLay = (LinearLayout) view.findViewById(R.id.ll_headerr_lay);
            llCheck = (LinearLayout) view.findViewById(R.id.check_lay);
            //   llMutiselect = (LinearLayout) view.findViewById(R.id.ll_multispinner_lay);

            question = (TextView) view.findViewById(R.id.tv_pd_question);
            header = (TextView) view.findViewById(R.id.tv_header);
            ///location layout
            locHeader = (TextView) view.findViewById(R.id.tv_loc_hed);
            locText = (TextView) view.findViewById(R.id.loc_text);
            spinnerResponse = (Spinner) view.findViewById(R.id.sp_response);
            //    multiSelect = (MultiSelectionSpinner) view.findViewById(R.id.multi_spinner);
            //date  and timelayout

            dateInpute = (TextInputLayout) view.findViewById(R.id.input_content_date);
            date = (EditText) view.findViewById(R.id.et_process_detail_date);
            date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (taskList.get(getAdapterPosition()).getTask_type__c().equals(Constants.DATE))
                        showDateDialog(mContext, getAdapterPosition());
                    else if (taskList.get(getAdapterPosition()).getTask_type__c().equals(Constants.MULTI_SELECT)) {
                        myList = new ArrayList<String>(Arrays.asList(getColumnIdex((taskList.get(getAdapterPosition()).getPicklist_Value__c()).split(","))));
                        showDialog(myList, getAdapterPosition());

                    } else if (taskList.get(getAdapterPosition()).getTask_type__c().equals(Constants.TIME)) {
                        Calendar mcurrentTime = Calendar.getInstance();
                        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                        int minute = mcurrentTime.get(Calendar.MINUTE);
                        TimePickerDialog mTimePicker;
                        mTimePicker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                taskList.get(getAdapterPosition()).setTask_Response__c(updateTime(selectedHour, selectedMinute));
                                notifyItemChanged(getAdapterPosition());
                            }
                        }, hour, minute, false);//Yes 24 hour time
                        mTimePicker.setTitle("Select Time");
                        mTimePicker.show();

                    }

                }
            });
            //text and multiline
            inputLayout = (TextInputLayout) view.findViewById(R.id.input_content);
            questionResponse = (EditText) view.findViewById(R.id.et_process_detail);
            questionResponse.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    //  taskList.get(getAdapterPosition()).setTask_Response__c(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    taskList.get(getAdapterPosition()).setTask_Response__c(s.toString());
                    ((ProcessDeatailActivity) mContext).saveDataToList(taskList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            //spinner
            spinnerResponse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0)
                        taskList.get(getAdapterPosition()).setTask_Response__c("");
                    else
                        taskList.get(getAdapterPosition()).setTask_Response__c(parent.getItemAtPosition(position).toString());

                    ((ProcessDeatailActivity) mContext).saveDataToList(taskList.get(getAdapterPosition()), getAdapterPosition());

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            checkText = (TextView) view.findViewById(R.id.check_header);
            checkBox = (CheckBox) view.findViewById(R.id.detail_chk);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        taskList.get(getAdapterPosition()).setTask_Response__c("true");

                        ((ProcessDeatailActivity) mContext).saveDataToList(taskList.get(getAdapterPosition()), getAdapterPosition());
                    } else {
                        taskList.get(getAdapterPosition()).setTask_Response__c("false");

                        ((ProcessDeatailActivity) mContext).saveDataToList(taskList.get(getAdapterPosition()), getAdapterPosition());

                    }
                }
            });


        }
    }


    public ProcessDetailAdapter(Activity context, List<Task> taskList) {
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
        Task task = taskList.get(position);
        if (!preferenceHelper.getBoolean(Constants.IS_EDITABLE) && !preferenceHelper.getBoolean(Constants.NEW_PROCESS)) {
            holder.questionResponse.setEnabled(false);
            holder.spinnerResponse.setEnabled(false);
        }

        switch (task.getTask_type__c().trim()) {

            case Constants.TASK_TEXT:
                holder.inputLayout.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.GONE);
                // holder.questionResponse.setHint(task.getTask_Text__c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.inputLayout.setHint("* " + task.getTask_Text__c());
                else
                    holder.inputLayout.setHint(task.getTask_Text__c());
                if (!preferenceHelper.getBoolean(Constants.NEW_PROCESS))
                    holder.questionResponse.setText(task.getTask_Response__c());
                if (task.getValidation().equals("Alphabets")) {
                    //  holder.questionResponse.setInputType();
                    holder.questionResponse.setInputType(InputType.TYPE_CLASS_TEXT);
                } else if (task.getValidation().equals("Number")) {
                    holder.questionResponse.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                break;
            case Constants.TASK_SELECTION:

                if (task.getIs_Response_Mnadetory__c())
                    holder.question.setText("*" + task.getTask_Text__c());
                else
                    holder.question.setText(task.getTask_Text__c());

                holder.llHeaderLay.setVisibility(View.GONE);
                holder.inputLayout.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.VISIBLE);

                myList = new ArrayList<String>(Arrays.asList(getColumnIdex(("Select," + task.getPicklist_Value__c()).split(","))));
                dimen_adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, myList);
                dimen_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.spinnerResponse.setPrompt(task.getTask_Text__c());
                holder.spinnerResponse.setAdapter(dimen_adapter);
                if (!preferenceHelper.getBoolean(Constants.NEW_PROCESS))
                    holder.spinnerResponse.setSelection(myList.indexOf(task.getTask_Response__c().trim()));

                break;
            case Constants.MULTI_LINE:
                holder.inputLayout.setVisibility(View.VISIBLE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                // holder.questionResponse.setHint(task.getTask_Text__c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.inputLayout.setHint("*" + task.getTask_Text__c());
                else
                    holder.inputLayout.setHint(task.getTask_Text__c());
                holder.questionResponse.setMinLines(3);
                holder.questionResponse.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                holder.questionResponse.setGravity(Gravity.LEFT | Gravity.TOP);
                if (!preferenceHelper.getBoolean(Constants.NEW_PROCESS))
                    holder.questionResponse.setText(task.getTask_Response__c());
                break;
            case Constants.HEADER:
                holder.llHeaderLay.setVisibility(View.VISIBLE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.inputLayout.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.GONE);
                holder.header.setText(task.getTask_Text__c());
                break;
            case Constants.LOCATION:
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.inputLayout.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.VISIBLE);
                if (task.getIs_Response_Mnadetory__c())
                    holder.locHeader.setText("* " + task.getTask_Text__c());
                else
                    holder.locHeader.setText(task.getTask_Text__c());
                holder.locText.setText(task.getTask_Response__c());
                break;
            case Constants.DATE:
                holder.inputLayout.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.VISIBLE);
                // holder.questionResponse.setHint(task.getTask_Text__c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.dateInpute.setHint("*" + task.getTask_Text__c());
                else
                    holder.dateInpute.setHint(task.getTask_Text__c());
                holder.date.setText(task.getTask_Response__c());
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);


                break;
            case Constants.TIME:
                holder.inputLayout.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.VISIBLE);
                // holder.questionResponse.setHint(task.getTask_Text__c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.dateInpute.setHint("*" + task.getTask_Text__c());
                else
                    holder.dateInpute.setHint(task.getTask_Text__c());
                holder.date.setText(task.getTask_Response__c());
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);
                break;
            case Constants.CHECK_BOX:

                holder.inputLayout.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.GONE);
                holder.llCheck.setVisibility(View.VISIBLE);

                if (task.getIs_Response_Mnadetory__c())
                    holder.checkText.setText("*" + task.getTask_Text__c());
                else
                    holder.checkText.setText(task.getTask_Text__c());

                break;
            case Constants.MULTI_SELECT:
                holder.inputLayout.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.VISIBLE);
                holder.llCheck.setVisibility(View.GONE);
                // holder.questionResponse.setHint(task.getTask_Text__c());
                if (task.getIs_Response_Mnadetory__c())
                    holder.dateInpute.setHint("*" + task.getTask_Text__c());
                else
                    holder.dateInpute.setHint(task.getTask_Text__c());
                holder.date.setText(task.getTask_Response__c());
                holder.date.setTag(position);
                holder.date.setFocusable(false);
                holder.date.setClickable(true);

                break;
            default:
                holder.llHeaderLay.setVisibility(View.GONE);
                holder.inputLayout.setVisibility(View.GONE);
                holder.llLocation.setVisibility(View.GONE);
                holder.llLayout.setVisibility(View.GONE);
                holder.inputLayout.setVisibility(View.GONE);
                holder.dateInpute.setVisibility(View.GONE);
                holder.llMutiselect.setVisibility(View.GONE);
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
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        taskList.get(Position).setTask_Response__c(getTwoDigit(dayOfMonth) + "/" + getTwoDigit(monthOfYear + 1) + "/" + year);
                        notifyItemChanged(Position);

                    }
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

        mSelection = new boolean[items.length];
        Arrays.fill(mSelection, false);

// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("Select template type")
                .setMultiChoiceItems(items, mSelection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (mSelection != null && which < mSelection.length) {
                            mSelection[which] = isChecked;
                            value = buildSelectedItemString(items);

                        } else {
                            throw new IllegalArgumentException(
                                    "Argument 'which' is out of bounds.");
                        }
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        taskList.get(pos).setTask_Response__c(value);
                        notifyItemChanged(pos);


                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
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

        String timeSet = "";
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


        String minutes = "";
        if (mins < 10)
            minutes = "0" + mins;
        else
            minutes = String.valueOf(mins);

        // Append in a StringBuilder
        String aTime = new StringBuilder().append(hours).append(':')
                .append(minutes).append(" ").append(timeSet).toString();

        return aTime;
    }
}