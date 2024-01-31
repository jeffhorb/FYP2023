package com.ecom.fyp2023.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CompletedProjectsAdapter extends RecyclerView.Adapter<CompletedProjectsAdapter.ViewHolder> {
    // creating variables for our ArrayList and context
    private List<Projects> projectsArrayList;

    private final Context context;


    // creating constructor for our adapter class
    public CompletedProjectsAdapter(ArrayList<Projects> projectsArrayList, Context context) {
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
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.completed_projects_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // setting data to our text views from our modal class.
        Projects projects = projectsArrayList.get(position);

        if (projects.getProgress().equalsIgnoreCase("Complete")) {
            holder.projectT.setText(projects.getTitle());
            holder.projectProgress.setText(projects.getProgress());
            holder.startDate.setText(projects.getStartDate());
            holder.endDate.setText(projects.getEndDate());

            holder.buttonOptions.setOnClickListener(v -> showPopupMenu(holder.buttonOptions, holder.getAdapterPosition()));
        } else {
            // Hide the project with progress other than "Complete"
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
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

        TextView projectProgress, startDate,endDate;

        ImageButton buttonOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views.
            projectT = itemView.findViewById(R.id.completedPTitle);
            buttonOptions = itemView.findViewById(R.id.CompletedButtonOptions);
            projectProgress = itemView.findViewById(R.id.completedPProgress);
            startDate = itemView.findViewById(R.id.completedPStartDate);
            endDate = itemView.findViewById(R.id.completedPEndDate);

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
                    // Do nothing, simply close the dialog
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
                removeItemFromFirestore(documentId);
            }  // Handle the case where the document ID couldn't be retrieved

        });
    }

    private void removeItemFromFirestore(String documentId) {
        FirebaseFirestore.getInstance().collection("Projects").document(documentId).delete().addOnCompleteListener(task -> {
            task.isSuccessful();
            // Document successfully removed from Firestore
            // Handle the error
        });

    }

}