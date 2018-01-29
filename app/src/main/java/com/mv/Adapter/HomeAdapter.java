package com.mv.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.AlignSelf;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.mv.Fragment.GroupsFragment;
import com.mv.Model.Community;
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
        public ImageView menu_icon;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);


            menu_name = (TextView) view.findViewById(R.id.tv_home_menu_name);
            menu_icon = (ImageView) view.findViewById(R.id.iv_home_menu_icon);
            layout = (LinearLayout) view.findViewById(R.id.layout_home);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent openClass = new Intent(mContext, menuList.get(getAdapterPosition()).getDestination());
                    mContext.startActivity(openClass);

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


    public HomeAdapter(List<HomeModel> menuList,Context context) {
        this.menuList = menuList;
        this.mContext = context;

    }

    @Override
    public HomeAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_home, parent, false);
/*        int height = parent.getMeasuredHeight() / 4;
        itemView.setMinimumHeight(height);*/
        return new HomeAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(HomeAdapter.MyViewHolder holder, int position) {


        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.blink);
        holder.itemView.startAnimation(animation);
        holder.menu_icon.setImageResource(menuList.get(position).getMenuIcon());
        holder.bindTo(menuList.get(position).getMenuName());
        //holder.menu_icon.setImageResource(menuList.get(position).getMenuIcon());
    }

    @Override
    public int getItemCount() {
        return menuList.size() ;
    }
    public static void openActivity(Activity source, Class<?> destination ) {
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