package com.mv.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Activity.CalenderFliterActivity;
import com.mv.ActivityMenu.TrainingCalender;
import com.mv.Model.CalenderEvent;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TraingCalenderAadapter  extends RecyclerView.Adapter<TraingCalenderAadapter.MyViewHolder> {

    private List<CalenderEvent> calenderlsList;
    private Activity mContext;
    private PreferenceHelper preferenceHelper;
    private int position;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView state, district, taluka, name, detail, index,title;
        public LinearLayout layout;
        ImageView delete;

        public MyViewHolder(View view) {
            super(view);
/*            state = (TextView) view.findViewById(R.id.txtTemplateName);
            district = (TextView) view.findViewById(R.id.txtTemplateName);
            taluka = (TextView) view.findViewById(R.id.txtTemplateName);*/
            layout = (LinearLayout) view.findViewById(R.id.ll_calender_layout);
            title = (TextView) view.findViewById(R.id.tv_piachart);
            detail = (TextView) view.findViewById(R.id.tv_piachart_description);
            index = (TextView) view.findViewById(R.id.tv_piachart_number);
            delete = (ImageView) view.findViewById(R.id.iv_calender_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position=getAdapterPosition();
                    showDeleteDialog();
                }
            });
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(calenderlsList.get(position).getMV_User1__c()!=null) {

                        if (calenderlsList.get(getAdapterPosition()).getMV_User1__c().equals(User.getCurrentUser(mContext).getMvUser().getId())) {
                            Intent intent = new Intent(mContext, CalenderFliterActivity.class);
                            intent.putExtra(Constants.My_Calendar, calenderlsList.get(getAdapterPosition()));
                            mContext.startActivity(intent);
                        }
                    }
                }
            });


        }
    }


    public TraingCalenderAadapter(Activity context, List<CalenderEvent> moviesList) {
        this.calenderlsList = moviesList;
        this.mContext = context;
        preferenceHelper = new PreferenceHelper(context);

    }

    @Override
    public TraingCalenderAadapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_trainning_calender, parent, false);

        return new TraingCalenderAadapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TraingCalenderAadapter.MyViewHolder holder, int position) {


        holder.index.setText(String.valueOf(position + 1));
         holder.title.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
            CalenderEvent piaChartModel= (CalenderEvent) calenderlsList.get(position);
            holder.detail.setText(piaChartModel.getDescription());
            holder.title.setText(piaChartModel.getTitle());
            if(calenderlsList.get(position).getMV_User1__c()!=null) {
                if (calenderlsList.get(position).getMV_User1__c().equals(User.getCurrentUser(mContext).getMvUser().getId())) {
                    holder.delete.setImageResource(R.drawable.form_delete);
                    holder.delete.setVisibility(View.VISIBLE);
                } else {
                    holder.delete.setVisibility(View.GONE);
                }

            }
            else
            {
                holder.delete.setVisibility(View.GONE);
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
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        // Setting OK Button
        alertDialog.setButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if(calenderlsList.get(position)instanceof CalenderEvent)
                    deleteEvent(((CalenderEvent) calenderlsList.get(position)));

            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    private void deleteEvent(final CalenderEvent calenderEvent) {
        Utills.showProgressDialog(mContext, "Loading ",mContext. getString(R.string.progress_please_wait));
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
                        if( jsonObject.getString("Status").equals("true"))
                        {
                            removeAt(position,calenderEvent);
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


    public void removeAt(int position,CalenderEvent calenderEvent) {
        calenderlsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, calenderlsList.size());
        AppDatabase.getAppDatabase(mContext).userDao().deleteCalenderEvent(calenderEvent.getId());
        ((TrainingCalender)mContext).removeEvent(calenderEvent.getDate());
    }
}






