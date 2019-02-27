package com.mv.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mv.Activity.ProcessDeatailActivity;
import com.mv.Activity.ProcessListActivity;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.R;
import com.mv.Retrofit.AppDatabase;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanostuffs on 26-09-2017.
 */
public class ProcessListAdapter extends RecyclerView.Adapter<ProcessListAdapter.MyViewHolder> {

    private final List<TaskContainerModel> resultList;
    private ArrayList<ArrayList<Task>> taskArrayList = new ArrayList<>();
    private PreferenceHelper preferenceHelper;
    private ProcessListActivity _context;
    private Activity mContext;

    private Gson gson;
    private Type listType;
    private String processName;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtCommunityName, textViewColor;
        LinearLayout layout;
        LinearLayout arrowLay;
        ImageView arrowImg;

        public MyViewHolder(View view) {
            super(view);

            txtCommunityName = view.findViewById(R.id.txtTemplateName);
            textViewColor = view.findViewById(R.id.temp_color);
            layout = view.findViewById(R.id.layoutTemplate);
            arrowLay = view.findViewById(R.id.lay_delete);
            arrowImg = view.findViewById(R.id.row_img);

            layout.setOnClickListener(view1 -> {
                if (taskArrayList.size() > 0) {
                    preferenceHelper.insertString(Constants.UNIQUE, resultList.size() > getAdapterPosition() ?
                            resultList.get(getAdapterPosition()).getUnique_Id() : "");

                    preferenceHelper.insertBoolean(Constants.NEW_PROCESS, false);

                    String locationLevel = "";
                    ArrayList<Task> tasks = taskArrayList.get(getAdapterPosition());
                    for (Task task : tasks) {
                        if (task.getLocationLevel() == null || task.getLocationLevel().equals("null")) {
                            break;
                        }
                        locationLevel = task.getLocationLevel();
                    }

                    preferenceHelper.insertString(Constants.STATE_LOCATION_LEVEL, locationLevel);

                    Intent openClass = new Intent(mContext, ProcessDeatailActivity.class);
                    openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, taskArrayList.get(getAdapterPosition()));
                    openClass.putExtra(Constants.PROCESS_NAME, processName);

                    String structureList = resultList.get(getAdapterPosition()).getProAnsListString();
//                    openClass.putExtra(Constants.PICK_LIST_ID, structureList);
                    generateFileOnSD(structureList);

                    mContext.startActivity(openClass);
                    mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                } else {
                    Utills.showToast("No Task Available ", mContext);
                }
            });
        }
    }

    public void generateFileOnSD(String sBody) {
        try {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV";

            File root = new File(filePath);
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "ProAnsListString.txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(mContext, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ProcessListAdapter(List<TaskContainerModel> resultList, Activity context, String processName) {
        this.resultList = resultList;
        this.mContext = context;
        this.processName = processName;
        this.preferenceHelper = new PreferenceHelper(context);

        gson = new Gson();

        listType = new TypeToken<ArrayList<Task>>() {
        }.getType();

        for(int i=0;i<resultList.size();i++){
            ArrayList<Task> tasks = gson.fromJson(resultList.get(i).getTaskListString(), listType);
            taskArrayList.add(tasks);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_template, parent, false);

        return new MyViewHolder(itemView);
    }

    public void clearTaskList() {
        if (!taskArrayList.isEmpty()) {
            taskArrayList.clear();
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        if (resultList.get(position).getHeaderPosition().equals("")) {
            if (taskArrayList.get(position).get(0).getTimestamp__c() != null && !taskArrayList.get(position).get(0).getTimestamp__c().equals("null")) {
                holder.txtCommunityName.setText(Utills.getDate(
                        Long.valueOf(taskArrayList.get(position).get(0).getTimestamp__c()), "dd/MM/yyyy hh:mm:ss.SSS"));
            }
        } else {
            holder.txtCommunityName.setText(resultList.get(position).getHeaderPosition());
        }

        if (taskArrayList.get(position).get(0).getIsSave().equals("false")) {
            if (taskArrayList.get(position).get(0).getIsApproved__c().equals("false")) {
                holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            } else {
                if (taskArrayList.get(position).get(0).getStatus__c() != null && taskArrayList.get(position).get(0).getStatus__c().equalsIgnoreCase("Expected")) {
                    holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.purple));
                } else {
                    holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.green));
                }
            }

            if (!resultList.get(position).getIsDeleteAllow()) {
                holder.arrowLay.setVisibility(View.GONE);
            } else {
                holder.arrowImg.setBackgroundResource(R.drawable.form_delete);
                holder.arrowLay.setOnClickListener(v -> showFormDeletePopUp(position));
            }
        } else {
            holder.textViewColor.setBackgroundColor(mContext.getResources().getColor(R.color.red));
            holder.arrowImg.setBackgroundResource(R.drawable.form_delete);
            holder.arrowLay.setOnClickListener(v -> showFormDeletePopUp(position));
        }
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    @SuppressWarnings("deprecation")
    private void showFormDeletePopUp(final int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.app_logo);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(android.R.string.cancel), (dialog, which) -> alertDialog.dismiss());

        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), (dialog, which) -> {
            if (resultList != null && resultList.size() > 0) {
                if (mContext instanceof ProcessListActivity) {
                    _context = (ProcessListActivity) mContext;
                    if (resultList.get(position).getIsSave().equals("false")) {
                        _context.deleteForm(resultList.get(position), position);
                    } else {
                        AppDatabase.getAppDatabase(mContext).userDao().deleteSingleTask(
                                resultList.get(position).getUnique_Id(),
                                resultList.get(position).getMV_Process__c());
                        // Removed entry from local db
                        resultList.remove(position);
                        taskArrayList.remove(position);
                        notifyDataSetChanged();
                    }
                }
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}