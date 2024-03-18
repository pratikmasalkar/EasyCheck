package com.example.easycheck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewAttendanceAdapter extends RecyclerView.Adapter<ViewAttendanceAdapter.ViewHolder> {
    private List<Attendance> attendanceList;
    private Context context;
    private View_Attendance activity;
    private Attendance attendance;

    public ViewAttendanceAdapter(Context context, List<Attendance> attendanceList, View_Attendance activity) {
        this.context = context;
        this.attendanceList = attendanceList;
        this.activity = activity;
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.attendance_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewAttendanceAdapter.ViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);
        if (attendance != null) {
            holder.date_text_view.setText(attendance.getDate());
            holder.status_text_view1.setText(attendance.getStatus());
            holder.status_text_view2.setText(attendance.getStatus());
            holder.status_text_view3.setText(attendance.getStatus());
            if((attendance.getStatus()).equals("Present")) {
                holder.status_text_view1.setVisibility(View.VISIBLE);
                holder.status_text_view2.setVisibility(View.GONE);
                holder.status_text_view3.setVisibility(View.GONE);

            }else if((attendance.getStatus()).equals("Absent")){
                holder.status_text_view1.setVisibility(View.GONE);
                holder.status_text_view2.setVisibility(View.VISIBLE);
                holder.status_text_view3.setVisibility(View.GONE);
            }else {
                holder.status_text_view1.setVisibility(View.GONE);
                holder.status_text_view2.setVisibility(View.GONE);
                holder.status_text_view3.setVisibility(View.VISIBLE);
            }
            holder.code_text_view.setText(attendance.getCode());
        }

    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView date_text_view;
        TextView status_text_view1,status_text_view2,status_text_view3;
        TextView code_text_view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date_text_view = itemView.findViewById(R.id.date_textview);
            status_text_view1 = itemView.findViewById(R.id.status_text_view1);
            status_text_view2= itemView.findViewById(R.id.status_text_view2);
            status_text_view3= itemView.findViewById(R.id.status_text_view3);
            code_text_view = itemView.findViewById(R.id.code_text_view);
        }
    }
}
