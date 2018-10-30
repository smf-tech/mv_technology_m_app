package com.sujalamsufalam.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sujalamsufalam.ActivityMenu.TrainingCalender;
import com.sujalamsufalam.Model.CalenderEvent;
import com.sujalamsufalam.Model.PiaChartModel;
import com.sujalamsufalam.R;
import com.sujalamsufalam.Retrofit.ApiClient;
import com.sujalamsufalam.Retrofit.AppDatabase;
import com.sujalamsufalam.Retrofit.ServiceRequest;
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

/**
 * Created by Abhi on 30-11-2017.
 */

public class PichartDescriptiveListAdapter extends RecyclerView.Adapter<PichartDescriptiveListAdapter.MyViewHolder> {

    private List<?> piaChartModelsList;
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
            taluka = (TextView) view.findViewById(R.id.txtTemplateName);
            name = (TextView) view.findViewById(R.id.txtTemplateName);*/
            title = view.findViewById(R.id.tv_piachart);
            detail = view.findViewById(R.id.tv_piachart_description);
            index = view.findViewById(R.id.tv_piachart_number);
            delete = view.findViewById(R.id.iv_calender_delete);
            delete.setOnClickListener(v -> {
                position=getAdapterPosition();
                showDeleteDialog();
            });

        }
    }


    public PichartDescriptiveListAdapter(Activity context, List<?> moviesList) {
        this.piaChartModelsList = moviesList;
        this.mContext = context;
        preferenceHelper = new PreferenceHelper(context);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_pichart_discreptive, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        holder.index.setText(String.valueOf(position + 1));
        if(piaChartModelsList.get(position)instanceof PiaChartModel)
        {
            PiaChartModel piaChartModel= (PiaChartModel) piaChartModelsList.get(position);
            holder.detail.setText(piaChartModel.getDetail());
            holder.title.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        }
        else
        {
            holder.title.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
            CalenderEvent piaChartModel= (CalenderEvent) piaChartModelsList.get(position);
            holder.detail.setText(piaChartModel.getDescription());
            holder.title.setText(piaChartModel.getTitle());
            holder.delete.setImageResource(R.drawable.form_delete);


        }



    }

    @Override
    public int getItemCount() {
        return piaChartModelsList.size();
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

            if(piaChartModelsList.get(position)instanceof CalenderEvent)
                deleteEvent(((CalenderEvent) piaChartModelsList.get(position)));

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


    private void removeAt(int position, CalenderEvent calenderEvent) {
        piaChartModelsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, piaChartModelsList.size());
        AppDatabase.getAppDatabase(mContext).userDao().deleteCalenderEvent(calenderEvent.getId());
        ((TrainingCalender)mContext).removeEvent(calenderEvent.getDate());
    }
}






