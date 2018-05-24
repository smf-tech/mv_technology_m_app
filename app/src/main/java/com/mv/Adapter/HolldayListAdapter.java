package com.mv.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.CardView;
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
import com.mv.Model.HolidayListModel;
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

public class HolldayListAdapter extends RecyclerView.Adapter<HolldayListAdapter.MyViewHolder> {

    private List<HolidayListModel> calenderlsList;
    private Activity mContext;
    private PreferenceHelper preferenceHelper;
    private int position;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView state, district, taluka, name, detail, index,title;
        public CardView layout;
        ImageView delete;

        public MyViewHolder(View view) {
            super(view);
/*            state = (TextView) view.findViewById(R.id.txtTemplateName);
            district = (TextView) view.findViewById(R.id.txtTemplateName);
            taluka = (TextView) view.findViewById(R.id.txtTemplateName);*/
            layout = (CardView) view.findViewById(R.id.ll_calender_layout_card);
            title = (TextView) view.findViewById(R.id.tv_piachart);
            detail = (TextView) view.findViewById(R.id.tv_piachart_description);
            index = (TextView) view.findViewById(R.id.tv_piachart_number);
            delete = (ImageView) view.findViewById(R.id.iv_calender_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position=getAdapterPosition();

                }
            });


        }
    }


    public HolldayListAdapter(Activity context, List<HolidayListModel> moviesList) {
        this.calenderlsList = moviesList;
        this.mContext = context;
        preferenceHelper = new PreferenceHelper(context);

    }

    @Override
    public HolldayListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_trainning_calender, parent, false);

        return new HolldayListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(HolldayListAdapter.MyViewHolder holder, int position) {

        holder.index.setVisibility(View.GONE);
        holder.title.setVisibility(View.VISIBLE);
        holder.delete.setVisibility(View.GONE);

         calenderlsList.get(position);

        holder.detail.setText(calenderlsList.get(position).getHoliday_Date__c());
        holder.title.setText(calenderlsList.get(position).getDescription__c());


    }

    @Override
    public int getItemCount() {
        return calenderlsList.size();
    }









}
