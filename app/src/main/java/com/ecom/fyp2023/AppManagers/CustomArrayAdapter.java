package com.ecom.fyp2023.AppManagers;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ecom.fyp2023.Fragments.BottomSheetFragmentAddTask;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<String> {

    public interface TaskIdProvider {
        void provideTaskId(String taskName, BottomSheetFragmentAddTask.OnTaskIdFetchedListener listener);
    }

    private final TaskIdProvider taskIdProvider;
    private final List<String> selectedPrerequisites;

    public CustomArrayAdapter(@NonNull Context context, int resource, @NonNull List<String> objects, List<String> selectedPrerequisites, TaskIdProvider taskIdProvider) {
        super(context, resource, objects);
        this.selectedPrerequisites = selectedPrerequisites;
        this.taskIdProvider = taskIdProvider;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        highlightItem(view, getItem(position));
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        highlightItem(view, getItem(position));
        return view;
    }

    private void highlightItem(View view, String taskName) {
        if (view != null) {
            TextView textView = view.findViewById(android.R.id.text1);
            if (textView != null) {
                taskIdProvider.provideTaskId(taskName, new BottomSheetFragmentAddTask.OnTaskIdFetchedListener() {
                    @Override
                    public void onTaskIdFetched(String taskId) {
                        if (taskId != null && selectedPrerequisites.contains(taskId)) {
                            // Highlight the selected item
                            textView.setTextColor(Color.BLUE);
                        } else {
                            // Reset the color for unselected items
                            textView.setTextColor(Color.BLACK);
                        }
                    }
                });
            }
       }
    }
    public void setData(List<String> newData) {
        clear();
        addAll(newData);
        notifyDataSetChanged();
    }
}
