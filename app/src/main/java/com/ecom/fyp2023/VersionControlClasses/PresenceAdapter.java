package com.ecom.fyp2023.VersionControlClasses;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.R;

import java.util.List;

public class PresenceAdapter extends RecyclerView.Adapter<PresenceAdapter.PresenceViewHolder> {
    private List<Presence> presenceList;

    public PresenceAdapter(List<Presence> presenceList) {
        this.presenceList = presenceList;
    }
    public void updateData(List<Presence> newData) {
        presenceList.clear();
        this.presenceList.addAll(newData);
        Log.d("TAG", "Updating data: " + newData.size() + " items");

        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public PresenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.presence_item, parent, false);
        return new PresenceViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull PresenceViewHolder holder, int position) {
        Presence presence = presenceList.get(position);

        Log.d("TAG", "Binding view for user: " + presence.getUsername());

        holder.usernameTextView.setText(presence.getUsername());

    }
    @Override
    public int getItemCount() {
        return presenceList.size();
    }
    static class PresenceViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;

        PresenceViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
        }
    }
}
