package org.openintents.timesheet.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.openintents.timesheet.R;

public class RateListAdapter extends BaseAdapter {
    static final long[][] rates;
    private Context mContext;
    private LayoutInflater mInflater;

    static {
        long[][] r0 = new long[2][];
        long[] jArr = new long[5];
        jArr[0] = 100;
        jArr[1] = 120;
        jArr[2] = 600000;
        r0[1] = jArr;
        rates = r0;
    }

    public RateListAdapter(Context context) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
    }

    public int getCount() {
        return rates.length;
    }

    public Object getItem(int position) {
        return rates[position];
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rateView;
        if (convertView == null) {
            rateView = this.mInflater.inflate(17367055, parent, false);
        } else {
            rateView = convertView;
        }
        TextView text = (TextView) rateView.findViewById(16908308);
        if (rates[position] == null) {
            text.setText(R.string.simple_rate);
        } else {
            text.setText(this.mContext.getString(R.string.two_rates, new Object[]{rates[position]}));
        }
        return rateView;
    }
}
