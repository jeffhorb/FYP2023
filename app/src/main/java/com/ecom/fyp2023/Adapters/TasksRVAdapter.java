package com.ecom.fyp2023.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.AppManagers.TimeConverter;
import com.ecom.fyp2023.Fragments.BottomSheetFragmentAddTask;
import com.ecom.fyp2023.Fragments.UpdateTaskFragment;
import com.ecom.fyp2023.ModelClasses.Tasks;
import com.ecom.fyp2023.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class TasksRVAdapter extends RecyclerView.Adapter<TasksRVAdapter.ViewHolder> {


    private List<Tasks> tasksList;

    private final Context context;
    FirebaseFirestore fb;
    SharedPreferenceManager sharedPreferenceManager;

    private String proIdFromSelectedPro;

    private String proIdFromAddTask;

    private OnEndDateUpdateListener endDateUpdateListener;


    public interface OnEndDateUpdateListener {
        void onEndDateUpdated(String updatedEndDate);
    }


    public interface OnTaskProgressUpdateListener {
        void onTaskProgressUpdated();
    }
    private OnTaskProgressUpdateListener taskProgressUpdateListener;


    // Setter method to set the selected project
    public void setSelectedProject(String selectedProject) {
        this.proIdFromSelectedPro = selectedProject;
        this.proIdFromAddTask = selectedProject;
        notifyDataSetChanged(); // Refresh the RecyclerView
    }

    public TasksRVAdapter(List<Tasks> tasksList, Context context, OnTaskProgressUpdateListener listener, OnEndDateUpdateListener endDateUpdateListener) {
        this.tasksList = tasksList;
        this.context = context;
        this.sharedPreferenceManager = new SharedPreferenceManager(context);
        this.taskProgressUpdateListener = listener;
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

        holder.taskDetails.setText(tasks.getTaskDetails());
        holder.taskDiff.setText(tasks.getDifficulty());
        holder.taskEstimatedTime.setText(tasks.getEstimatedTime());
        holder.option.setOnClickListener(v -> showPopupMenu(holder.option, holder.getAdapterPosition()));



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

        boolean isComplete = sharedPreferenceManager.getTaskProgress(tasks.getTaskDetails(), false);


        if (isComplete) {
            holder.done.setImageResource(R.drawable.baseline_check_circle_outline_24);

        } else {
            holder.done.setImageResource(R.drawable.baseline_radio_button_unchecked_24);
        }

        holder.updateTextColor(isComplete);

        holder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isComplete = !sharedPreferenceManager.getTaskProgress(tasks.getTaskDetails(), false);

                FirestoreManager firestoreManager = new FirestoreManager();
                firestoreManager.getDocumentId("Tasks", "taskDetails", tasks.getTaskDetails(), new FirestoreManager.OnDocumentIdRetrievedListener() {
                    @Override
                    public void onDocumentIdRetrieved(String documentId) {
                        if (documentId != null) {
                            // Update the task progress in Firestore
                            updateTaskProgress(documentId, isComplete ? "Complete" : "Incomplete", isComplete ? "Task completed!" : "Task marked as incomplete!");
                            notifyDataSetChanged();
                            holder.updateTextColor(isComplete);

                            // Save progress in SharedPreferences
                            sharedPreferenceManager.saveTaskProgress(tasks.getTaskDetails(), isComplete);

                            // Notify the listener that the task progress is updated
                            if (taskProgressUpdateListener != null) {
                                taskProgressUpdateListener.onTaskProgressUpdated();
                            }
                        } else {
                            // Handle the case where the document ID couldn't be retrieved
                            Toast.makeText(context, "Failed to retrieve document ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }


    private void updateTaskProgress(String taskId, String progress, String completionMessage) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference dbTasks = firestore.collection("Tasks");

        // Update the "progress" field in Firestore
        dbTasks.document(taskId).update("progress", progress)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, completionMessage, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update task progress", Toast.LENGTH_SHORT).show());
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
        TextView taskDetails, taskEstimatedTime, taskDiff,completeT;
        ImageView done, option;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskDetails = itemView.findViewById(R.id.taskDetails);
            taskEstimatedTime = itemView.findViewById(R.id.taskTime);
            taskDiff = itemView.findViewById(R.id.taskDifficulty);
            done = itemView.findViewById(R.id.doneCheck);
            option = itemView.findViewById(R.id.taskButtonOptions);
            completeT = itemView.findViewById(R.id.completedT);

            taskDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (taskDetails.getMaxLines() == 2) {
                        taskDetails.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        taskDetails.setMaxLines(2);
                    }
                }
            });

        }

        public void updateTextColor(boolean isComplete) {
            if (isComplete) {
                completeT.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.green)); // Assuming you have a color resource named "green"
            } else {
                // Set the default text color here
                completeT.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
            }

        }
    }
    private void showUpdateFragmrnt(Tasks tasks) {

        // Create an instance of your UpdateFragment.
        UpdateTaskFragment updateFragment = new UpdateTaskFragment();

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
                    // Do nothing, simply close the dialog
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
                removeItemFromFirestore(documentId);
            }  // Handle the case where the document ID couldn't be retrieved
        });
        if (proIdFromSelectedPro != null) {
            // Example: Set some data based on the selected project
            calculateTotalEstimatedTimeAndEndDate(proIdFromSelectedPro);
        }
        if (proIdFromAddTask != null) {
            // Example: Set some data based on the selected project
            calculateTotalEstimatedTimeAndEndDate(proIdFromAddTask);
        }

    }

    private void removeItemFromFirestore(String documentId) {
        FirebaseFirestore.getInstance().collection("Tasks").document(documentId).delete().addOnCompleteListener(task -> {
            task.isSuccessful();

        });

    }

    private void calculateTotalEstimatedTimeAndEndDate(String projectId) {
        Log.d("CalculateTime", "Calculating total estimated time and end date for project: " + projectId);
        fb.collection("projectTasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AtomicLong totalEstimatedTime = new AtomicLong();

                        for (DocumentSnapshot document : task.getResult()) {
                            String taskId = document.getString("taskId");
                            Log.d("CalculateTime", "Processing task: " + taskId);

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
                                                    totalEstimatedTime.addAndGet(taskDays);

                                                    Log.d("CalculateTime", "Task processed. Total estimated time so far: " + totalEstimatedTime);

                                                    // Update the end date of the project based on the total estimated time
                                                    updateProjectEndDate(projectId, totalEstimatedTime.get());
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
                                    });
                        }
                    } else {
                        // Handle failure in fetching project tasks
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
                        // Handle failure in fetching project document
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