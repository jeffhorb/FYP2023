package com.ecom.fyp2023;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProjectsRVAdapter extends RecyclerView.Adapter<ProjectsRVAdapter.ViewHolder> {
    // creating variables for our ArrayList and context
    private List<Projects> projectsArrayList;

    private Context context;

    private FirebaseFirestore db;

    // creating constructor for our adapter class
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
        holder.projectT.setText(projects.getTitle());

        holder.buttonOptions.setOnClickListener(v -> showPopupMenu(holder.buttonOptions, holder.getAdapterPosition()));


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
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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
            }
        });
        popupMenu.show();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView projectT;

        ImageButton buttonOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views.
            projectT = itemView.findViewById(R.id.projectTitle);
            buttonOptions = itemView.findViewById(R.id.buttonOptions);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // After clicking the item in the RecyclerView.
                    // Retrieve data from the clicked item.

                }

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
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeProject(position);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing, simply close the dialog
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    private void removeProject(int position) {
        Projects removedProject = projectsArrayList.remove(position);
        notifyItemRemoved(position);

        FirestoreManager firestoreManager = new FirestoreManager();
        firestoreManager.getDocumentId("Projects", "title", removedProject.getTitle(), new FirestoreManager.OnDocumentIdRetrievedListener() {
            @Override
            public void onDocumentIdRetrieved(String documentId) {
                if (documentId != null) {
                    removeItemFromFirestore(documentId);
                } else {
                    // Handle the case where the document ID couldn't be retrieved
                }
            }

        });
    }

    private void removeItemFromFirestore(String documentId) {
        FirebaseFirestore.getInstance().collection("Projects").document(documentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    // Document successfully removed from Firestore
                } else {
                    // Handle the error
                }
            }
        });

    }

}