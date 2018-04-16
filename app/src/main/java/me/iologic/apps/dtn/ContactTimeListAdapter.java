package me.iologic.apps.dtn;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ContactTimeListAdapter extends ArrayAdapter<ContactTimeList> {
    private static final String TAG = "ContactListAdapter";
    private Context mContext;

    public ContactTimeListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ContactTimeList> objects, Context mContext) {
        super(context, resource, objects);
        this.mContext = mContext;
        this.resource = mResource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String deviceName = getItem(position).getDeviceName();
        String deviceConnectedTime = getItem(position).getDeviceConnectedTime();
        String interContactTime = getItem(position).getInterContactTime();

        ContactTimeList contactTimeList = new ContactTimeList(deviceName, deviceConnectedTime, interContactTime);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvDeciveName = (TextView) convertView.findViewById(R.id.deviceName);
        TextView tvDeviceConnectedTime = (TextView) convertView.findViewById(R.id.connectionStartedTime);
        TextView tvInterContactTime = (TextView) convertView.findViewById(R.id.interContactTime);

        tvDeciveName.setText(deviceName);
        tvDeviceConnectedTime.setText(deviceConnectedTime);
        tvInterContactTime.setText(interContactTime);

        return  convertView;
    }
}
