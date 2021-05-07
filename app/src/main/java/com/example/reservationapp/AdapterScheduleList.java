package com.example.reservationapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class AdapterScheduleList extends ArrayAdapter<ModelScheduleListItem> {
    private Context mContext;
    ArrayList<ModelScheduleListItem> mObjects;
    private int mResource;

    public AdapterScheduleList(@NonNull Context context, int resource, @NonNull ArrayList<ModelScheduleListItem> objects) {
        super(context, resource, objects);

        this.mContext = context;
        this.mResource = resource;
        this.mObjects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        View customView = layoutInflater.inflate(mResource, parent, false);

        TextView txtTime = customView.findViewById(R.id.txtTime);

        txtTime.setText(getItem(position).getTime());

        return customView;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

}
