package com.ecom.fyp2023.TeamManagementClasses;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.Analysis.TeamMemberEvaluation;
import com.ecom.fyp2023.AppManagers.SharedPreferenceManager;
import com.ecom.fyp2023.ModelClasses.Projects;
import com.ecom.fyp2023.R;

import java.util.List;

public class GroupMemberProjectAdapter extends RecyclerView.Adapter<GroupMemberProjectAdapter.ViewHolder> {

    private List<Projects> projectsList;
    private Context context;
    private String userId;
    private  String userName;



    public GroupMemberProjectAdapter(Context context, List<Projects> projectsList, String userId, String userName) {
        this.context = context;
        this.projectsList = projectsList;
        this.userId = userId;
        this.userName = userName;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_project_item_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Projects project = projectsList.get(position);
        holder.projectNameTextView.setText(project.getTitle());
        holder.descriptionTextView.setText(project.getDescription());
        holder.progressTextView.setText(project.getProgress());

        holder.itemView.setOnClickListener(v -> {
            navigateToTeamMemberEvaluation(project, userId,userName);
        });
    }

    @Override
    public int getItemCount() {
        return projectsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView projectNameTextView;
        public TextView descriptionTextView;
        public TextView progressTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(R.id.projectNameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            progressTextView = itemView.findViewById(R.id.progressTextView);
        }
    }

    private void navigateToTeamMemberEvaluation(@NonNull Projects project, String userId,String userName) {
        String projectId = project.getProjectId();
        Intent intent = new Intent(context, TeamMemberEvaluation.class);
        intent.putExtra("userID", userId);
        intent.putExtra("userName", userName);
        intent.putExtra("projID", projectId);
        context.startActivity(intent);
    }
}

