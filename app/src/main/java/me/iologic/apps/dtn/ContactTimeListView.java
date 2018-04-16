package me.iologic.apps.dtn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class ContactTimeListView extends AppCompatActivity {
    private static final String TAG = "ContactTimeListView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacttime_list_view);
        ListView myListView = (ListView) findViewById(R.id.listview);

        Bundle extras = getIntent().getExtras();

        ArrayList<ContactTimeList> ReceivedContactTimeList = extras.getParcelable("contactTimeListArray");

        ContactTimeListAdapter adapter = new ContactTimeListAdapter(this, R.layout.adapter_view_layout, ReceivedContactTimeList);
        myListView.setAdapter(adapter);
    }
}
