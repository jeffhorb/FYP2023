package com.ecom.fyp2023;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProjectsRVAdapter extends RecyclerView.Adapter<ProjectsRVAdapter.ViewHolder> {
    // creating variables for our ArrayList and context
    private List<Projects> projectsArrayList;
    private Context context;



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

        holder.buttonOptions.setOnClickListener(v -> showPopupMenu(holder.buttonOptions));

    }

    @Override
    public int getItemCount() {
        // returning the size of our array list.
        return projectsArrayList.size();
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.project_menu_option, popupMenu.getMenu());

        // Set up a click listener for the menu items
        popupMenu.setOnMenuItemClickListener(item -> handleMenuItemClick(item));

        popupMenu.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our text views.
        private TextView projectT;

        ImageButton buttonOptions;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views.
            projectT = itemView.findViewById(R.id.projectTitle);
            buttonOptions = itemView.findViewById(R.id.buttonOptions);
        }
    }

    private boolean handleMenuItemClick(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.updateProject) {
            return true;
        }
        if (id == R.id.deletePrject) {

            return true;
        }
        return false;
    }

}