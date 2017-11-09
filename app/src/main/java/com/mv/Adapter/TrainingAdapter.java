

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
import com.mv.R;
import com.mv.Utils.Constants;

import java.util.Arrays;
import java.util.List;

/**
 * Created by acer on 9/8/2016.
 */


public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.ViewHolder> {

    private Context mContext;
    private Resources resources;
    private List<String> name;

    public TrainingAdapter(Context context) {
        mContext = context;
        resources = context.getResources();
        name = Arrays.asList(resources.getStringArray(R.array.array_of_training));
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_trainging, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return 14;
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
                    String url = "file:///" + Environment.getExternalStorageDirectory().getPath() + "/MV_e-learning_Mar/Modules/" + (getAdapterPosition() + 1) + "/story_html5.html";

                    Intent intent = new Intent(mContext, ActivityWebView.class);
                    intent.putExtra(Constants.URL, url);
                    intent.putExtra(Constants.TITLE, name.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                }
            });
            txtName = (TextView) itemLayoutView.findViewById(R.id.txtName);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtCount.setText("" + (position + 1) + ". ");
        holder.txtName.setText(name.get(position));
    }


}

