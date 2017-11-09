package com.mv.Fragment;

/**
 * Created by Rohit Gujar on 09-10-2017.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mv.Adapter.TrainingAdapter;
import com.mv.R;

public class TrainingFragment extends Fragment {

    private RecyclerView recyclerView;
    private View view;
    private TrainingAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_training, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        setRecyclerView();
        return view;
    }

    private void setRecyclerView() {
        adapter = new TrainingAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
