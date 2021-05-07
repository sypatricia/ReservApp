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

public class AdapterReservationList extends ArrayAdapter<ModelReservationListItem> {
    private Context mContext;
    ArrayList<ModelReservationListItem> mObjects;
    private int mResource;

    public AdapterReservationList(@NonNull Context context, int resource, @NonNull ArrayList<ModelReservationListItem> objects) {
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

        TextView txtDriver = customView.findViewById(R.id.txtDriver);
        TextView txtTime = customView.findViewById(R.id.txtTime);
        TextView txtFrom = customView.findViewById(R.id.txtFrom);
        TextView txtDestination = customView.findViewById(R.id.txtDestination);

        txtDriver.setText(getItem(position).getDriver());
        txtTime.setText(getItem(position).getSched());
        txtFrom.setText(getItem(position).getFrom());
        txtDestination.setText(getItem(position).getDestination());

        return customView;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }
}
