package com.mv.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.AlignSelf;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.mv.Model.HomeModel;
import com.mv.R;

import java.util.List;

/**
 * Created by nanostuffs on 19-01-2018.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

    private List<HomeModel> menuList;

    private Context mContext;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView menu_name;
        public ImageView menu_icon, img_lock;
        public LinearLayout layout;
        public RelativeLayout invisiblityLayout;

        public MyViewHolder(View view) {
            super(view);

            menu_name = (TextView) view.findViewById(R.id.tv_home_menu_name);
            menu_icon = (ImageView) view.findViewById(R.id.iv_home_menu_icon);


            layout = (LinearLayout) view.findViewById(R.id.layout_home);
            invisiblityLayout = (RelativeLayout) view.findViewById(R.id.invisiblityLayout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (menuList.get(getAdapterPosition()).getAccessible()) {
                        Intent openClass = new Intent(mContext, menuList.get(getAdapterPosition()).getDestination());
                        mContext.startActivity(openClass);
                    } else {

                    }
                }
            });
        }

        void bindTo(String drawable) {
            menu_name.setText(drawable);
            ViewGroup.LayoutParams lp = menu_name.getLayoutParams();
            if (lp instanceof FlexboxLayoutManager.LayoutParams) {
                FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
                flexboxLp.setFlexGrow(1.0f);
                flexboxLp.setAlignSelf(AlignSelf.BASELINE);
                //flexboxLp.setAlignSelf(AlignSelf.FLEX_END);
            }
        }
    }


    public HomeAdapter(List<HomeModel> menuList, Context context) {
        this.menuList = menuList;
        this.mContext = context;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_home, parent, false);
/*        int height = parent.getMeasuredHeight() / 4;
        itemView.setMinimumHeight(height);*/
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Log.d("position", String.valueOf(position));
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.blink);
        holder.itemView.startAnimation(animation);
        holder.menu_icon.setImageResource(menuList.get(position).getMenuIcon());
        holder.bindTo(menuList.get(position).getMenuName());
        if (!menuList.get(position).getAccessible()) {
            //  holder.layout.setBackgroundColor(mContext.getColor(R.color.blue));
            // holder..setBackgroundColor(mContext.getColor(R.color.tranparant_lighter_grey));

            holder.invisiblityLayout.setVisibility(View.VISIBLE);
        } else {
            holder.invisiblityLayout.setVisibility(View.GONE);
        }

        //holder.menu_icon.setImageResource(menuList.get(position).getMenuIcon());
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static void openActivity(Activity source, Class<?> destination) {
        Intent openClass = new Intent(source, destination);
        source.startActivity(openClass);
        source.overridePendingTransition(R.anim.right_in, R.anim.left_out);
        /*if (Util.isLollipop()) {
            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(source, view, mTransitionName);
            source.startActivity(openClass, transitionActivityOptions.toBundle());
        } else {

        }*/
    }

}