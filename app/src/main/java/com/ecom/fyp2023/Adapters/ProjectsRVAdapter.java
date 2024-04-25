package com.ecom.fyp2023.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.AppManagers.FirestoreManager;
import com.ecom.fyp2023.Fragments.UpdateProject;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.ProjectActivity;
import com.ecom.fyp2023.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProjectsRVAdapter extends RecyclerView.Adapter<ProjectsRVAdapter.ViewHolder> {

    private List<Projects> projectsArrayList;

    private final Context context;

    public ProjectsRVAdapter(ArrayList<Projects> projectsArrayList, Context context) {
        this.projectsArrayList = projectsArrayList;
        this.context = context;

    }

    public void updateList(List<Projects> itemList) {
        this.projectsArrayList = itemList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // passing our layout file for displaying our card item
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.project_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectsRVAdapter.ViewHolder holder, int position) {
        // setting data to our text views from our modal class.
        Projects projects = projectsArrayList.get(position);

        if (projects.getProgress().equalsIgnoreCase("In Progress") ||
                projects.getProgress().equalsIgnoreCase("Incomplete")) {
            holder.projectT.setText(projects.getTitle());
            holder.projectP.setText(projects.getProgress());
            holder.buttonOptions.setOnClickListener(v -> showPopupMenu(holder.buttonOptions, holder.getAdapterPosition()));
        } else {
            // Hide the project with progress other than "In Progress" or "Incomplete"
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
//        FirestoreManager firestoreManager = new FirestoreManager();
//        firestoreManager.getDocumentId("Projects", "title", projects.getTitle(), documentId -> {
//            if (documentId != null) {
//
//               String projectId = documentId;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        // returning the size of our array list.
        return projectsArrayList.size();
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
            }else if(id == R.id.updateProject){

                Projects project = projectsArrayList.get(position);
                showUpdateFragmrnt(project);

                return true;
            }

            return false;
        });
        popupMenu.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView projectT;
        private final TextView projectP;

        ImageButton buttonOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views.

            buttonOptions = itemView.findViewById(R.id.buttonOptions);
            projectP = itemView.findViewById(R.id.proProgress);
            projectT = itemView.findViewById(R.id.projectTitle);
            projectT.setMaxLines(1);
            // Set an onClickListener for the TextView
            projectP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (projectP.getMaxLines() == 1) {
                        projectP.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        projectP.setMaxLines(1);
                    }
                }
            });

            itemView.setOnClickListener(v -> {

                Projects selectedProject = projectsArrayList.get(getAdapterPosition());
                Intent intent = new Intent(itemView.getContext(), ProjectActivity.class);
                // the selected project as an extra in the Intent
                intent.putExtra("selectedProject", selectedProject);
                itemView.getContext().startActivity(intent);
            });
        }
    }

    private void showUpdateFragmrnt(Projects project) {

        // Create an instance of your UpdateFragment.
        UpdateProject updateFragment = new UpdateProject();

        // Pass data to the fragment using Bundle.
        Bundle bundle = new Bundle();
        bundle.putSerializable("project",  project);
        updateFragment.setArguments(bundle);

        // Replace the existing fragment with the new fragment.
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.updatefra, updateFragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    private void showRemoveConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to remove this project and its tasks?")
                .setPositiveButton("Yes", (dialogInterface, i) -> removeProject(position))
                .setNegativeButton("No", (dialogInterface, i) -> {
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeProject(int position) {
        Projects removedProject = projectsArrayList.remove(position);
        notifyItemRemoved(position);

        FirestoreManager firestoreManager = new FirestoreManager();
        firestoreManager.getDocumentId("Projects", "title", removedProject.getTitle(), documentId -> {
            if (documentId != null) {
                removeUserPorject(documentId);
                removeItemFromFirestore(documentId);
                removeProjectTasks(documentId);
                removeProjectComments(documentId);
            }  // Handle the case where the document ID couldn't be retrieved
        });
    }

    private void removeItemFromFirestore(String documentId) {
        FirebaseFirestore.getInstance().collection("Projects").document(documentId).delete().addOnCompleteListener(task -> {
            task.isSuccessful();
        });
    }

    private void removeUserPorject(String projectid) {
        FirebaseFirestore.getInstance().collection("userProjects")
                .whereEqualTo("projectId", projectid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String userProjectId = document.getId();
                            removeItemFromUserProject(userProjectId);
                        }
                    } else {
                        // Handle failure in fetching projectTasks
                        Log.e("RemoveProjectTask", "Error fetching projectTasks", task.getException());
                    }
                });
    }

    private void removeItemFromUserProject(String userProjectId) {
        FirebaseFirestore.getInstance().collection("userProjects").document(userProjectId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("RemoveProjectTask", "Task removed from projectTasks successfully");
                    } else {
                        Log.e("RemoveProjectTask", "Error removing task from projectTasks", task.getException());
                    }
                });
    }

    private void removeProjectTasks(String projectId) {
        FirebaseFirestore.getInstance().collection("projectTasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String taskDocumentId = document.getString("taskId");
                            if (taskDocumentId != null) {
                                removeTask(taskDocumentId);
                                removeTaskNotes(taskDocumentId);
                                removeTaskUser(taskDocumentId);
                            }
                            // After removing tasks from projectTasks, you may want to remove the projectTasks entry as well
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("RemoveProjectTask", "Task removed from projectTasks successfully"))
                                    .addOnFailureListener(e -> Log.e("RemoveProjectTask", "Error removing task from projectTasks", e));
                        }
                    } else {
                        Log.e("RemoveProjectTask", "Error fetching projectTasks", task.getException());
                    }
                });
    }

    private void removeTask(String taskId) {
        FirebaseFirestore.getInstance().collection("Tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("RemoveTask", "Task removed successfully"))
                .addOnFailureListener(e -> Log.e("RemoveTask", "Error removing task", e));
    }

    private void removeTaskNotes(String taskId) {
        FirebaseFirestore.getInstance().collection("taskNotes")
                .whereEqualTo("taskId", taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String noteId = document.getString("noteId");
                            if (noteId != null) {
                                removeNote(noteId); // Remove note from "Notes" collection
                            }
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("RemoveTaskNote", "Note removed successfully"))
                                    .addOnFailureListener(e -> Log.e("RemoveTaskNote", "Error removing note", e));
                        }
                    } else {
                        Log.e("RemoveTaskNote", "Error fetching taskNotes", task.getException());
                    }
                });
    }

    private void removeTaskUser(String taskId) {
        FirebaseFirestore.getInstance().collection("userTasks")
                .whereEqualTo("taskId", taskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("RemoveTaskNote", "Note removed successfully"))
                                    .addOnFailureListener(e -> Log.e("RemoveTaskNote", "Error removing note", e));
                        }
                    } else {
                        Log.e("RemoveTaskNote", "Error fetching taskNotes", task.getException());
                    }
                });
    }

    private void removeNote(String noteId) {
        FirebaseFirestore.getInstance().collection("Notes").document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("RemoveNote", "Note removed successfully"))
                .addOnFailureListener(e -> Log.e("RemoveNote", "Error removing note", e));
    }

    private void removeProjectComments(String projectId) {
        FirebaseFirestore.getInstance().collection("ProjectComments")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String commentDocumentId = document.getString("commentId");
                            if (commentDocumentId != null) {
                                removeComment(commentDocumentId);
                            }
                            // After removing tasks from projectTasks, you may want to remove the projectTasks entry as well
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("RemoveProjectTask", "Task removed from projectTasks successfully"))
                                    .addOnFailureListener(e -> Log.e("RemoveProjectTask", "Error removing task from projectTasks", e));
                        }
                    } else {
                        Log.e("RemoveProjectTask", "Error fetching projectTasks", task.getException());
                    }
                });
    }

    private void removeComment(String commentId) {
        FirebaseFirestore.getInstance().collection("Comments").document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("RemoveComment", "Comment removed successfully"))
                .addOnFailureListener(e -> Log.e("RemoveComent", "Error removing comment", e));
    }
}