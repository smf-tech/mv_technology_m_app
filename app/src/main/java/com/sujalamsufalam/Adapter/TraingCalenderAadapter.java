package com.sujalamsufalam.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sujalamsufalam.Activity.CalenderFliterActivity;
import com.sujalamsufalam.ActivityMenu.TrainingCalender;
import com.sujalamsufalam.Model.CalenderEvent;
import com.sujalamsufalam.Model.User;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.ApiClient;
import com.sujalamsufalam.Retrofit.AppDatabase;
import com.sujalamsufalam.Retrofit.ServiceRequest;
import com.sujalamsufalam.Utils.Constants;
import com.sujalamsufalam.Utils.PreferenceHelper;
import com.sujalamsufalam.Utils.Utills;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TraingCalenderAadapter extends RecyclerView.Adapter<TraingCalenderAadapter.MyViewHolder> {

    private List<CalenderEvent> calenderlsList;
    private Activity mContext;
    private PreferenceHelper preferenceHelper;
    private boolean isAllPlans;
    private int position;

    public TraingCalenderAadapter(Activity context, List<CalenderEvent> moviesList,boolean isAllPlans) {
        this.calenderlsList = moviesList;
        this.mContext = context;
        this.isAllPlans = isAllPlans;
        preferenceHelper = new PreferenceHelper(context);

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvProjectName, tvDateName, tvNoOfPeopleName, tvTotalExpenseName, tvStatusName;
        public LinearLayout lnr_content;
        ImageView imgDelete;

        public MyViewHolder(View view) {
            super(view);
/*            state = (TextView) view.findViewById(R.id.txtTemplateName);
            district = (TextView) view.findViewById(R.id.txtTemplateName);*/
            tvTotalExpenseName = view.findViewById(R.id.tvTotalExpenseName);
            lnr_content = view.findViewById(R.id.lnr_content);
            tvProjectName = view.findViewById(R.id.tvProjectName);
            tvDateName = view.findViewById(R.id.tvDateName);
            tvNoOfPeopleName = view.findViewById(R.id.tvNoOfPeopleName);

            tvStatusName = view.findViewById(R.id.tvStatusName);

            imgDelete = view.findViewById(R.id.imgDelete);
            imgDelete.setOnClickListener(v -> {
                position = getAdapterPosition();
                showDeleteDialog();
            });
            lnr_content.setOnClickListener(v -> {
                if (calenderlsList.get(position).getMV_User1__c() != null) {
                    CalenderEvent cc= calenderlsList.get(getAdapterPosition());
                //    if (calenderlsList.get(getAdapterPosition()).getMV_User1__c().equals(User.getCurrentUser(mContext).getMvUser().getId())) {
                        Intent intent = new Intent(mContext, CalenderFliterActivity.class);
                        intent.putExtra(Constants.My_Calendar, calenderlsList.get(getAdapterPosition()));
                        mContext.startActivity(intent);
                 //   }
                }
            });


        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_calendar, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        CalenderEvent piaChartModel = calenderlsList.get(position);
        holder.tvProjectName.setText(piaChartModel.getTitle());
        holder.tvDateName.setText(piaChartModel.getDescription());
        holder.tvNoOfPeopleName.setText(piaChartModel.getProceesSubmittedCount() + "/" + piaChartModel.getProceesTotalCount());
    //    holder.tvNoOfPeopleName.setText(piaChartModel.getProceesSubmittedCount());
        holder.tvTotalExpenseName.setText(piaChartModel.getCreatedUserData());
        holder.tvStatusName.setText(piaChartModel.getStatus());
        if (calenderlsList.get(position).getMV_User1__c() != null) {
            if (calenderlsList.get(position).getMV_User1__c().equals(User.getCurrentUser(mContext).getMvUser().getId())) {
                holder.imgDelete.setImageResource(R.drawable.form_delete);
                holder.imgDelete.setVisibility(View.VISIBLE);
            } else {
                holder.imgDelete.setVisibility(View.GONE);
                if(!isAllPlans){
                    holder.lnr_content.setVisibility(View.GONE);
                }
            }

        } else {
            holder.imgDelete.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return calenderlsList.size();
    }


    private void showDeleteDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_launcher);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(R.string.cancel), (dialog, which) -> alertDialog.dismiss());
        // Setting OK Button
        alertDialog.setButton(mContext.getString(R.string.ok), (dialog, which) -> {

            if (calenderlsList.get(position) != null)
                deleteEvent(calenderlsList.get(position));

        });

        // Showing Alert Message
        alertDialog.show();
    }


    private void deleteEvent(final CalenderEvent calenderEvent) {
        Utills.showProgressDialog(mContext, "Loading ", mContext.getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/deleteEventcalender?eventId=" + calenderEvent.getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                try {
                    if (response.isSuccess()) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if (jsonObject.getString("Status").equals("true")) {
                            removeAt(position, calenderEvent);
                        }


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }


    private void removeAt(int position, CalenderEvent calenderEvent) {
        calenderlsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, calenderlsList.size());
        AppDatabase.getAppDatabase(mContext).userDao().deleteCalenderEvent(calenderEvent.getId());
        ((TrainingCalender) mContext).removeEvent(calenderEvent.getDate());
    }
}






