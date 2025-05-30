package org.pada.ice.frottori.lab_control;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ComputerListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] computers;
    private final Boolean[] onlineStatus;

    public ComputerListAdapter(Context context, String[] computers, Boolean[] onlineStatus) {
        super(context, android.R.layout.simple_list_item_multiple_choice, computers);
        this.context = context;
        this.computers = computers;
        this.onlineStatus = onlineStatus;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Use the default layout for multiple choice items
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        // Change text color based on online status
        if (onlineStatus[position]) {
            textView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));  // Online = green
        } else {
            textView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));    // Offline = red
        }

        return view;
    }
}
