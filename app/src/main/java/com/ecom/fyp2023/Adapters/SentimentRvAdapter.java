package com.ecom.fyp2023.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecom.fyp2023.ModelClasses.Sentiments;
import com.ecom.fyp2023.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SentimentRvAdapter extends RecyclerView.Adapter<SentimentRvAdapter.ViewHolder> {

    private ArrayList<Sentiments> sentimentsList;
    private Context context;

    public SentimentRvAdapter(ArrayList<Sentiments> sentimentsList, Context context) {
        this.sentimentsList = sentimentsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sentiment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sentiments sentiment = sentimentsList.get(position);
        holder.sentimentTextView.setText(sentiment.getSentiment());
        holder.timestampTextView.setText(getFormattedTimestamp(sentiment.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return sentimentsList.size();
    }

    public void update(ArrayList<Sentiments> updatedList) {
        sentimentsList.clear();
        sentimentsList.addAll(updatedList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sentimentTextView;
        TextView timestampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sentimentTextView = itemView.findViewById(R.id.sentiment_text);
            timestampTextView = itemView.findViewById(R.id.timestamp_text);
        }
    }

    // Helper method to format timestamp
    @NonNull
    private String getFormattedTimestamp(Date timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(timestamp);
    }
}
