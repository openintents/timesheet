package org.openintents.timesheet.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.openintents.timesheet.R;

public class RateListAdapter extends BaseAdapter {

    private static final long[][] rates = new long[][]{null, // simple editable rate
            new long[]{100, 120, 600000, 0, 0}};
    private LayoutInflater mInflater;
    private Context mContext;

    public RateListAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    public int getCount() {
        return rates.length;
    }

    public Object getItem(int position) {
        return rates[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rateView;
        if (convertView == null) {
            rateView = mInflater.inflate(
                    android.R.layout.simple_list_item_single_choice, parent,
                    false);
        } else {
            rateView = convertView;
        }
        TextView text = (TextView) rateView.findViewById(android.R.id.text1);

        if (rates[position] == null) {
            text.setText(R.string.simple_rate);
        } else {
            text.setText(mContext.getString(R.string.two_rates));
        }

        return rateView;
    }
}
