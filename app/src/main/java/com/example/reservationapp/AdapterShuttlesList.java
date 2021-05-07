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

public class AdapterShuttlesList extends ArrayAdapter<ModelShuttleListItem> {
    private Context mContext;
    ArrayList<ModelShuttleListItem> mObjects;
    private int mResource;


    public AdapterShuttlesList(@NonNull Context context, int resource, @NonNull ArrayList<ModelShuttleListItem> objects) {
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

        TextView txtDriverName = customView.findViewById(R.id.txtDriverName);
        TextView txtStatus = customView.findViewById(R.id.txtStatus);
        TextView txtDestination = customView.findViewById(R.id.txtDestination);

        txtDriverName.setText(getItem(position).getDriver());
        txtStatus.setText(getItem(position).getStatus());
        txtDestination.setText(getItem(position).getDestination());

        return customView;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }
}