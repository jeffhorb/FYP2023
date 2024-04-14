package com.ecom.fyp2023.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.AppManagers.TimeUtils;
import com.ecom.fyp2023.ModelClasses.VersionModel;
import com.ecom.fyp2023.R;
import com.ecom.fyp2023.VersionControlClasses.VersionDifference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.List;

public class VersionAdapter extends RecyclerView.Adapter<VersionAdapter.ViewHolder> {

    private List<VersionModel> versionList;
    private Context context;
    private String filePath;
    private String fileId;

    public VersionAdapter(List<VersionModel> versionList, Context context, String filePath, String fileId) {
        this.versionList = versionList;
        this.context = context;
        this.filePath = filePath;
        this.fileId = fileId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_version_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VersionModel version = versionList.get(position);

        String userId = version.getUserId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");

        usersRef.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userName = document.getString("userName");
                            holder.textUser.setText(userName);
                        }
                    } else {
                        Log.e("TAG", "Error getting user document: ", task.getException());
                    }
                });

        Date timestamp = version.getTimestamp();
        String timeAgo = TimeUtils.getTimeAgo(timestamp);
        holder.textTimestamp.setText(timeAgo);

        holder.textSummary.setText(version.getMessage());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VersionDifference.class);
                intent.putExtra("content",version.getContent());
                long timestampMillis = version.getTimestamp().getTime();
                intent.putExtra("timestamp", timestampMillis);
                intent.putExtra("fileId",fileId);
                intent.putExtra("filePath", filePath);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return versionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUser;
        TextView textTimestamp;
        TextView textSummary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textUser = itemView.findViewById(R.id.text_user);
            textTimestamp = itemView.findViewById(R.id.text_timestamp);
            textSummary = itemView.findViewById(R.id.summary);


        }
    }
}

