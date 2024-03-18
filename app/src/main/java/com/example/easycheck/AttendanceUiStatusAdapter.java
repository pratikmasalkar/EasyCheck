package com.example.easycheck;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AttendanceUiStatusAdapter extends ArrayAdapter<String> {


    private final Context context;
    private final List<String> statuses;

    public AttendanceUiStatusAdapter(Context context, List<String> statuses) {
        super(context, android.R.layout.simple_list_item_1, statuses);
        this.context = context;
        this.statuses = statuses;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getView(position, convertView, parent);
        String status = statuses.get(position);

        int color;
        switch (status) {
            case "Present":
                color = context.getResources().getColor(R.color.colorPresent);
                break;
            case "Absent":
                color = context.getResources().getColor(R.color.colorAbsent);
                break;
            case "Invalid":
                color = context.getResources().getColor(R.color.colorInvalid);
                break;
            case "Incomplete":
                color = context.getResources().getColor(R.color.colorIncomplete);
                break;
            default:
                color = context.getResources().getColor(R.color.colorDefault);
                break;
        }

        textView.setText(status);
        textView.setTextColor(color);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);

        return textView;
    }
}
