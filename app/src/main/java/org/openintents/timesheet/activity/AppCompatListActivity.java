package org.openintents.timesheet.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

class AppCompatListActivity extends AppCompatActivity {
    private ListView mListView;


    protected ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) findViewById(android.R.id.list);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onListItemClick(parent, view, position, id);
                }
            });
        }
        return mListView;
    }

    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    protected ListAdapter getListAdapter() {
        return getListView().getAdapter();
    }

    protected Object getSelectedItem() {
        return getListView().getSelectedItem();
    }

    protected long getSelectedItemId() {
        return getListView().getSelectedItemId();
    }

    protected void onListItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

}
