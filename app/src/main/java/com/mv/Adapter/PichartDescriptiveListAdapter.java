package com.mv.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mv.Model.CalenderEvent;
import com.mv.Model.PiaChartModel;
import com.mv.Model.Task;
import com.mv.R;
import com.mv.Utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhi on 30-11-2017.
 */

public class PichartDescriptiveListAdapter extends RecyclerView.Adapter<PichartDescriptiveListAdapter.MyViewHolder> {

    private List<?> piaChartModelsList;
    private Activity mContext;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView state, district, taluka, name, detail, index;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
/*            state = (TextView) view.findViewById(R.id.txtTemplateName);
            district = (TextView) view.findViewById(R.id.txtTemplateName);
            taluka = (TextView) view.findViewById(R.id.txtTemplateName);
            name = (TextView) view.findViewById(R.id.txtTemplateName);*/
            detail = (TextView) view.findViewById(R.id.tv_piachart_description);
            index = (TextView) view.findViewById(R.id.tv_piachart_number);

        }
    }


    public PichartDescriptiveListAdapter(Activity context, List<?> moviesList) {
        this.piaChartModelsList = moviesList;
        this.mContext = context;

    }

    @Override
    public PichartDescriptiveListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_pichart_discreptive, parent, false);

        return new PichartDescriptiveListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PichartDescriptiveListAdapter.MyViewHolder holder, int position) {

      /*  holder.state.setText(piaChartModelsList.get(position).getState());
        holder.district.setText(piaChartModelsList.get(position).getDistrict());
        holder.taluka.setText(piaChartModelsList.get(position).getTaluka());
        holder.name.setText(piaChartModelsList.get(position).getName());*/
        holder.index.setText(String.valueOf(position + 1));
        if(piaChartModelsList.get(position)instanceof PiaChartModel)
        {
            PiaChartModel piaChartModel= (PiaChartModel) piaChartModelsList.get(position);
            holder.detail.setText(piaChartModel.getDetail());
        }
        else
        {
            CalenderEvent piaChartModel= (CalenderEvent) piaChartModelsList.get(position);
            holder.detail.setText(piaChartModel.getDescription());
        }



    }

    @Override
    public int getItemCount() {
        return piaChartModelsList.size();
    }

}






