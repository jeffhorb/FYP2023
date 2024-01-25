package com.ecom.fyp2023.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;

import java.util.List;

public class TasksRVAdapter extends RecyclerView.Adapter<TasksRVAdapter.ViewHolder> {

    private final List<Tasks> tasksList;

    public TasksRVAdapter(List<Tasks> tasksList) {
        this.tasksList = tasksList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new ViewHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        Tasks tasks = tasksList.get(position);

        holder.taskDetails.setText(tasks.getTaskDetails());
        holder.taskDiff.setText(tasks.getDifficulty());
        holder.taskEstimatedTime.setText(tasks.getEstimatedTime());


        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the item click event, you might want to open a detailed view or perform some action
            }
        });*/


       /* //will used to code the done vector asset
        holder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a method to delete the item from Firestore
                removeProject(holder.getAdapterPosition());
            }
        });*/
    }


    @Override
    public int getItemCount() {
        return tasksList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskDetails, taskEstimatedTime, taskDiff;
        ImageView done, option;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskDetails = itemView.findViewById(R.id.taskDetails);
            taskEstimatedTime = itemView.findViewById(R.id.taskTime);
            taskDiff = itemView.findViewById(R.id.taskDifficulty);
            done = itemView.findViewById(R.id.doneCheck);
            option = itemView.findViewById(R.id.taskButtonOptions);

        }
    }
}
