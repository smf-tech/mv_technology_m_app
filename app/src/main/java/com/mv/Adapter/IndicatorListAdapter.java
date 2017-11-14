package com.mv.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.ActivityWebView;
import com.mv.Activity.IndicatorTaskList;
import com.mv.R;
import com.mv.Utils.Constants;

import java.util.Arrays;
import java.util.List;

/**
 * Created by nanostuffs on 14-11-2017.
 */

public class IndicatorListAdapter extends RecyclerView.Adapter<IndicatorListAdapter.ViewHolder> {

    private Context mContext;
    private Resources resources;
    private List<String> name;

    public IndicatorListAdapter(Context context) {
        mContext = context;
        resources = context.getResources();
        name = Arrays.asList(resources.getStringArray(R.array.array_of_indicator));
    }


    @Override
    public IndicatorListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_trainging, parent, false);

        // create ViewHolder
        IndicatorListAdapter.ViewHolder viewHolder = new IndicatorListAdapter.ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return name.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtCount, txtName;
        RelativeLayout layoutMain;

        public ViewHolder(View itemLayoutView) {

            super(itemLayoutView);
            layoutMain = (RelativeLayout) itemLayoutView.findViewById(R.id.layoutMain);
            txtCount = (TextView) itemLayoutView.findViewById(R.id.txtCount);
            layoutMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///" + Environment.getExternalStorageDirectory().getPath() + "/MV_e-learning_Mar/Modules/1/story_html5.html"));
                    mContext.startActivity(browserIntent);*/
                    /*String url = "file:///" + Environment.getExternalStorageDirectory().getPath() + "/MV_e-learning_Mar/Modules/" + (getAdapterPosition() + 1) + "/story_html5.html";

                    Intent intent = new Intent(mContext, ActivityWebView.class);
                    intent.putExtra(Constants.URL, url);
                    intent.putExtra(Constants.TITLE, name.get(getAdapterPosition()));
                    mContext.startActivity(intent);*/
                    Intent intent = new Intent(mContext, IndicatorTaskList.class);
                    intent.putExtra(Constants.TITLE, name.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                }
            });
            txtName = (TextView) itemLayoutView.findViewById(R.id.txtName);
        }
    }

    @Override
    public void onBindViewHolder(IndicatorListAdapter.ViewHolder holder, int position) {
      //  holder.txtCount.setText("" + (position ) + ". ");
        holder.txtName.setText(name.get(position));
    }


}

