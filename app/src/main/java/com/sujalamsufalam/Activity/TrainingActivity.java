package com.sujalamsufalam.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sujalamsufalam.Adapter.TrainingAdapter;
import com.sujalamsufalam.R;


/**
 * Created by acer on 6/26/2017.
 */

public class TrainingActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img_back, img_list;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private RecyclerView recyclerView;
    private TrainingAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initialize();
        setRecyclerView();
    }

    private void setRecyclerView() {
        // adapter = new TrainingAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * method to initialize all views
     */
    private void initialize() {
        setActionbar(getString(R.string.MV_training));
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

    }

    /**
     * method to set ActionBar
     *
     * @param Title
     */
    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.INVISIBLE);
        img_list.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);

    }
}
