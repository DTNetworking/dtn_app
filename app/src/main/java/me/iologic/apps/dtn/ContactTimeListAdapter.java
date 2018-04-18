package me.iologic.apps.dtn;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContactTimeListAdapter extends ArrayAdapter<ContactTimeList> {
    private static final String TAG = "ContactListAdapter";
    private Context mContext;
    int mResource;

    public ContactTimeListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ContactTimeList> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String deviceName = getItem(position).getDeviceName();
        String deviceConnectedTime = getItem(position).getDateAndTime();
        String interContactTime = getItem(position).getInterContactTime();

        ContactTimeList contactTimeList = new ContactTimeList(deviceName, deviceConnectedTime, interContactTime);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvDeciveName = (TextView) convertView.findViewById(R.id.deviceName);
        TextView tvDateAndTimeTime = (TextView) convertView.findViewById(R.id.dateAndTime);
        TextView tvInterContactTime = (TextView) convertView.findViewById(R.id.interContactTime);

        tvDeciveName.setText(deviceName);
        tvDateAndTimeTime.setText(deviceConnectedTime);
        tvInterContactTime.setText(interContactTime);

        return  convertView;
    }
}