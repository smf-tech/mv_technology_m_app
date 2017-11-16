package com.mv.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.HomeActivity;
import com.mv.Activity.IndicatorTaskList;
import com.mv.Activity.LocationSelectionActity;
import com.mv.Activity.PiachartActivity;
import com.mv.Activity.ProcessDeatailActivity;
import com.mv.Activity.ProcessListActivity;
import com.mv.Activity.TemplatesActivity;
import com.mv.Activity.UserApproveDetail;
import com.mv.Model.Task;
import com.mv.Model.TaskContainerModel;
import com.mv.Model.Template;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nanostuffs on 16-11-2017.
 */

public class TeamManagementAdapter extends RecyclerView.Adapter<TeamManagementAdapter.MyViewHolder> {

    private List<Template> teplateList;
    private Activity mContext;
    ArrayList<Task> programManagementProcessLists = new ArrayList<>();
    private PreferenceHelper preferenceHelper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = (TextView) view.findViewById(R.id.txtTemplateName);
            layout = (LinearLayout) view.findViewById(R.id.layoutTemplate);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Intent openClass = new Intent(mContext, UserApproveDetail.class);
                    openClass.putExtra(Constants.ID,teplateList.get(getAdapterPosition()).getId());
                    //  openClass.putExtra(Constants.PROCESS_ID, taskList);
                   // openClass.putParcelableArrayListExtra(Constants.PROCESS_ID, Utills.convertStringToArrayList(taskContainerModel.getTaskListString()));
                    //  openClass.putExtra("stock_list", resultList.get(programManagementProcessLists()).get(0));
                    mContext.startActivity(openClass);
                    mContext.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }


            });
        }
    }


    public TeamManagementAdapter(List<Template> moviesList, Activity context) {
        this.teplateList = moviesList;
        this.mContext = context;
        preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public TeamManagementAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_template, parent, false);

        return new TeamManagementAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TeamManagementAdapter.MyViewHolder holder, int position) {
        Template template = teplateList.get(position);
        holder.txtCommunityName.setText(template.getName());
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }








}