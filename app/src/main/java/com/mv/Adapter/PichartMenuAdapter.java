package com.mv.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.data.PieEntry;
import com.mv.R;
import com.mv.Utils.PreferenceHelper;

import java.util.List;

/**
 * Created by nanostuffs on 11-01-2018.
 */

public class PichartMenuAdapter extends RecyclerView.Adapter<PichartMenuAdapter.MyViewHolder> {

    private List<PieEntry> teplateList;

    private List<Integer> colorList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCommunityName, bacColor;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            txtCommunityName = view.findViewById(R.id.txtTemplateName);
            bacColor = view.findViewById(R.id.temp_color);
            layout = view.findViewById(R.id.layoutTemplate);

        }
    }


    public PichartMenuAdapter(List<PieEntry> moviesList, List<Integer> colorList, Activity context) {
        this.teplateList = moviesList;
        Activity mContext = context;
        this.colorList = colorList;
        PreferenceHelper preferenceHelper = new PreferenceHelper(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_menu_color, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bacColor.setBackgroundColor(colorList.get((position % colorList.size())));
        holder.txtCommunityName.setText(teplateList.get(position).getLabel());
    }

    @Override
    public int getItemCount() {
        return teplateList.size();
    }


}