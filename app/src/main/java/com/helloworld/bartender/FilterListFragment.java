package com.helloworld.bartender;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.helloworld.bartender.Item.Item;
import com.helloworld.bartender.adapter.FilterListAdapter_making;

import java.util.ArrayList;

public class FilterListFragment extends Fragment {
    private RecyclerView recyclerView;
    private DatabaseHelper databaseHelper;
    private ArrayList<Item> arrayList = new ArrayList<Item>();
    private Cursor cursor;
    private FilterListAdapter_making adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup viewGroup=(ViewGroup) inflater.inflate(R.layout.activity_filter_making_page,container,false);
        loadDatabase();
        return viewGroup;

    }

    public void loadDatabase(){
        databaseHelper =new DatabaseHelper(getActivity());
        try {
            databaseHelper.checkAndCopyDatabase();
            databaseHelper.openDatabase();
        }catch (SQLiteException e){
            e.printStackTrace();
        }
        try {
            cursor = databaseHelper.QueryData("select * from filter_list");
            if (cursor != null) {
                do {
                    Item item = new Item();
                    item.setFilter_name(cursor.getString(2));
                    item.setAttributes(cursor.getString(1));
                    arrayList.add(item);
                } while (cursor.moveToNext());
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter= new FilterListAdapter_making(getActivity(),arrayList);
        adapter.setOnTapListener(new OnTapListener() {
            @Override
            public void OnTapView(int position) {
                Toast.makeText(getContext(),"Click to "+position,Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

    }

}
