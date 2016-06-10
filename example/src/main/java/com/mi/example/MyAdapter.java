package com.mi.example;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<JSONObject> mDataset;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<JSONObject> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject item = mDataset.get(position);

        String message = "";
        Integer timestamp = 0;
        try {
            message = item.getString("data");
            timestamp = item.getInt("timestamp");
        } catch (JSONException ignored) {
            // e.printStackTrace();
        }

        int os_res = -1;
        try {
            os_res = item.getInt("os_res");
        } catch (JSONException ignored) {
            // e.printStackTrace();
        }

        if (os_res != -1) {
            holder.icon.setImageResource(os_res);
        } else {
            holder.icon.setImageResource(R.drawable.oth_icon);
        }

        holder.text.setText(message);

        long milliseconds = new Date((long) timestamp * 1000).getTime();
        String date_string = getFormattedDate(holder.timestamp.getContext(), milliseconds);

        holder.timestamp.setText(date_string);
    }

    private String getFormattedDate(Context ctx, long timeInMilis) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(timeInMilis);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = ctx.getString(R.string.timeFormatString);
        final String dateTimeFormatString = ctx.getString(R.string.dateTimeFormatString);
        final String dateYearsFormatString = ctx.getString(R.string.dateYearsFormatString);
        final String yesterday = ctx.getString(R.string.yesterday);

        if (now.get(Calendar.DATE) == time.get(Calendar.DATE)) {
            return DateFormat.format(timeFormatString, time).toString();
        } else if (now.get(Calendar.DATE) - time.get(Calendar.DATE) == 1) {
            return yesterday + " " + DateFormat.format(timeFormatString, time);
        } else if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, time).toString();
        } else {
            return DateFormat.format(dateYearsFormatString, time).toString();
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView icon;
        public TextView text;
        public TextView timestamp;

        public ViewHolder(View itemView) {
            super(itemView);

            icon = (ImageView) itemView.findViewById(R.id.icon);
            text = (TextView) itemView.findViewById(R.id.text);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
        }
    }
}