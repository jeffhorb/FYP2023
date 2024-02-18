package com.ecom.fyp2023.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.Fragments.UpdateTaskFragment;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;
import com.ecom.fyp2023.TaskActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TasksRVAdapter extends RecyclerView.Adapter<TasksRVAdapter.ViewHolder> {

    private List<Tasks> tasksList;

    private final Context context;
    FirebaseFirestore fb;
    SharedPreferenceManager sharedPreferenceManager;

    private String proIdFromSelectedPro;
    Tasks selectedTask;
    private String proIdFromAddTask;

    private final OnEndDateUpdateListener endDateUpdateListener;

    public interface OnEndDateUpdateListener {
        void onEndDateUpdated(String updatedEndDate);
    }

    // Setter method to set the selected project
    public void setSelectedProject(String selectedProject) {
        this.proIdFromSelectedPro = selectedProject;
        this.proIdFromAddTask = selectedProject;
    }

    public TasksRVAdapter(List<Tasks> tasksList, Context context, OnEndDateUpdateListener endDateUpdateListener ) {
        this.tasksList = tasksList;
        this.context = context;
        this.sharedPreferenceManager = new SharedPreferenceManager(context);
        this.fb = FirebaseFirestore.getInstance();
        this.endDateUpdateListener = endDateUpdateListener;
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

        holder.taskName.setText(tasks.getTaskName());
        holder.taskDiff.setText(tasks.getDifficulty());
        holder.taskEstimatedTime.setText(tasks.getEstimatedTime());
        holder.taskProgress.setText(tasks.getProgress());
        holder.option.setOnClickListener(v -> showPopupMenu(holder.option, holder.getAdapterPosition()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the item click event, you might want to open a detailed view or perform some action
                selectedTask = tasksList.get(holder.getAdapterPosition());

                Intent intent = new Intent(context, TaskActivity.class);

                // the selected project as an extra in the Intent
                intent.putExtra("selectedTask", selectedTask);
                intent.putExtra("projectId1",proIdFromAddTask);
                intent.putExtra("projectId2",proIdFromSelectedPro);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return tasksList.size();
    }

    public void updateList(@NonNull List<Tasks> itemList) {
        this.tasksList = itemList;
        notifyDataSetChanged();
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.project_menu_option, popupMenu.getMenu());
        // Set up a click listener for the menu items
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.deletePrject) {
                showRemoveConfirmationDialog(position);
                return true;
            } else if (id == R.id.updateProject) {
                Tasks tasks = tasksList.get(position);
                showUpdateFragmrnt(tasks);
                return true;
            }

            return false;
        });
        popupMenu.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskEstimatedTime, taskDiff,taskProgress;
        ImageView  option;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskN);
            taskEstimatedTime = itemView.findViewById(R.id.taskTime);
            taskDiff = itemView.findViewById(R.id.taskDifficulty);
            option = itemView.findViewById(R.id.taskButtonOptions);
            taskProgress = itemView.findViewById(R.id.progressTask);

            taskName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (taskName.getMaxLines() == 1) {
                        taskName.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        taskName.setMaxLines(1);
                    }
                }
            });
        }
    }

    private void showUpdateFragmrnt(Tasks tasks) {

        UpdateTaskFragment updateFragment = new UpdateTaskFragment();

        // Set the OnTaskUpdateListener to receive callbacks in ProjectActivity to update recycerview with updated task details
        updateFragment.setOnTaskUpdateListener((UpdateTaskFragment.OnTaskUpdateListener) context);

        // Pass data to the fragment using Bundle.
        Bundle bundle = new Bundle();
        bundle.putSerializable("tasks",  tasks);

        bundle.putString("pId", proIdFromSelectedPro);
        bundle.putString("pId2", proIdFromAddTask);

        updateFragment.setArguments(bundle);

        // Replace the existing fragment with the new fragment.
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.updateTaskfra, updateFragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    private void showRemoveConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to remove this project and its tasks?")
                .setPositiveButton("Yes", (dialogInterface, i) -> removeTask(position))
                .setNegativeButton("No", (dialogInterface, i) -> {
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeTask(int position) {
        Tasks removeTask = tasksList.remove(position);
        notifyItemRemoved(position);

        FirestoreManager firestoreManager = new FirestoreManager();
        firestoreManager.getDocumentId("Tasks", "taskDetails", removeTask.getTaskDetails(), documentId -> {
            if (documentId != null) {

                if (proIdFromSelectedPro != null) {
                    //Set some data based on the selected project
                    calculateTotalEstimatedTimeAndEndDate(proIdFromSelectedPro);
                }
                if (proIdFromAddTask != null) {
                    //Set some data based on the selected project
                    calculateTotalEstimatedTimeAndEndDate(proIdFromAddTask);
                }
                removeTaskFromProjectTasks(documentId);
                removeItemFromFirestore(documentId);
            }
        });
    }

    private void removeItemFromFirestore(String documentId) {
        FirebaseFirestore.getInstance().collection("Tasks").document(documentId).delete().addOnCompleteListener(task -> {
            task.isSuccessful();
        });
    }

    private void removeTaskFromProjectTasks(String taskId) {
        FirebaseFirestore.getInstance().collection("projectTasks")
                .whereEqualTo("taskId", taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String projectTaskId = document.getId();
                            removeItemFromProjectTasks(projectTaskId);
                        }
                    } else {
                        // Handle failure in fetching projectTasks
                        Log.e("RemoveProjectTask", "Error fetching projectTasks", task.getException());
                    }
                });
    }

    private void removeItemFromProjectTasks(String projectTaskId) {
        FirebaseFirestore.getInstance().collection("projectTasks").document(projectTaskId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("RemoveProjectTask", "Task removed from projectTasks successfully");
                    } else {
                        Log.e("RemoveProjectTask", "Error removing task from projectTasks", task.getException());
                    }
                });
    }

    private void calculateTotalEstimatedTimeAndEndDate(String projectId) {
        fb.collection("projectTasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AtomicLong highestEstimatedTime = new AtomicLong();
                        AtomicInteger tasksProcessed = new AtomicInteger(0);
                        int totalTasks = task.getResult().size();

                        for (DocumentSnapshot document : task.getResult()) {
                            String taskId = document.getString("taskId");

                            fb.collection("Tasks")
                                    .document(taskId)
                                    .get()
                                    .addOnCompleteListener(taskDocumentTask -> {
                                        if (taskDocumentTask.isSuccessful()) {
                                            DocumentSnapshot taskDocument = taskDocumentTask.getResult();

                                            if (taskDocument != null && taskDocument.exists()) {
                                                String estimatedTime = taskDocument.getString("estimatedTime");

                                                if (estimatedTime != null) {
                                                    long taskDays = TimeConverter.convertToDays(estimatedTime);

                                                    // Update the highest estimated time
                                                    highestEstimatedTime.updateAndGet(currentValue ->
                                                            Math.max(currentValue, taskDays)
                                                    );

                                                    Log.d("CalculateTime", "Task processed. Highest estimated time so far: " + highestEstimatedTime);
                                                } else {
                                                    Log.e("CalculateTime", "Estimated time is null for task: " + taskId);
                                                }
                                            } else {
                                                Log.e("CalculateTime", "Task document is null or doesn't exist for taskId: " + taskId);
                                            }
                                        } else {
                                            // Handle failure in fetching task document
                                            Log.e("CalculateTime", "Error fetching task document", taskDocumentTask.getException());
                                        }

                                        // Increment the counter for processed tasks
                                        tasksProcessed.incrementAndGet();

                                        // Check if all tasks have been processed
                                        if (tasksProcessed.get() == totalTasks) {
                                            // Update the project's end date based on the highest estimated time
                                            updateProjectEndDate(projectId, highestEstimatedTime.get());
                                        }
                                    });
                        }
                    } else {
                        Log.e("CalculateTime", "Error fetching project tasks", task.getException());
                    }
                });
    }

    private void updateProjectEndDate(String projectId, long totalEstimatedTime) {
        fb.collection("Projects")
                .document(projectId)
                .get()
                .addOnCompleteListener(projectDocumentTask -> {
                    if (projectDocumentTask.isSuccessful()) {
                        DocumentSnapshot projectDocument = projectDocumentTask.getResult();
                        if (projectDocument.exists()) {
                            String startDate = projectDocument.getString("startDate");
                            String updatedEndDate = calculateEndDate(startDate, totalEstimatedTime);

                            fb.collection("Projects")
                                    .document(projectId)
                                    .update("endDate", updatedEndDate)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Log.d("UpdateProject", "Project end date updated successfully");
                                            if (endDateUpdateListener != null) {
                                                endDateUpdateListener.onEndDateUpdated(updatedEndDate);
                                            }
                                        } else {
                                            Log.e("UpdateProject", "Error updating project end date", updateTask.getException());
                                        }

                                    });
                        }
                    } else {
                        Log.e("UpdateProject", "Error fetching project document", projectDocumentTask.getException());
                    }
                });
    }

    @Nullable
    private String calculateEndDate(String startDate, long totalEstimatedTime) {
        try {
            // Parse the start date
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date parsedStartDate = sdf.parse(startDate);

            // Calculate end date based on start date and total estimated time
            if (parsedStartDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parsedStartDate);
                calendar.add(Calendar.DAY_OF_YEAR, (int) totalEstimatedTime);

                // Format the updated end date
                return sdf.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}