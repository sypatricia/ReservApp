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

public class AdapterTransitList extends ArrayAdapter<ModelTransitListItem> {
    private Context mContext;
    ArrayList<ModelTransitListItem> mObjects;
    private int mResource;


    public AdapterTransitList(@NonNull Context context, int resource, @NonNull ArrayList<ModelTransitListItem> objects) {
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

        TextView txtName = customView.findViewById(R.id.txtName);
        TextView txtFrom = customView.findViewById(R.id.txtFrom);
        TextView txtTo = customView.findViewById(R.id.txtTo);

        txtFrom.setText(getItem(position).getFrom());
        txtTo.setText(getItem(position).getTo());
        txtName.setText(getItem(position).getDriver());

        return customView;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }
}
